package com.bowzin.ife.client.gui;

import com.bowzin.ife.client.config.IFEConfig;
import com.bowzin.ife.client.gui.cogs.*;
import com.bowzin.ife.client.data.UserRideData;
import com.bowzin.ife.client.IFEClient;
import com.bowzin.ife.client.data.ScoreboardData;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.imaginefun.api.ImagineFunClientEvents;
import net.imaginefun.session.ApiSession;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;

public class IFEGUI {

    public static ArrayList<HudCog> cogs = new ArrayList<>();

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(IFEClient.MOD_ID,"key_category"));
    public static final KeyMapping EDIT_GUI_KEYBIND = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.ife.edit_gui",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_BACKSLASH,
                    CATEGORY
            )
    );

    public static void initialize() {

        UserRideData rideStats = new UserRideData();
        ScoreboardData scoreboardData = new ScoreboardData();
        cogs.add(new RideProgressCog("ride-progress-cog", IFEConfig.get().rideProgress, rideStats));
        cogs.add(new CoinsAndKeysCog("coins-keys-cog", IFEConfig.get().coinsAndKeys, scoreboardData));
        cogs.add(new PlayersAndMultCog("players-mult-cog", IFEConfig.get().playersAndMult, scoreboardData));
        cogs.add(new PartyCog("party-cog", IFEConfig.get().party));
        cogs.add(new TimeCog("time-cog", IFEConfig.get().time));
        for(HudCog cog : cogs) {
            registerCog(cog);
        }

        disableScoreboard();
        ImagineFunClientEvents.SESSION_UPDATED.register(payload -> rideStats.refresh());
        ClientTickEvents.END_CLIENT_TICK.register(IFEGUI::editScreenKeybind);
    }

    private static void registerCog(HudCog cog) {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(IFEClient.MOD_ID, cog.id()),
                (graphics,delta) -> {
                    if(!(Minecraft.getInstance().gui.screen() instanceof ConfigScreen)) {
                        cog.extractRenderState(graphics,delta);
                    }
                }
        );
    }

    private static void disableScoreboard() {
        HudElementRegistry.replaceElement(VanillaHudElements.SCOREBOARD, original -> (g, delta) ->
        {
            if (!ApiSession.isActive()) {
                original.extractRenderState(g, delta);
            }
        });
    }

    private static void editScreenKeybind(Minecraft client) {
        while (EDIT_GUI_KEYBIND.consumeClick()) {
            if (client.player == null) return;
            Minecraft.getInstance().gui.setScreen(new ConfigScreen());
        }
    }
}