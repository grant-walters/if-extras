package com.bowzin.ife.client.network;

import com.bowzin.ife.client.events.IFEEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.multiplayer.ServerData;

public class ConnectionManager {
    private static boolean isOnImagineFun;

    public static void initialize() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerData server = client.getCurrentServer();
            if (server != null && server.ip.toLowerCase().contains("imaginefun.net")) {
                IFEEvents.IMAGINE_FUN_JOIN_EVENT.invoker().onJoin(handler,sender,client);
                isOnImagineFun=true;
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->isOnImagineFun=false);
    }

    public boolean isOnServer() {
        return isOnImagineFun;
    }

}
