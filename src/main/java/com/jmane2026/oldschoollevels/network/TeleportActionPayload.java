package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.TeleportLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record TeleportActionPayload(String name, @Nullable TeleportLocation location, Action action, boolean isPortal) implements CustomPacketPayload {
    public static final Type<TeleportActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "teleport_action"));

    public static final StreamCodec<FriendlyByteBuf, TeleportActionPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeUtf(p.name);
                buf.writeBoolean(p.location != null);
                if (p.location != null) TeleportLocation.STREAM_CODEC.encode(buf, p.location);
                buf.writeEnum(p.action);
                buf.writeBoolean(p.isPortal);
            },
            buf -> new TeleportActionPayload(buf.readUtf(), buf.readBoolean() ? TeleportLocation.STREAM_CODEC.decode(buf) : null, buf.readEnum(Action.class), buf.readBoolean())
    );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public enum Action { ADD, DELETE, EXECUTE }
}