package com.jmane2026.oldschoollevels.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jmane2026.oldschoollevels.OldSchoolLevels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
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

public class SmithingStatsManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Item, SmithingStat> SMITHING_STATS = new HashMap<>();

    public static void load() {
        SMITHING_STATS.clear();
        
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(OldSchoolLevels.MODID);
        File configFile = configDir.resolve("smithing_stats.json").toFile();

        if (!configFile.exists()) {
            createDefaultFile(configDir.toFile(), configFile);
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type listType = new TypeToken<List<SmithingStat>>() {}.getType();
            List<SmithingStat> stats = GSON.fromJson(reader, listType);

            if (stats != null) {
                for (SmithingStat stat : stats) {
                    Identifier itemId = Identifier.tryParse(stat.item());
                    if (itemId != null && BuiltInRegistries.ITEM.containsKey(itemId)) {
                        Item item = BuiltInRegistries.ITEM.getValue(itemId);
                        SMITHING_STATS.put(item, stat);
                    } else {
                        OldSchoolLevels.LOGGER.warn("SmithingStatsManager: Unknown item {}", stat.item());
                    }
                }
            }
            OldSchoolLevels.LOGGER.info("Loaded {} custom smithing stats.", SMITHING_STATS.size());
        } catch (Exception e) {
            OldSchoolLevels.LOGGER.error("Failed to load smithing_stats.json", e);
        }
    }

    private static void createDefaultFile(File configDir, File configFile) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        List<SmithingStat> defaults = new ArrayList<>();
        // Add examples for smelting and crafting
        defaults.add(new SmithingStat("oritech:platinum_ingot", 45, 100));

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(defaults, writer);
        } catch (IOException e) {
            OldSchoolLevels.LOGGER.error("Failed to create default smithing_stats.json", e);
        }
    }

    public static SmithingStat getStat(Item item) {
        return SMITHING_STATS.get(item);
    }

    public static Map<Item, SmithingStat> getAllStats() {
        return SMITHING_STATS;
    }
}
