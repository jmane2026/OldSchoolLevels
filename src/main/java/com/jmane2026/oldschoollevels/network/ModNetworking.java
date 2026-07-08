package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.OldSchoolLevels;
import com.jmane2026.oldschoollevels.client.gui.DamageIndicatorManager;
import com.jmane2026.oldschoollevels.client.gui.XpNotificationOverlay;
import com.jmane2026.oldschoollevels.common.SkillAttributeHandler;
import com.jmane2026.oldschoollevels.core.ModAttachments;
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
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        XpNotificationOverlay.notify(payload.skill(), payload.amount(), payload.totalXp());
                    });
                }
        );

        registrar.playToClient(
                DamageNumberPayload.TYPE,
                DamageNumberPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        DamageIndicatorManager.add(
                                payload.x(), payload.y(), payload.z(), payload.amount()
                        );
                    });
                }
        );

        registrar.playToServer(
                ChangeStylePayload.TYPE,
                ChangeStylePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        context.player().setData(ModAttachments.COMBAT_STYLE, payload.style());
                        SkillAttributeHandler.refreshAttributes((net.minecraft.server.level.ServerPlayer) context.player());
                    });
                }
        );
    }
}