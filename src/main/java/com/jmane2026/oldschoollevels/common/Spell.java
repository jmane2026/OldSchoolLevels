package com.jmane2026.oldschoollevels.common;

import com.jmane2026.oldschoollevels.core.ModItems;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Supplier;

public enum Spell {
    AIR_BLAST("Air Blast", 1, List.of(new SpellCost(ModItems.AIR_SIGIL, 1)), "air_blast", 5.0f, 0),
    BLINK("Blink", 10, List.of(new SpellCost(ModItems.AIR_SIGIL, 1), new SpellCost(ModItems.LOGIC_SIGIL, 1)), "blink", 0.0f, 20),
    TRANSMUTE_COPPER("Transmute Copper", 15, List.of(new SpellCost(ModItems.FIRE_SIGIL, 1), new SpellCost(ModItems.LOGIC_SIGIL, 1)), "transmute_copper_to_iron", 0.0f, 20),
    SPAWN_TELEPORT("Spawn Teleport", 20, List.of(new SpellCost(ModItems.AIR_SIGIL, 2), new SpellCost(ModItems.LOGIC_SIGIL, 1)), "spawn_teleport", 0.0f, 40),
    STRENGTH("Strength", 25, List.of(new SpellCost(ModItems.EARTH_SIGIL, 1), new SpellCost(ModItems.LOGIC_SIGIL, 1)), "strength", 0.0f, 35),
    TRANSMUTE_IRON("Transmute Iron", 30, List.of(new SpellCost(ModItems.FIRE_SIGIL, 1), new SpellCost(ModItems.LOGIC_SIGIL, 1)), "transmute_iron_to_gold", 0.0f, 45),
    WATER_BLAST("Water Blast", 40, List.of(new SpellCost(ModItems.WATER_SIGIL, 1), new SpellCost(ModItems.AIR_SIGIL, 1)), "water_blast", 9.0f, 0),
    TELEPORT("Teleport", 50, List.of(new SpellCost(ModItems.WATER_SIGIL, 2), new SpellCost(ModItems.AIR_SIGIL, 1)), "teleport", 0.0f, 55),
    TELEKINESIS("Telekinesis", 55, List.of(new SpellCost(ModItems.EARTH_SIGIL, 1), new SpellCost(ModItems.LOGIC_SIGIL, 1)), "telekinesis", 0.0f, 60),
    TRANSMUTE_DIAMOND("Transmute Diamond", 70, List.of(new SpellCost(ModItems.FIRE_SIGIL, 1), new SpellCost(ModItems.LOGIC_SIGIL, 2)), "transmute_diamond_to_emerald", 0.0f, 100),
    FIRE_BLAST("Fire Blast", 80, List.of(new SpellCost(ModItems.FIRE_SIGIL, 1), new SpellCost(ModItems.AIR_SIGIL, 1)), "fire_blast", 14.0f, 0),
    PORTAL("Portal", 90, List.of(new SpellCost(ModItems.LOGIC_SIGIL, 2), new SpellCost(ModItems.WATER_SIGIL, 1)), "portal", 0.0f, 120);


    // Changed to flat Codec to match ModAttachments pattern
    public static final Codec<Spell> CODEC = Codec.STRING.xmap(Spell::valueOf, Spell::name);
    public static final StreamCodec<FriendlyByteBuf, Spell> STREAM_CODEC = StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(Spell.class));

    private final String displayName;
    private final int requiredMagicLevel;
    private final List<SpellCost> costs;
    private final Identifier icon;
    private final float baseDamage;
    private final int baseXp;

    Spell(String displayName, int requiredMagicLevel, List<SpellCost> costs, String iconName, float baseDamage, int baseXp) {
        this.displayName = displayName;
        this.requiredMagicLevel = requiredMagicLevel;
        this.costs = costs;
        this.icon = Identifier.fromNamespaceAndPath("oldschoollevels", "textures/gui/spells/" + iconName + ".png");
        this.baseDamage = baseDamage;
        this.baseXp = baseXp;
    }

    public String getDisplayName() { return displayName; }
    public int getRequiredMagicLevel() { return requiredMagicLevel; }
    public List<SpellCost> getCosts() { return costs; }
    public Component getNameComponent() { return Component.literal(displayName); }
    public Identifier getIconTexture() { return icon; }
    public float getBaseDamage() { return baseDamage; }
    public int getBaseXp() { return baseXp; }

    public record SpellCost(Supplier<Item> item, int amount) {}
}