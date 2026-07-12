package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.Spell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SelectSpellPayload(Spell spell) implements CustomPacketPayload {
    public static final Type<SelectSpellPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "select_spell"));

    public static final StreamCodec<FriendlyByteBuf, SelectSpellPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> buf.writeEnum(p.spell),
            buf -> new SelectSpellPayload(buf.readEnum(Spell.class))
    );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}