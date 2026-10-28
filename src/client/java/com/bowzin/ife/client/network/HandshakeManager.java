package com.bowzin.ife.client.network;

import com.bowzin.ife.client.events.DelayedActionsManager;
import com.bowzin.ife.client.events.IFEEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.imaginefun.ImagineFunUtils;
import net.imaginefun.networking.HandshakePayload;
import net.imaginefun.session.ApiSession;

public class HandshakeManager {

    public static void initialize() {
        IFEEvents.IMAGINE_FUN_JOIN_EVENT.register(()->scheduleHandshake());
    }

    private static void scheduleHandshake() {
        DelayedActionsManager.schedule(100,()->{
            if(!ApiSession.isActive()) {
                sendHandshake();
                scheduleHandshake();
            }
        });
    }

    private static void sendHandshake() {
        String version = FabricLoader.getInstance()
                .getModContainer(ImagineFunUtils.MOD_ID)
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        ClientPlayNetworking.send(new HandshakePayload(version));
    }
}
