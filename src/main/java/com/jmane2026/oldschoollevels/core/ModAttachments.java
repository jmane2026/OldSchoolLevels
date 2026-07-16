package com.jmane2026.oldschoollevels.core;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.CombatStyle;
import com.jmane2026.oldschoollevels.common.SkillData;
import com.jmane2026.oldschoollevels.common.Spell;
import com.jmane2026.oldschoollevels.common.TeleportLocation;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, OldSchoolLevels.MODID);

    public static final Supplier<AttachmentType<SkillData>> SKILLS = ATTACHMENT_TYPES.register(
            "skills",
            () -> AttachmentType.builder(() -> SkillData.EMPTY)
                    .serialize(SkillData.CODEC.fieldOf("skills"), _ -> true)
                    .sync(SkillData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<CombatStyle>> COMBAT_STYLE = ATTACHMENT_TYPES.register(
            "combat_style",
            () -> AttachmentType.builder(() -> CombatStyle.ACCURATE)
                    .serialize(CombatStyle.CODEC.fieldOf("style"), _ -> true)
                    .sync(CombatStyle.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> IS_CRITICAL = ATTACHMENT_TYPES.register(
            "is_critical",
            () -> AttachmentType.builder(() -> false).build()
    );

    public static final Supplier<AttachmentType<BlockPos>> ECHO_TARGET = ATTACHMENT_TYPES.register(
            "echo_target",
            () -> AttachmentType.builder(() -> BlockPos.ZERO)
                    .serialize(BlockPos.CODEC.fieldOf("pos")) // Fixed: Satisfies copyOnDeath requirement
                    .sync(BlockPos.STREAM_CODEC)             // Fixed: Enables syncing to the HUD
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<Spell>> ACTIVE_SPELL = ATTACHMENT_TYPES.register(
            "active_spell",
            () -> AttachmentType.builder(() -> Spell.AIR_BLAST)
                    .serialize(Spell.CODEC.fieldOf("active_spell"), _ -> true)
                    .sync(Spell.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<List<TeleportLocation>>> TELEPORT_LOCATIONS = ATTACHMENT_TYPES.register(
            "teleport_locations",
            () -> AttachmentType.builder(() -> (List<TeleportLocation>) new ArrayList<TeleportLocation>())
                    .serialize(TeleportLocation.CODEC.listOf().fieldOf("locations"))
                    .sync(ByteBufCodecs.collection(ArrayList::new, TeleportLocation.STREAM_CODEC))
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<Float>> STAMINA = ATTACHMENT_TYPES.register(
            "stamina",
            () -> AttachmentType.builder(() -> 100.0f)
                    .serialize(Codec.FLOAT.fieldOf("value"))
                    .sync(ByteBufCodecs.FLOAT)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<Vec3>> LAST_POS = ATTACHMENT_TYPES.register(
            "last_pos",
            () -> AttachmentType.builder(() -> Vec3.ZERO)
                    .build());
}