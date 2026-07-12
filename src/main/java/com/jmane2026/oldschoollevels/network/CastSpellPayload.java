package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.Spell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CastSpellPayload(Spell spell) implements CustomPacketPayload {
    public static final Type<CastSpellPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "cast_spell"));

    public static final StreamCodec<FriendlyByteBuf, CastSpellPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeEnum(payload.spell),
            buf -> new CastSpellPayload(buf.readEnum(Spell.class))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}