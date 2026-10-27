package com.bowzin.ife.client;

import com.bowzin.ife.client.cmds.IFECmds;
import com.bowzin.ife.client.config.IFEConfig;
import com.bowzin.ife.client.events.DelayedActionsManager;
import com.bowzin.ife.client.gui.IFEGUI;
import com.bowzin.ife.client.data.RideDurationsData;
import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IFEClient implements ClientModInitializer {
    public static final String MOD_ID = "ife";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        IFECmds.register();
        IFEConfig.load();
        RideDurationsData.initialize();
        IFEGUI.initialize();
        DelayedActionsManager.initialize();
    }

}