package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.Skill;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public record UnlockNotificationPayload(Skill skill, int level, String description, ItemStack icon) implements CustomPacketPayload {
    public static final Type<UnlockNotificationPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "unlock_notification"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockNotificationPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> { buf.writeEnum(p.skill); buf.writeInt(p.level); buf.writeUtf(p.description); ItemStack.STREAM_CODEC.encode(buf, p.icon); },
            buf -> new UnlockNotificationPayload(buf.readEnum(Skill.class), buf.readInt(), buf.readUtf(), ItemStack.STREAM_CODEC.decode(buf))
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}