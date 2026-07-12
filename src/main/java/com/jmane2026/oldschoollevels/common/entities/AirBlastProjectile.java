package com.jmane2026.oldschoollevels.common.entities;

import com.jmane2026.oldschoollevels.common.Spell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AirBlastProjectile extends AbstractWindCharge {
    private Spell spellType = Spell.AIR_BLAST;
    private float damage = 3.0f;

    public AirBlastProjectile(EntityType<? extends AirBlastProjectile> type, Level level) {
        super(type, level);
    }

    public AirBlastProjectile(EntityType<? extends AirBlastProjectile> type, LivingEntity shooter, Vec3 direction, Level level, float damage, Spell spell) {
        super(type, level, shooter, shooter.getX(), shooter.getEyeY(), shooter.getZ());
        this.damage = damage;
        this.spellType = spell;
        this.setDeltaMovement(direction.scale(1.5)); // Straight line speed
    }
    public Spell getSpellType() {
        return this.spellType;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity hit = result.getEntity();
            // Critical: Ensure getOwner() is used so LevelingHandler sees the Player as the attacker
            hit.hurtServer(serverLevel, this.damageSources().indirectMagic(this, this.getOwner()), this.damage);

            if (hit instanceof LivingEntity target) {
                if (spellType == Spell.FIRE_BLAST) {
                    target.igniteForTicks(100); // 5 seconds
                } else if (spellType == Spell.WATER_BLAST) {
                    // Reduce air supply to induce drowning
                    target.setAirSupply(Math.max(0, target.getAirSupply() - 150));
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    public boolean isNoGravity() { return true; }

    @Override
    protected void explode(Vec3 pos) {
        // We don't want the standard wind charge explosion/knockback,
        // so we leave this empty or implement a custom effect.
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }
}