package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.common.MagicHandler;
import com.jmane2026.oldschoollevels.common.SkillAttributeHandler;
import com.jmane2026.oldschoollevels.common.items.SigilPouchItem;
import com.jmane2026.oldschoollevels.core.ModAttachments;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = OldSchoolLevels.MODID)
public class ModNetworking {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(OldSchoolLevels.MODID);

        registrar.playToClient(
                XpGainPayload.TYPE,
                XpGainPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> 
                        ClientPayloadHandler.handleXpGain(payload))
        );

        registrar.playToClient(
                DamageNumberPayload.TYPE,
                DamageNumberPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> 
                        ClientPayloadHandler.handleDamageSplat(payload))
        );

        registrar.playToServer(
                ChangeStylePayload.TYPE,
                ChangeStylePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        context.player().setData(ModAttachments.COMBAT_STYLE, payload.style());
                        SkillAttributeHandler.refreshAttributes((ServerPlayer) context.player());
                    });
                }
        );

        registrar.playToServer(
                CastSpellPayload.TYPE,
                CastSpellPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        MagicHandler.castSpell((ServerPlayer) context.player(), payload.spell());
                    });
                }
        );

        registrar.playToServer(
                TeleportActionPayload.TYPE,
                TeleportActionPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ServerPlayer player = (ServerPlayer) context.player();
                        if (payload.action() == TeleportActionPayload.Action.ADD) {
                            MagicHandler.addLocation(player, payload.name());
                        } else if (payload.action() == TeleportActionPayload.Action.DELETE) {
                            MagicHandler.deleteLocation(player, payload.location());
                        } else if (payload.action() == TeleportActionPayload.Action.EXECUTE) {
                            MagicHandler.handleTeleportRequest(player, payload.location(), payload.isPortal());
                        }
                    });
                }
        );

        registrar.playToServer(
                SelectSpellPayload.TYPE,
                SelectSpellPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        context.player().setData(ModAttachments.ACTIVE_SPELL.get(), payload.spell());
                    });
                }
        );

        registrar.playToClient(
                UnlockNotificationPayload.TYPE,
                UnlockNotificationPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> 
                        ClientPayloadHandler.handleUnlock(payload))
        );

        registrar.playToServer(
                AdjustPouchPayload.TYPE,
                AdjustPouchPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ServerPlayer player = (ServerPlayer) context.player();
                        ItemStack pouch = player.getMainHandItem().getItem() instanceof SigilPouchItem ? player.getMainHandItem() : player.getOffhandItem();
                        Item sigil = BuiltInRegistries.ITEM.get(Identifier.parse(payload.sigilId()))
                                .map(Holder::value)
                                .orElse(Items.AIR);
                        
                        if (!pouch.isEmpty() && SigilPouchItem.getSigilCount(pouch, sigil) > 0) {
                            SigilPouchItem.removeSigils(pouch, sigil, 64);
                            if (!player.getInventory().add(new ItemStack(sigil, 64))) {
                                player.drop(new ItemStack(sigil, 64), false);
                            }
                        }
                    });
                }
        );
    }
}