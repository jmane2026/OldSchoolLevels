package com.jmane2026.oldschoollevels.common;

import net.neoforged.neoforge.common.ModConfigSpec;

public class OSLConfig {
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    // Common Settings
    public static final ModConfigSpec.BooleanValue ENABLE_JUMP_SCALING;
    public static final ModConfigSpec.BooleanValue ENABLE_WALL_JUMPING;
    public static final ModConfigSpec.BooleanValue ENABLE_WATER_STRIDING;
    public static final ModConfigSpec.BooleanValue ENABLE_SWIM_SPEED_SCALING;
    public static final ModConfigSpec.BooleanValue ENABLE_MOVEMENT_SPEED_SCALING;
    public static final ModConfigSpec.BooleanValue ENABLE_MINING_SPEED_SCALING;
    public static final ModConfigSpec.BooleanValue ENABLE_WOODCUTTING_SPEED_SCALING;

    public static final ModConfigSpec.BooleanValue ENABLE_CLOUD_SYNC;
    public static final ModConfigSpec.ConfigValue<String> SUPABASE_URL;
    public static final ModConfigSpec.ConfigValue<String> SUPABASE_API_KEY;

    // Client Settings
    public static final ModConfigSpec.BooleanValue ENABLE_ENTITY_HEALTH_BARS;

    static {
        COMMON_BUILDER.translation("oldschoollevels.config.category.mobility").push("Mobility Features");
        ENABLE_MOVEMENT_SPEED_SCALING = COMMON_BUILDER.comment("Toggle passive movement speed scaling from Mobility level.")
                .translation("oldschoollevels.config.enableMovementSpeedScaling")
                .define("enableMovementSpeedScaling", true);
        ENABLE_JUMP_SCALING = COMMON_BUILDER.comment("Toggle jump height scaling from Mobility level.")
                .translation("oldschoollevels.config.enableJumpScaling")
                .define("enableJumpScaling", true);
        ENABLE_WALL_JUMPING = COMMON_BUILDER.comment("Toggle the ability to wall jump at level 60.")
                .translation("oldschoollevels.config.enableWallJumping")
                .define("enableWallJumping", true);
        ENABLE_WATER_STRIDING = COMMON_BUILDER.comment("Toggle the ability to water skip at level 80.")
                .translation("oldschoollevels.config.enableWaterStriding")
                .define("enableWaterStriding", true);
        ENABLE_SWIM_SPEED_SCALING = COMMON_BUILDER.comment("Toggle passive swim speed scaling from Mobility level.")
                .translation("oldschoollevels.config.enableSwimSpeedScaling")
                .define("enableSwimSpeedScaling", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.translation("oldschoollevels.config.category.gathering").push("Gathering Features");
        ENABLE_MINING_SPEED_SCALING = COMMON_BUILDER.comment("Toggle break speed scaling for Mining.")
                .translation("oldschoollevels.config.enableMiningSpeedScaling")
                .define("enableMiningSpeedScaling", true);
        ENABLE_WOODCUTTING_SPEED_SCALING = COMMON_BUILDER.comment("Toggle break speed scaling for Woodcutting.")
                .translation("oldschoollevels.config.enableWoodcuttingSpeedScaling")
                .define("enableWoodcuttingSpeedScaling", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.translation("oldschoollevels.config.category.cloudSync").push("Cloud Sync");
        ENABLE_CLOUD_SYNC = COMMON_BUILDER.comment("Enable syncing player stats to a Supabase database.")
                .translation("oldschoollevels.config.enableCloudSync")
                .define("enableCloudSync", false);
        SUPABASE_URL = COMMON_BUILDER.comment("The REST URL of the Supabase project.")
                .translation("oldschoollevels.config.supabaseUrl")
                .define("supabaseUrl", "https://gbimvviaawgvsxqzyoxr.supabase.co/rest/v1/players");
        SUPABASE_API_KEY = COMMON_BUILDER.comment("The API Key for Supabase authentication.")
                .translation("oldschoollevels.config.supabaseApiKey")
                .define("supabaseApiKey", "sb_publishable_XqNh6YwQsMEBQkiI8-FUuw_3_xAZwo9");
        COMMON_BUILDER.pop();

        CLIENT_BUILDER.translation("oldschoollevels.config.category.visuals").push("Visuals");
        ENABLE_ENTITY_HEALTH_BARS = CLIENT_BUILDER.comment("Toggle the OSRS-style health bar overlay when targeting mobs.")
                .translation("oldschoollevels.config.enableEntityHealthBars")
                .define("enableEntityHealthBars", true);
        CLIENT_BUILDER.pop();

        COMMON_SPEC = COMMON_BUILDER.build();
        CLIENT_SPEC = CLIENT_BUILDER.build();
    }
}