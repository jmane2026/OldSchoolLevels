package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DamageNumberPayload(double x, double y, double z, float amount) implements CustomPacketPayload {
    public static final Type<DamageNumberPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "damage_number"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DamageNumberPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, DamageNumberPayload::x,
            ByteBufCodecs.DOUBLE, DamageNumberPayload::y,
            ByteBufCodecs.DOUBLE, DamageNumberPayload::z,
            ByteBufCodecs.FLOAT, DamageNumberPayload::amount,
            DamageNumberPayload::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}