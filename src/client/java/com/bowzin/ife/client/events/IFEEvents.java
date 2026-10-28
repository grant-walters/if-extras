package com.bowzin.ife.client.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.scores.Objective;

public class IFEEvents {

    public static final Event<ScoreboardUpdateListener> SCOREBOARD_UPDATE_EVENT = EventFactory.createArrayBacked(
            ScoreboardUpdateListener.class,
            listeners -> arg -> {
                for(ScoreboardUpdateListener event : listeners) { event.onUpdate(arg); }
            });
    public static final Event<ImproperDismountListener> IMPROPER_DISMOUNT_EVENT  = EventFactory.createArrayBacked(
            ImproperDismountListener.class,
            listeners -> () -> {
                for(ImproperDismountListener event : listeners) { event.onDismount(); }
            });
    public static final Event<ImagineFunJoinListener> IMAGINE_FUN_JOIN_EVENT = EventFactory.createArrayBacked(
            ImagineFunJoinListener.class,
            listeners -> (handler,sender,client) -> {
                for(ImagineFunJoinListener event : listeners) { event.onJoin(handler,sender,client); }
            }
    );

    public interface ScoreboardUpdateListener {
        void onUpdate(Objective objective);
    }
    public interface ImproperDismountListener {
        void onDismount();
    }
    public interface ImagineFunJoinListener {
        void onJoin(ClientPacketListener handler, PacketSender sender, Minecraft client);
    }
}
