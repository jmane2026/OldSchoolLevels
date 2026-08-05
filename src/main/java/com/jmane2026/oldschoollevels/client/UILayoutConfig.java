package com.jmane2026.oldschoollevels.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import net.neoforged.fml.loading.FMLPaths;

public class UILayoutConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("oldschoollevels-ui.json").toFile();

    public int statsButtonX = 0;
    public int statsButtonY = 0;
    public int skillsButtonX = 0;
    public int skillsButtonY = 0;
    public int spellsButtonX = 0;
    public int spellsButtonY = 0;

    private static UILayoutConfig instance;

    public static UILayoutConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                instance = GSON.fromJson(reader, UILayoutConfig.class);
            } catch (IOException e) {
                LOGGER.error("Failed to load UI layout config", e);
                instance = new UILayoutConfig();
            }
        } else {
            instance = new UILayoutConfig();
            save();
        }
    }

    public static void save() {
        if (instance == null) return;
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save UI layout config", e);
        }
    }
}
