package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.Skill;
import com.jmane2026.oldschoollevels.common.SkillData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record XpGainPayload(Skill skill, long amount, long totalXp) implements CustomPacketPayload {
    public static final Type<XpGainPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "xp_gain"));

    public static final StreamCodec<RegistryFriendlyByteBuf, XpGainPayload> STREAM_CODEC = StreamCodec.composite(
            SkillData.SKILL_STREAM_CODEC,
            XpGainPayload::skill,
            ByteBufCodecs.VAR_LONG, XpGainPayload::amount,
            ByteBufCodecs.VAR_LONG, XpGainPayload::totalXp,
            XpGainPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}