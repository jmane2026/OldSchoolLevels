package com.jmane2026.oldschoollevels.network;

import com.jmane2026.oldschoollevels.client.gui.DamageIndicatorManager;
import com.jmane2026.oldschoollevels.client.gui.UnlockToast;
import com.jmane2026.oldschoollevels.client.gui.WarningOverlay;
import com.jmane2026.oldschoollevels.client.gui.XpNotificationOverlay;
import net.minecraft.client.Minecraft;

/**
 * Safe isolation class for client-side packet logic to prevent dedicated server crashes.
 */
public class ClientPayloadHandler {
    
    public static void handleXpGain(XpGainPayload payload) {
        XpNotificationOverlay.notify(payload.skill(), payload.amount(), payload.totalXp());
    }

    public static void handleDamageSplat(DamageNumberPayload payload) {
        DamageIndicatorManager.add(payload.amount(), payload.isCritical(), payload.isIncoming());
    }

    public static void handleUnlock(UnlockNotificationPayload payload) {
        UnlockToast.add(Minecraft.getInstance().getToastManager(), payload.skill(), payload.description(), payload.icon());
    }

    public static void handleWarning(WarningPayload payload) {
        WarningOverlay.showWarning(payload.message());
    }
}