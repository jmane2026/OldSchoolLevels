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

public class WoodcuttingStatsManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Block, WoodcuttingStat> WOODCUTTING_STATS = new HashMap<>();

    public static void load() {
        WOODCUTTING_STATS.clear();
        
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(OldSchoolLevels.MODID);
        File configFile = configDir.resolve("woodcutting_stats.json").toFile();

        if (!configFile.exists()) {
            createDefaultFile(configDir.toFile(), configFile);
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Type listType = new TypeToken<List<WoodcuttingStat>>() {}.getType();
            List<WoodcuttingStat> stats = GSON.fromJson(reader, listType);

            if (stats != null) {
                for (WoodcuttingStat stat : stats) {
                    Identifier blockId = Identifier.tryParse(stat.block());
                    if (blockId != null && BuiltInRegistries.BLOCK.containsKey(blockId)) {
                        Block block = BuiltInRegistries.BLOCK.getValue(blockId);
                        WOODCUTTING_STATS.put(block, stat);
                    } else {
                        OldSchoolLevels.LOGGER.warn("WoodcuttingStatsManager: Unknown block {}", stat.block());
                    }
                }
            }
            OldSchoolLevels.LOGGER.info("Loaded {} custom woodcutting stats.", WOODCUTTING_STATS.size());
        } catch (Exception e) {
            OldSchoolLevels.LOGGER.error("Failed to load woodcutting_stats.json", e);
        }
    }

    private static void createDefaultFile(File configDir, File configFile) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        List<WoodcuttingStat> defaults = new ArrayList<>();
        // Add an example
        defaults.add(new WoodcuttingStat("biomesoplenty:fir_log", 35, 60));

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(defaults, writer);
        } catch (IOException e) {
            OldSchoolLevels.LOGGER.error("Failed to create default woodcutting_stats.json", e);
        }
    }

    public static WoodcuttingStat getStat(Block block) {
        return WOODCUTTING_STATS.get(block);
    }

    public static Map<Block, WoodcuttingStat> getAllStats() {
        return WOODCUTTING_STATS;
    }
}
