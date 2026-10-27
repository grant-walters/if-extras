package com.bowzin.ife.client.gui.cogs;

import com.bowzin.ife.client.config.IFEConfig;
import com.bowzin.ife.client.gui.HudCog;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class TimeCog extends HudCog {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
    public TimeCog(String id, IFEConfig.CogSettings settings) {
        super(id, settings, 20, true);
        this.hasBackground = false;

        row()
                .left(this::getTime)
                .add();

    }

    private Component getTime() {
        return Component.literal(LocalTime.now().format(formatter));
    }
}
