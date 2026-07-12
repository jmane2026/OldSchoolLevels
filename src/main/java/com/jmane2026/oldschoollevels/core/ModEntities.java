package com.jmane2026.oldschoollevels.core;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.entities.AirBlastProjectile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, OldSchoolLevels.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<AirBlastProjectile>> AIR_BLAST_PROJECTILE =
            ENTITY_TYPES.register("air_blast_projectile",
                    () -> EntityType.Builder.<AirBlastProjectile>of(AirBlastProjectile::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F) // Hitbox size
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    Identifier.fromNamespaceAndPath(OldSchoolLevels.MODID, "air_blast_projectile"))));
}