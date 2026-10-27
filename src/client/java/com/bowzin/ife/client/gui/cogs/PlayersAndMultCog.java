package com.bowzin.ife.client.gui.cogs;

import com.bowzin.ife.client.config.IFEConfig;
import com.bowzin.ife.client.gui.HudCog;
import com.bowzin.ife.client.data.ScoreboardData;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;


public class PlayersAndMultCog extends HudCog {


    public PlayersAndMultCog(String id, IFEConfig.CogSettings settings, ScoreboardData scoreboardData) {
        super(id, settings, 20, true);
        this.hasBackground = false;

        row()
                .left(()-> Component.literal("👥 "+scoreboardData.getStat(ScoreboardData.Stat.PLAYERS)))
                .add();
        row()
                .left(()-> {
                    String multStr = scoreboardData.getStat(ScoreboardData.Stat.MULTIPLIER);
                    float mult;
                    try {
                        mult = Float.parseFloat(multStr.substring(0, multStr.length() - 1));
                    } catch (Exception e) {
                        mult = 1f;
                    }
                    float lerpAmt = Math.clamp((float) (Math.log(mult) / Math.log(25)),0,1);
                    float hue = (50 + (250*lerpAmt)) / 360f;
                    int rgb = Mth.hsvToRgb(hue, 0.67f, 1.0f);
                    return Component.literal("✨ " ).append(Component.literal(multStr).withColor(rgb));
                })

                .add();
        
    }
}
