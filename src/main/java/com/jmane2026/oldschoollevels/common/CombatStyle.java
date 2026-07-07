package com.jmane2026.oldschoollevels.common;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

public enum CombatStyle {
    ACCURATE("Accurate (Attack)", 0.5f),
    AGGRESSIVE("Aggressive (Strength)", 1.0f),
    DEFENSIVE("Defensive (Defense)", 0.25f),
    CONTROLLED("Controlled (Shared)", 0.75f);

    private final String description;
    private final float strengthScale;

    CombatStyle(String description, float strengthScale) {
        this.description = description;
        this.strengthScale = strengthScale;
    }

    public Component getName() { return Component.literal(description); }
    public float getStrengthScale() { return strengthScale; }
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