package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.CombatStyle;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ChangeStylePayload(CombatStyle style) implements CustomPacketPayload {
    public static final Type<ChangeStylePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "change_style"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeStylePayload> STREAM_CODEC = StreamCodec.composite(
            CombatStyle.STREAM_CODEC, ChangeStylePayload::style,
            ChangeStylePayload::new
    );
    @Override public @NonNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}