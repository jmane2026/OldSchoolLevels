package com.jmane2026.oldschoollevels.core;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = 
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, OldSchoolLevels.MODID);

    // Maps Item ID (String) to Integer count for "infinite" storage logic
    public static final Supplier<DataComponentType<Map<String, Integer>>> SIGIL_STORAGE = COMPONENTS.register("sigil_storage", 
            () -> DataComponentType.<Map<String, Integer>>builder()
                    .persistent(Codec.unboundedMap(Codec.STRING, Codec.INT))
                    .networkSynchronized(ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT))
                    .build());
}