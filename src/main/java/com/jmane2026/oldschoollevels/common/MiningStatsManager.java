package com.jmane2026.oldschoollevels.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jmane2026.oldschoollevels.OldSchoolLevels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiningStatsManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Block, MiningStat> MINING_STATS = new HashMap<>();

    public static void load() {
        MINING_STATS.clear();
        
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(OldSchoolLevels.MODID);
        File configFile = configDir.resolve("mining_stats.json").toFile();

        if (!configFile.exists()) {
            createDefaultFile(configDir.toFile(), configFile);
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type listType = new TypeToken<List<MiningStat>>() {}.getType();
            List<MiningStat> stats = GSON.fromJson(reader, listType);

            if (stats != null) {
                for (MiningStat stat : stats) {
                    Identifier blockId = Identifier.tryParse(stat.block());
                    if (blockId != null && BuiltInRegistries.BLOCK.containsKey(blockId)) {
                        Block block = BuiltInRegistries.BLOCK.getValue(blockId);
                        MINING_STATS.put(block, stat);
                    } else {
                        OldSchoolLevels.LOGGER.warn("MiningStatsManager: Unknown block {}", stat.block());
                    }
                }
            }
            OldSchoolLevels.LOGGER.info("Loaded {} custom mining stats.", MINING_STATS.size());
        } catch (Exception e) {
            OldSchoolLevels.LOGGER.error("Failed to load mining_stats.json", e);
        }
    }

    private static void createDefaultFile(File configDir, File configFile) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        List<MiningStat> defaults = new ArrayList<>();
        // Add an example
        defaults.add(new MiningStat("oritech:platinum_ore", 45, 75));

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(defaults, writer);
        } catch (IOException e) {
            OldSchoolLevels.LOGGER.error("Failed to create default mining_stats.json", e);
        }
    }

    public static MiningStat getStat(Block block) {
        return MINING_STATS.get(block);
    }

    public static Map<Block, MiningStat> getAllStats() {
        return MINING_STATS;
    }
}
