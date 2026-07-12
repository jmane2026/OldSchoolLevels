package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public record AdjustPouchPayload(String sigilId) implements CustomPacketPayload {
    public static final Type<AdjustPouchPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "adjust_pouch"));
    public static final StreamCodec<FriendlyByteBuf, AdjustPouchPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeUtf(p.sigilId),
            buf -> new AdjustPouchPayload(buf.readUtf())
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}