package com.jmane2026.oldschoollevels.common;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.client.gui.TeleportScreen;
import com.jmane2026.oldschoollevels.client.gui.WarningOverlay;
import com.jmane2026.oldschoollevels.common.blocks.entity.MagicPortalBlockEntity;
import com.jmane2026.oldschoollevels.common.entities.AirBlastProjectile;
import com.jmane2026.oldschoollevels.common.items.SigilPouchItem;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import com.jmane2026.oldschoollevels.core.ModBlocks;
import com.jmane2026.oldschoollevels.core.ModEntities;
import com.jmane2026.oldschoollevels.network.CastSpellPayload;
import com.jmane2026.oldschoollevels.util.ExperienceUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.EventHooks;

import java.util.*;

import static net.minecraft.core.registries.Registries.ENCHANTMENT;

@EventBusSubscriber(modid = OldSchoolLevels.MODID, value = Dist.CLIENT)
public class MagicHandler {
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    private static ItemStack findPouch(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof SigilPouchItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static boolean isMiddleMouseDown = false;

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (event.getButton() == 2) {
            isMiddleMouseDown = event.getAction() == 1; // 1 is Press, 0 is Release
            if (isMiddleMouseDown) {
                assert Minecraft.getInstance().player != null;
                Spell active = Minecraft.getInstance().player.getData(ModAttachments.ACTIVE_SPELL.get());

                // Open Teleport/Portal screen directly on client if selected
                if (active == Spell.TELEPORT || active == Spell.PORTAL) {
                    Minecraft.getInstance().setScreen(new TeleportScreen(active == Spell.PORTAL));
                    return;
                }

                ClientPacketDistributor.sendToServer(new CastSpellPayload(active));
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        // Continuous casting for Transmutation (runs every 1 second while held)
        if (isMiddleMouseDown && Minecraft.getInstance().player != null && Minecraft.getInstance().player.tickCount % 20 == 0) {
            Spell active = Minecraft.getInstance().player.getData(ModAttachments.ACTIVE_SPELL.get());
            if (active.name().startsWith("TRANSMUTE") || active == Spell.STRENGTH) {
                ClientPacketDistributor.sendToServer(new CastSpellPayload(active));
            }
        }
    }

    public static void castSpell(ServerPlayer player, Spell spell) {
        SkillData data = player.getData(ModAttachments.SKILLS.get());
        int magicLevel = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MAGIC));

        if (magicLevel < spell.getRequiredMagicLevel()) {
            WarningOverlay.showWarning("Requires Level " + spell.getRequiredMagicLevel() + " Magic");
            return;
        }

        long currentTime = player.level().getGameTime();
        if (COOLDOWNS.getOrDefault(player.getUUID(), 0L) > currentTime) return;

        ItemStack pouch = findPouch(player);

        // 1. Check all costs
        for (Spell.SpellCost cost : spell.getCosts()) {
            int inPouch = pouch.isEmpty() ? 0 : SigilPouchItem.getSigilCount(pouch, cost.item().get());
            int inInv = player.getInventory().countItem(cost.item().get());
            
            if (inPouch + inInv < cost.amount()) {
                 WarningOverlay.showWarning("Missing: " + Component.translatable(cost.item().get().getDescriptionId()).getString());
                 return;
            }
        }

        boolean success = false;
        switch (spell) {
            case AIR_BLAST, FIRE_BLAST, WATER_BLAST -> {
                AirBlastProjectile proj = new AirBlastProjectile(ModEntities.AIR_BLAST_PROJECTILE.get(),
                        player, player.getLookAngle(), player.level(), spell.getBaseDamage(), spell);
                proj.setPos(player.getX(), player.getEyeY(), player.getZ());
                player.level().addFreshEntity(proj);
                SoundEvent sound = spell == Spell.FIRE_BLAST ? SoundEvents.BLAZE_SHOOT : SoundEvents.ILLUSIONER_CAST_SPELL;
                player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 1.0f, 1.2f);
                success = true;
            }
            case BLINK -> {
                // Safe Teleport: Raytrace to find destination
                Vec3 start = player.getEyePosition();
                Vec3 end = start.add(player.getLookAngle().scale(8));
                HitResult hit = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                Vec3 dest = hit.getLocation();

                // Back up slightly if we hit a block to avoid getting stuck
                if (hit.getType() == HitResult.Type.BLOCK) {
                    dest = dest.subtract(player.getLookAngle().normalize().scale(0.5));
                }
                
                player.teleportTo(dest.x, dest.y, dest.z);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.5f);
                success = true;
            }
            case STRENGTH -> {
                // Stack duration if already active
                MobEffectInstance current = player.getEffect(MobEffects.STRENGTH);
                int extraTicks = 600;
                if (current != null) extraTicks += current.getDuration();
                
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, Math.min(extraTicks, 12000), 0));
                player.level().playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.PLAYERS, 1.0f, 0.8f);
                success = true;
            }
            case SPAWN_TELEPORT -> {
                BlockPos spawn;
                ServerPlayer.RespawnConfig config = player.getRespawnConfig();
                if (config != null) {
                    spawn = config.respawnData().pos();
                } else {
                    spawn = player.level().getRespawnData().pos();
                }
                player.teleportTo(spawn.getX(), spawn.getY(), spawn.getZ());
                player.level().playSound(null, player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.5f);
                success = true;
            }
            case TELEKINESIS -> {
                double range = 256.0;
                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getLookAngle();
                Vec3 endPos = eyePos.add(lookVec.scale(range));

                // 1. Check for Blocks
                BlockHitResult blockHit = player.level().clip(new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

                // 2. Check for Entities (specifically ItemEntities)
                AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(1.0);
                EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eyePos, endPos, searchBox, (e) -> e instanceof ItemEntity || e instanceof Zombie, range * range);

                // Determine closest target
                boolean hitEntity = entityHit != null && entityHit.getLocation().distanceTo(eyePos) < blockHit.getLocation().distanceTo(eyePos);

                if (hitEntity) {
                    Entity target = entityHit.getEntity();
                    if (target instanceof ItemEntity item) {
                    item.setPos(player.getX(), player.getY(), player.getZ());
                    item.setPickUpDelay(0);
                    player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                    success = true;
                    } else if (target instanceof Zombie zombie && zombie.level() instanceof ServerLevel serverLevel) {
                        // Ritual: Transform Zombie into Giant
                        Giant giant = EntityType.GIANT.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
                        
                        if (giant != null) {
                            // Use snapTo for precise instant placement before adding to level
                            giant.snapTo(zombie.getX(), zombie.getY() + 0.5, zombie.getZ(), zombie.getYRot(), zombie.getXRot());
                            giant.setYHeadRot(zombie.getYHeadRot());
                            // Initialize AI and stats properly
                            EventHooks.finalizeMobSpawn(giant, serverLevel, serverLevel.getCurrentDifficultyAt(giant.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
                            giant.setHealth(giant.getMaxHealth()); // Ensure health is full before spawning

                            if (serverLevel.addFreshEntity(giant)) {
                                zombie.discard();
                                serverLevel.playSound(null, giant.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.0f, 0.5f);
                                success = true;
                            }
                        }
                    }
                } else if (blockHit.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = blockHit.getBlockPos();
                    BlockState state = player.level().getBlockState(pos);

                    if (!state.isAir() && state.getDestroySpeed(player.level(), pos) >= 0) {
                        // Level Requirements Check
                        int miningLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.MINING));
                        int wcLvl = ExperienceUtils.getLevelAtExperience(data.getExperience(Skill.WOODCUTTING));
                        int reqMining = RequirementUtils.getRequiredMiningLevel(state.getBlock());
                        int reqWc = RequirementUtils.getRequiredWoodcuttingLevel(state.getBlock());

                        if (miningLvl < reqMining || wcLvl < reqWc) {
                            WarningOverlay.showWarning("Your Skill levels are too low to move this block");
                            return;
                        }

                        // Create a "Silk Touch" fake tool to calculate drops
                        ItemStack silkTool = new ItemStack(Items.STICK);
                        var enchantmentRegistry = player.level().registryAccess().lookupOrThrow(ENCHANTMENT);
                        silkTool.enchant(enchantmentRegistry.getOrThrow(Enchantments.SILK_TOUCH), 1);

                        // Simulate breaking and collect drops
                        List<ItemStack> drops = Block.getDrops(state, player.level(), pos, player.level().getBlockEntity(pos), player, silkTool);
                        player.level().destroyBlock(pos, false);
                        for (ItemStack drop : drops) {
                            if (!player.getInventory().add(drop)) {
                                player.drop(drop, false);
                            }
                        }
                        player.level().playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0f, 1.5f);
                        success = true;
                    }
                }
            }
            case TRANSMUTE_COPPER -> success = transmute(player, Items.COPPER_INGOT, Items.IRON_INGOT);
            case TRANSMUTE_IRON -> success = transmute(player, Items.IRON_INGOT, Items.GOLD_INGOT);
            case TRANSMUTE_DIAMOND -> success = transmute(player, Items.DIAMOND, Items.EMERALD);
        }

        if (success) {
            consumeCosts(player, spell, pouch);
            player.swing(InteractionHand.MAIN_HAND, true);
            COOLDOWNS.put(player.getUUID(), currentTime + 20);

            // Award XP based on the spell's defined baseXp (only for utility spells, Blasts are handled in onCombat)
            if (!spell.name().endsWith("_BLAST")) {
                LevelingHandler.awardXp(player, Skill.MAGIC, spell.getBaseXp());
            }
        }
    }

    public static void handleTeleportRequest(ServerPlayer player, TeleportLocation loc, boolean isPortal) {
        Spell spell = isPortal ? Spell.PORTAL : Spell.TELEPORT;

        ItemStack pouch = findPouch(player);

        // Check requirements again on server
        for (Spell.SpellCost cost : spell.getCosts()) {
            int inPouch = pouch.isEmpty() ? 0 : SigilPouchItem.getSigilCount(pouch, cost.item().get());
            int inInv = player.getInventory().countItem(cost.item().get());
            if (inPouch + inInv < cost.amount()) {
                return;
            }
        }

        if (isPortal) {
            BlockPos pos = player.blockPosition().relative(player.getDirection());
            BlockPos topPos = pos.above();
            // Ensure space is clear before spawning
            if (!player.level().getBlockState(pos).canBeReplaced() || !player.level().getBlockState(topPos).canBeReplaced()) {
                WarningOverlay.showWarning("Not enough space to create a Portal");
                return;
            }

            // Flag 2 prevents neighbor updates, preventing the portal from breaking itself during placement
            player.level().setBlock(pos, ModBlocks.MAGIC_PORTAL.get().defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), 2);
            player.level().setBlock(topPos, ModBlocks.MAGIC_PORTAL.get().defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), 3);
            
            if (player.level().getBlockEntity(pos) instanceof MagicPortalBlockEntity be) {
                be.setDestination(loc);
            }
            player.level().playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            ServerLevel destLevel = player.level().getServer().getLevel(loc.dimension());
            if (destLevel != null) {
                // Added +0.1 to Y to ensure the player isn't stuck in the floor
                player.teleportTo(destLevel, loc.pos().getX() + 0.5, loc.pos().getY() + 0.1, loc.pos().getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true);
                player.level().playSound(null, player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }

        // 3. Consume Costs (Pouch Priority)
        consumeCosts(player, spell, pouch);
        LevelingHandler.awardXp(player, Skill.MAGIC, spell.getBaseXp());
    }

    private static void consumeCosts(ServerPlayer player, Spell spell, ItemStack pouch) {
        for (Spell.SpellCost cost : spell.getCosts()) {
            int needed = cost.amount();
            if (!pouch.isEmpty()) {
                int fromPouch = Math.min(needed, SigilPouchItem.getSigilCount(pouch, cost.item().get()));
                if (fromPouch > 0) {
                    SigilPouchItem.consumeSigils(pouch, cost.item().get(), fromPouch);
                    needed -= fromPouch;
                }
            }
            if (needed > 0) {
                player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == cost.item().get(), needed, player.inventoryMenu.getCraftSlots());
            }
        }
    }

    public static void addLocation(ServerPlayer player, String name) {
        List<TeleportLocation> locations = new ArrayList<>(player.getData(ModAttachments.TELEPORT_LOCATIONS.get()));
        locations.add(new TeleportLocation(name, player.blockPosition(), player.level().dimension()));
        player.setData(ModAttachments.TELEPORT_LOCATIONS.get(), locations);
        // Critical: Sync the new list to the client so the UI can see it
        player.syncData(ModAttachments.TELEPORT_LOCATIONS.get());
    }

    public static void deleteLocation(ServerPlayer player, TeleportLocation loc) {
        List<TeleportLocation> locations = new ArrayList<>(player.getData(ModAttachments.TELEPORT_LOCATIONS.get()));
        locations.removeIf(l -> l.name().equals(loc.name()) && l.pos().equals(loc.pos()));
        player.setData(ModAttachments.TELEPORT_LOCATIONS.get(), locations);
        // Sync the removal
        player.syncData(ModAttachments.TELEPORT_LOCATIONS.get());
    }

    private static boolean transmute(ServerPlayer player, Item from, Item to) {
        if (player.getInventory().contains(new ItemStack(from))) {
            player.getInventory().clearOrCountMatchingItems(p -> p.getItem() == from, 1, player.inventoryMenu.getCraftSlots());
            player.getInventory().add(new ItemStack(to));
            player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_BREAK, SoundSource.PLAYERS, 1.0f, 1.5f);
            return true;
        }
        // Use description ID to ensure the item name renders correctly
        WarningOverlay.showWarning("Missing: " + Component.translatable(from.getDescriptionId()).getString());
        return false;
    }

    public static void renderActiveSpell(GuiGraphicsExtractor graphics, DeltaTracker ignoredDelta) {
        Minecraft mc = Minecraft.getInstance();
        // Respect F1 (hideGui) and keep visible even if a screen is open
        if (mc.options.hideGui || mc.player == null || mc.level == null) return;

        Spell active = mc.player.getData(ModAttachments.ACTIVE_SPELL.get());
        int x = mc.getWindow().getGuiScaledWidth() / 2 - 110;
        int y = mc.getWindow().getGuiScaledHeight() - 19;

        graphics.fill(x - 2, y - 2, x + 18, y + 18, 0x88000000);
        graphics.outline(x - 2, y - 2, 20, 20, 0xFF373737);

        // Render Custom PNG Icon
        graphics.blit(RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, active.getIconTexture(), x, y, 0.0f, 0.0f, 16, 16, 32, 32, 32, 32, -1);

        long cooldownEnd = COOLDOWNS.getOrDefault(mc.player.getUUID(), 0L);
        long currentTime = mc.level.getGameTime();
        if (cooldownEnd > currentTime) {
            float progress = (cooldownEnd - currentTime) / 20.0f;
            int height = (int) (progress * 16);
            graphics.fill(x, y + 16 - height, x + 16, y + 16, 0x88FFFFFF);
        }

        // Calculate remaining casts based on current inventory sigil counts
        ItemStack pouch = ItemStack.EMPTY;
        if (mc.player == null) return;
        for (ItemStack s : mc.player.getInventory().getNonEquipmentItems()) if (s.getItem() instanceof SigilPouchItem) pouch = s;

        int minCasts = Integer.MAX_VALUE;
        for (Spell.SpellCost cost : active.getCosts()) {
            int owned = mc.player.getInventory().countItem(cost.item().get()) + (pouch.isEmpty() ? 0 : SigilPouchItem.getSigilCount(pouch, cost.item().get()));
            minCasts = Math.min(minCasts, owned / cost.amount());
        }
        if (minCasts == Integer.MAX_VALUE) minCasts = 0;

        String castStr;
        int color = 0xFFFFFFFF; // Default White

        if (minCasts > 99) {
            castStr = "*";
        } else {
            castStr = String.valueOf(minCasts);
            if (minCasts <= 0) color = 0xFFFF5555;
        }

        // Render the cast count string in the bottom-right of the icon area
        int textX = x + 16 - mc.font.width(castStr);
        int textY = y + 8; // Shifted up to prevent cutoff

        // Render 4-way black outline for better contrast
        graphics.text(mc.font, castStr, textX + 1, textY, 0xFF000000);
        graphics.text(mc.font, castStr, textX - 1, textY, 0xFF000000);
        graphics.text(mc.font, castStr, textX, textY + 1, 0xFF000000);
        graphics.text(mc.font, castStr, textX, textY - 1, 0xFF000000);

        graphics.text(mc.font, castStr, textX, textY, color);
    }
}