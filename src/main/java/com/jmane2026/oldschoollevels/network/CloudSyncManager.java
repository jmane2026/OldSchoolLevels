package com.jmane2026.oldschoollevels.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.OSLConfig;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.SkillAttributeHandler;
import com.jmane2026.oldschoollevels.common.SkillData;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@EventBusSubscriber(modid = OldSchoolLevels.MODID)
public class CloudSyncManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HttpClient client = HttpClient.newHttpClient();

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (OSLConfig.ENABLE_CLOUD_SYNC.get() && event.getEntity() instanceof ServerPlayer player) {
            downloadPlayerData(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (OSLConfig.ENABLE_CLOUD_SYNC.get() && event.getEntity() instanceof ServerPlayer player) {
            uploadPlayerData(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (OSLConfig.ENABLE_CLOUD_SYNC.get() && event.getEntity() instanceof ServerPlayer player) {
            // Upload every 5 minutes (6000 ticks) to prevent data loss on crash
            if (player.tickCount % 6000 == 0) {
                uploadPlayerData(player);
            }
        }
    }

    public static void downloadPlayerData(ServerPlayer player) {
        UUID uuid = player.getUUID();
        LOGGER.info("[OSL] Requesting cloud data for UUID: " + uuid);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OSLConfig.SUPABASE_URL.get() + "?uuid=eq." + uuid.toString()))
                .header("apikey", OSLConfig.SUPABASE_API_KEY.get())
                .header("Authorization", "Bearer " + OSLConfig.SUPABASE_API_KEY.get())
                .header("Accept", "application/json")
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            LOGGER.info("[OSL] Received response from Supabase: " + response.body());
                            JsonArray array = JsonParser.parseString(response.body()).getAsJsonArray();
                            if (array.size() > 0) {
                                JsonObject data = array.get(0).getAsJsonObject();
                                player.level().getServer().execute(() -> applyDownloadedData(player, data));
                            } else {
                                LOGGER.info("[OSL] No cloud data found for UUID: " + uuid);
                            }
                        } catch (Exception e) {
                            LOGGER.error("Failed to parse downloaded player data", e);
                        }
                    } else {
                        LOGGER.error("[OSL] Failed to fetch data. Status code: " + response.statusCode() + " Body: " + response.body());
                    }
                })
                .exceptionally(e -> {
                    LOGGER.error("Failed to download player data", e);
                    return null;
                });
    }

    private static void applyDownloadedData(ServerPlayer player, JsonObject data) {
        SkillData localData = player.getData(ModAttachments.SKILLS.get());
        boolean changed = false;
        
        for (Skill skill : Skill.values()) {
            String colName = skill.name().toLowerCase() + "_xp";
            if (data.has(colName) && !data.get(colName).isJsonNull()) {
                long cloudXp = data.get(colName).getAsLong();
                long currentXp = localData.getExperience(skill);
                if (cloudXp > currentXp) {
                    LOGGER.info("[OSL] Syncing " + skill.name() + " from cloud: " + cloudXp + " (was " + currentXp + ")");
                    localData = localData.setExperience(skill, cloudXp);
                    changed = true;
                }
            }
        }
        
        if (data.has("cheater") && !data.get("cheater").isJsonNull()) {
            boolean cloudCheater = data.get("cheater").getAsBoolean();
            boolean localCheater = player.getData(ModAttachments.CHEATER.get());
            if (cloudCheater && !localCheater) {
                player.setData(ModAttachments.CHEATER.get(), true);
                LOGGER.info("[OSL] Marked player as cheater from cloud sync.");
            }
        }
        
        if (changed) {
            player.setData(ModAttachments.SKILLS.get(), localData);
            player.syncData(ModAttachments.SKILLS.get());
            SkillAttributeHandler.refreshAttributes(player);
            LOGGER.info("[OSL] Successfully applied and synced cloud data to client.");
        }
    }

    public static void uploadPlayerData(ServerPlayer player) {
        UUID uuid = player.getUUID();
        String username = player.getGameProfile().name();
        SkillData localData = player.getData(ModAttachments.SKILLS.get());
        boolean cheater = player.getData(ModAttachments.CHEATER.get());
        
        JsonObject payload = new JsonObject();
        payload.addProperty("uuid", uuid.toString());
        payload.addProperty("username", username);
        payload.addProperty("cheater", cheater);
        payload.addProperty("mc_version", SharedConstants.getCurrentVersion().name()); // Works on both client and server
        
        for (Skill skill : Skill.values()) {
            String colName = skill.name().toLowerCase() + "_xp";
            payload.addProperty(colName, localData.getExperience(skill));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OSLConfig.SUPABASE_URL.get()))
                .header("apikey", OSLConfig.SUPABASE_API_KEY.get())
                .header("Authorization", "Bearer " + OSLConfig.SUPABASE_API_KEY.get())
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates") // UPSERT behavior
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .exceptionally(e -> {
                    LOGGER.error("Failed to upload player data", e);
                    return null;
                });
    }
}
