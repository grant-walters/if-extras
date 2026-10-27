package com.bowzin.ife.client.gui.cogs;

import com.bowzin.ife.client.config.IFEConfig;
import com.bowzin.ife.client.gui.HudCog;
import com.bowzin.ife.client.data.ScoreboardData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class CoinsAndKeysCog extends HudCog {


    public CoinsAndKeysCog(String id, IFEConfig.CogSettings settings, ScoreboardData scoreboardData) {
        super(id, settings, 20, true);
        this.hasBackground = false;
        row()
                .right(()-> Component.literal(scoreboardData.getStat(ScoreboardData.Stat.COINS)))
                .draw((g,w,h)-> {
                    float scaleFac = 1.2f * ((float) Minecraft.getInstance().font.lineHeight / 16);
                    g.pose().pushMatrix();
                    g.pose().scale(scaleFac);
                    g.fakeItem(new ItemStack(Items.COAL), 0, Math.round((h-(16*scaleFac))/2));
                    g.pose().popMatrix();
                })
                .width(()->getWidth(scoreboardData.getStat(ScoreboardData.Stat.COINS)))
                .add();
        row()
                .right(()-> Component.literal(scoreboardData.getStat(ScoreboardData.Stat.MAGIC_KEYS)))
                .draw((g,w,h)-> {
                    float scaleFac = 1.2f * ((float) Minecraft.getInstance().font.lineHeight / 16);
                    float movement = Minecraft.getInstance().font.width(scoreboardData.getStat(ScoreboardData.Stat.COINS)) -
                            Minecraft.getInstance().font.width(scoreboardData.getStat(ScoreboardData.Stat.MAGIC_KEYS));
                    g.pose().pushMatrix();
                    g.pose().translate(movement,0);
                    g.pose().scale(scaleFac);
                    ItemStack key = new ItemStack(Items.IRON_SHOVEL);
                    key.setDamageValue(121);
                    g.fakeItem(key, 0, Math.round((h-(16*scaleFac))/2));
                    g.pose().popMatrix();
                })
                .add();

    }

    private int getWidth(String coinCount) {
        float scaleFac = 1.2f * ((float) Minecraft.getInstance().font.lineHeight / 16);
        return Minecraft.getInstance().font.width(coinCount)+(int)(16*scaleFac)+(SPACING*2);
    }
}
