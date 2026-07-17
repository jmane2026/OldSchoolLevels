package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record WarningPayload(String message) implements CustomPacketPayload {
    public static final Type<WarningPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "warning_message"));

    public static final StreamCodec<FriendlyByteBuf, WarningPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, WarningPayload::message,
            WarningPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}