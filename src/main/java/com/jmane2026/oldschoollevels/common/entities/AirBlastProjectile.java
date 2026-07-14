package com.jmane2026.oldschoollevels.common.entities;

import com.jmane2026.oldschoollevels.common.Spell;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AirBlastProjectile extends AbstractWindCharge {
    private Spell spellType = Spell.AIR_BLAST;
    private float damage = 5.0f;

    public AirBlastProjectile(EntityType<? extends AirBlastProjectile> type, Level level) {
        super(type, level);
    }

    public AirBlastProjectile(EntityType<? extends AirBlastProjectile> type, LivingEntity shooter, Vec3 direction, Level level, float damage, Spell spell) {
        super(type, level, shooter, shooter.getX(), shooter.getEyeY(), shooter.getZ());
        this.damage = damage;
        this.spellType = spell;
        this.setDeltaMovement(direction.scale(1.5)); // Straight line speed
        this.hurtMarked = true; // Ensures immediate client-side sync of the custom velocity
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
                }   // 1. Air Blast: Apply Knockback
                else if (this.spellType == Spell.AIR_BLAST) {
                    Vec3 movement = this.getDeltaMovement();
                    target.knockback(1.2f, -movement.x, -movement.z);
                }
                // 2. Water Blast: Extinguish + Poison (Waterlog)
                else if (this.spellType == Spell.WATER_BLAST) {
                    target.extinguishFire();
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 1));
                    // Visual feedback for extinguishing
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, 
                            target.getX(), target.getY() + 1, target.getZ(), 
                            5, 0.2, 0.2, 0.2, 0.05);
                            
                    this.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide()) return;

        BlockPos hitPos = result.getBlockPos();
        BlockPos firePos = hitPos.relative(result.getDirection());

        // 3. Fire Blast: Ignite ground/blocks
        if (this.spellType == Spell.FIRE_BLAST) {
            if (this.level().getBlockState(firePos).isAir()) {
                this.level().setBlockAndUpdate(firePos, BaseFireBlock.getState(this.level(), firePos));
                this.level().playSound(null, firePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
        // 4. Water Blast: Extinguish Block Fire
        else if (this.spellType == Spell.WATER_BLAST) {
            if (this.level().getBlockState(firePos).is(BlockTags.FIRE)) {
                this.level().removeBlock(firePos, false);
                this.level().playSound(null, firePos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }

        this.discard();
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