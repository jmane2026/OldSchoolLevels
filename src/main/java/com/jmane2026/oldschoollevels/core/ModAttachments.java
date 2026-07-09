package com.jmane2026.oldschoollevels.core;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.CombatStyle;
import com.jmane2026.oldschoollevels.common.SkillData;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, OldSchoolLevels.MODID);

    public static final Supplier<AttachmentType<SkillData>> SKILLS = ATTACHMENT_TYPES.register(
            "skills",
            () -> AttachmentType.builder(() -> SkillData.EMPTY)
                    .serialize(SkillData.CODEC.fieldOf("skills"), data -> true)
                    .sync(SkillData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<CombatStyle>> COMBAT_STYLE = ATTACHMENT_TYPES.register(
            "combat_style",
            () -> AttachmentType.builder(() -> CombatStyle.ACCURATE)
                    .serialize(CombatStyle.CODEC.fieldOf("style"), data -> true)
                    .sync(CombatStyle.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> IS_CRITICAL = ATTACHMENT_TYPES.register(
            "is_critical",
            () -> AttachmentType.builder(() -> false).build()
    );
}