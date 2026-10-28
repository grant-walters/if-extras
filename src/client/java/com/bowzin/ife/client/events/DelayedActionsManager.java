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

import java.util.Comparator;
import java.util.PriorityQueue;

public class DelayedActionsManager {
    private static int ticks_alive = -1;

    private record ScheduledAction(int tick, Runnable action) {}
    private static PriorityQueue<ScheduledAction> scheduledActions = new PriorityQueue<>(Comparator.comparingInt(ScheduledAction::tick));

    public static void initialize() {
        IFEEvents.IMAGINE_FUN_JOIN_EVENT.register((handler,sender,client)-> ticks_alive=0);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, sender) -> {
            ticks_alive = -1;
            scheduledActions.clear();
        });
        ClientTickEvents.END_CLIENT_TICK.register((client) -> onTick(client));
    }

    private static void onTick(Minecraft client) {
        if(ticks_alive == -1) return;
        ticks_alive++;
        while(!scheduledActions.isEmpty() && scheduledActions.peek().tick() <= ticks_alive) {
            scheduledActions.poll().action().run();
        }
    }

    public static void schedule(int delay, Runnable action) {
        if(ticks_alive==-1) {
            scheduledActions.add(new ScheduledAction(delay,action));
        } else {
            scheduledActions.add(new ScheduledAction(ticks_alive+delay,action));
        }
    }
}
