package com.jmane2026.oldschoollevels.common;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public enum CombatStyle {
    ACCURATE("Accurate (Attack)", 0.5f, 0.5f, 1.0f),
    AGGRESSIVE("Aggressive (Strength)", 1.0f, 0.25f, 0.25f),
    DEFENSIVE("Defensive (Defense)", 0.25f, 1.0f, 0.5f),
    CONTROLLED("Controlled (Shared)", 0.75f, 0.75f, 0.75f);

    private final String description;
    private final float strengthScale;
    private final float defenseScale;
    private final float attackSpeedScale;

    CombatStyle(String description, float strengthScale, float defenseScale, float attackSpeedScale) {
        this.description = description;
        this.strengthScale = strengthScale;
        this.defenseScale = defenseScale;
        this.attackSpeedScale = attackSpeedScale;
    }

    public Component getName() { return Component.literal(description); }
    public float getStrengthScale() { return strengthScale; }
    public float getDefenseScale() { return defenseScale; }
    public float getAttackSpeedScale() { return attackSpeedScale; }

    public CombatStyle next() { return values()[(this.ordinal() + 1) % values().length]; }

    public static final Codec<CombatStyle> CODEC = Codec.INT.xmap(i -> values()[i], CombatStyle::ordinal);

    public static final StreamCodec<RegistryFriendlyByteBuf, CombatStyle> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CombatStyle decode(RegistryFriendlyByteBuf buf) {
            return values()[buf.readVarInt()];
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CombatStyle val) {
            buf.writeVarInt(val.ordinal());
        }
    };
}