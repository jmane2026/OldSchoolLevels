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

    // Client Settings
    public static final ModConfigSpec.BooleanValue ENABLE_ENTITY_HEALTH_BARS;

    static {
        COMMON_BUILDER.push("Mobility Features");
        ENABLE_MOVEMENT_SPEED_SCALING = COMMON_BUILDER.comment("Toggle passive movement speed scaling from Mobility level.")
                .define("enableMovementSpeedScaling", true);
        ENABLE_JUMP_SCALING = COMMON_BUILDER.comment("Toggle jump height scaling from Mobility level.")
                .define("enableJumpScaling", true);
        ENABLE_WALL_JUMPING = COMMON_BUILDER.comment("Toggle the ability to wall jump at level 60.")
                .define("enableWallJumping", true);
        ENABLE_WATER_STRIDING = COMMON_BUILDER.comment("Toggle the ability to water skip at level 80.")
                .define("enableWaterStriding", true);
        ENABLE_SWIM_SPEED_SCALING = COMMON_BUILDER.comment("Toggle passive swim speed scaling from Mobility level.")
                .define("enableSwimSpeedScaling", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Gathering Features");
        ENABLE_MINING_SPEED_SCALING = COMMON_BUILDER.comment("Toggle break speed scaling for Mining.")
                .define("enableMiningSpeedScaling", true);
        ENABLE_WOODCUTTING_SPEED_SCALING = COMMON_BUILDER.comment("Toggle break speed scaling for Woodcutting.")
                .define("enableWoodcuttingSpeedScaling", true);
        COMMON_BUILDER.pop();

        CLIENT_BUILDER.push("Visuals");
        ENABLE_ENTITY_HEALTH_BARS = CLIENT_BUILDER.comment("Toggle the OSRS-style health bar overlay when targeting mobs.")
                .define("enableEntityHealthBars", true);
        CLIENT_BUILDER.pop();

        COMMON_SPEC = COMMON_BUILDER.build();
        CLIENT_SPEC = CLIENT_BUILDER.build();
    }
}