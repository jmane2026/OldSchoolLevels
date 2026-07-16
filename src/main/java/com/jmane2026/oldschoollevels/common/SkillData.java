package com.jmane2026.oldschoollevels.common;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.Map;

public record SkillData(Map<Skill, Long> skillExperience) {
    
    public static final SkillData EMPTY = new SkillData(new EnumMap<>(Skill.class));

    // Manual StreamCodec for Skill enum to avoid valueOf and xmap issues
    public static final StreamCodec<RegistryFriendlyByteBuf, Skill> SKILL_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NonNull Skill decode(RegistryFriendlyByteBuf buf) {
            return Skill.values()[buf.readVarInt()];
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Skill skill) {
            buf.writeVarInt(skill.ordinal());
        }
    };

    public static final Codec<SkillData> CODEC = Codec.unboundedMap(Codec.STRING, Codec.LONG)
            .xmap(map -> {
                Map<Skill, Long> experience = new EnumMap<>(Skill.class);
                map.forEach((key, value) -> {
                    try {
                        experience.put(Skill.valueOf(key), value);
                    } catch (IllegalArgumentException ignored) {}
                });
                return new SkillData(experience);
            }, skillData -> {
                Map<String, Long> map = new java.util.HashMap<>();
                skillData.skillExperience.forEach((key, value) -> map.put(key.name(), value));
                return map;
            });

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    _ -> new EnumMap<>(Skill.class),
                    SKILL_STREAM_CODEC,
                    ByteBufCodecs.VAR_LONG
            ),
            SkillData::skillExperience,
            SkillData::new
    );

    public long getExperience(Skill skill) {
        return skillExperience.getOrDefault(skill, skill.getDefaultXp());
    }

    public SkillData setExperience(Skill skill, long xp) {
        Map<Skill, Long> newMap = new EnumMap<>(Skill.class);
        newMap.putAll(this.skillExperience);
        newMap.put(skill, xp);
        return new SkillData(newMap);
    }

    public SkillData addExperience(Skill skill, long xp) {
        long currentXp = getExperience(skill);
        return setExperience(skill, currentXp + xp);
    }
}