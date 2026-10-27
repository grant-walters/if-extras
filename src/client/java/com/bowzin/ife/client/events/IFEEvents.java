package com.bowzin.ife.client.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
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

    public interface ScoreboardUpdateListener {
        void onUpdate(Objective objective);
    }
    public interface ImproperDismountListener {
        void onDismount();
    }
}
