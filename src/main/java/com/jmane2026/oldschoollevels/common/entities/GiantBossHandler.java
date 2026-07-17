package com.jmane2026.oldschoollevels.common.entities;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = OldSchoolLevels.MODID)
public class GiantBossHandler {
    private static final Map<UUID, ServerBossEvent> BOSS_BARS = new HashMap<>();

    // This method MUST be called from the Mod Bus (see OldSchoolLevels.java)
    public static void onAttributeModification(EntityAttributeModificationEvent event) {
        try {
            event.add(EntityType.GIANT, Attributes.MAX_HEALTH, 300.0);
            event.add(EntityType.GIANT, Attributes.ATTACK_DAMAGE, 12.0); // Reduced from 20
            event.add(EntityType.GIANT, Attributes.MOVEMENT_SPEED, 0.28); // Slightly slower
            event.add(EntityType.GIANT, Attributes.FOLLOW_RANGE, 40.0);
        } catch (Exception _) {

        }
    }

    @SubscribeEvent
    public static void onGiantJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Giant giant && !event.getLevel().isClientSide()) {
            ServerBossEvent bossBar = new ServerBossEvent(
                    UUID.randomUUID(),
                    giant.getDisplayName(),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.PROGRESS
            );
            BOSS_BARS.put(giant.getUUID(), bossBar);
        }
    }

    @SubscribeEvent
    public static void onGiantTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Giant giant && !giant.level().isClientSide()) {
            ServerBossEvent bossBar = BOSS_BARS.get(giant.getUUID());
            if (bossBar != null) {
                bossBar.setProgress(giant.getHealth() / giant.getMaxHealth());
                for (Player player : giant.level().players()) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        if (giant.distanceToSqr(serverPlayer) < 2500) bossBar.addPlayer(serverPlayer);
                        else bossBar.removePlayer(serverPlayer);
                    }
                }
            }

            // Simple AI
            if (giant.tickCount % 20 == 0 && giant.getTarget() == null) {
                Player nearest = giant.level().getNearestPlayer(giant, 30.0);
                if (nearest != null) giant.setTarget(nearest);
            }
            if (giant.getTarget() != null) {
                giant.getNavigation().moveTo(giant.getTarget(), 1.2);

                // Manual Attack Trigger: Giants have no attack goals, so we force the logic
                double attackReach = (giant.getBbWidth() * 2.0F * giant.getBbWidth() * 2.0F + giant.getTarget().getBbWidth());
                if (giant.distanceToSqr(giant.getTarget()) <= attackReach * attackReach && giant.tickCount % 20 == 0) {
                    giant.doHurtTarget((ServerLevel) giant.level(), giant.getTarget());
                    giant.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onGiantDeath(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Giant giant && !giant.isAlive()) {
            ServerBossEvent bar = BOSS_BARS.remove(giant.getUUID());
            if (bar != null) {
                bar.removeAllPlayers();
            }
        }
    }
}