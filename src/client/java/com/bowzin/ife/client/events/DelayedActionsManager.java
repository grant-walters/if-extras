package com.bowzin.ife.client.events;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.imaginefun.ImagineFunUtils;
import net.imaginefun.networking.HandshakePayload;
import net.imaginefun.session.ApiSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

public class DelayedActionsManager {
    private static final int HANDSHAKE_RETRY_TICKS = 100;
    private static final int PARTY_CHECK_DELAY = 20;
    private static int ticks_alive = -1;

    private static boolean watchForParty = false;


    public static void initialize() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerData server = client.getCurrentServer();
            if (server != null && server.ip.toLowerCase().contains("imaginefun.net")) {
                ticks_alive = 0;
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, sender) -> ticks_alive=-1);
        ClientTickEvents.END_CLIENT_TICK.register((client) -> onTick(client));
        ClientReceiveMessageEvents.ALLOW_GAME.register((component,overlay)->onChat(component,overlay));
    }

    private static void onTick(Minecraft client) {
        if(ticks_alive == -1) return;
        ticks_alive++;
        if(ticks_alive == PARTY_CHECK_DELAY) {
            client.player.connection.sendCommand("p list");
            watchForParty = true;
        }
        if(!ApiSession.isActive() && ticks_alive % HANDSHAKE_RETRY_TICKS == 0) {
            sendHandshake();
        }
    }

    private static void sendHandshake() {
        String version = FabricLoader.getInstance()
                .getModContainer(ImagineFunUtils.MOD_ID)
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        ClientPlayNetworking.send(new HandshakePayload(version));
    }

    private static boolean onChat(Component component, boolean isOverlay) {
        if(watchForParty && component.getString().equals("ᴘᴀʀᴛʏ: You are not in a party!")) {
            watchForParty = false;
            return false;
        }
        return true;
    }
}
