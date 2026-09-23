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

public class CookingStatsManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Item, CookingStat> COOKING_STATS = new HashMap<>();

    public static void load() {
        COOKING_STATS.clear();
        
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(OldSchoolLevels.MODID);
        File configFile = configDir.resolve("cooking_stats.json").toFile();

        if (!configFile.exists()) {
            createDefaultFile(configDir.toFile(), configFile);
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type listType = new TypeToken<List<CookingStat>>() {}.getType();
            List<CookingStat> stats = GSON.fromJson(reader, listType);

            if (stats != null) {
                for (CookingStat stat : stats) {
                    Identifier itemId = Identifier.tryParse(stat.item());
                    if (itemId != null && BuiltInRegistries.ITEM.containsKey(itemId)) {
                        Item item = BuiltInRegistries.ITEM.getValue(itemId);
                        COOKING_STATS.put(item, stat);
                    } else {
                        OldSchoolLevels.LOGGER.warn("CookingStatsManager: Unknown item {}", stat.item());
                    }
                }
            }
            OldSchoolLevels.LOGGER.info("Loaded {} custom cooking stats.", COOKING_STATS.size());
        } catch (Exception e) {
            OldSchoolLevels.LOGGER.error("Failed to load cooking_stats.json", e);
        }
    }

    private static void createDefaultFile(File configDir, File configFile) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        List<CookingStat> defaults = new ArrayList<>();
        // Add an example
        defaults.add(new CookingStat("farmersdelight:hamburger", 45, 120));

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(defaults, writer);
        } catch (IOException e) {
            OldSchoolLevels.LOGGER.error("Failed to create default cooking_stats.json", e);
        }
    }

    public static CookingStat getStat(Item item) {
        return COOKING_STATS.get(item);
    }

    public static Map<Item, CookingStat> getAllStats() {
        return COOKING_STATS;
    }
}
