package com.bowzin.ife.client.gui;

import com.bowzin.ife.client.config.IFEConfig;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    private HudCog holding = null;
    private double xOffset = 0;
    private double yOffset = 0;
    //SPACES for manual spacing, oops i liked this lazy crazy method
    private final Component[] lines = {
        Component.literal("  Click & Drag to move an element.  "),
        Component.literal("  Hover & Scroll to resize an element.  "),
        Component.literal("  Double Click to toggle an element.  ")
    };

    public ConfigScreen() {
        super(Component.literal("IF:E Config"));
    }

    @Override
    protected void init() {
        for(HudCog cog : IFEGUI.cogs) {
            cog.setDebugMode(true);
        }
        Component resetLbl = Component.literal("Reset GUI");
        int resetWidth = font.width(resetLbl)+10;
        Button resetBtn = Button.builder(resetLbl, (btn) -> {
            IFEConfig.get().reset();
        }).bounds((width-resetWidth)/2,height-45,resetWidth,20)
                .build();
        this.addRenderableWidget(resetBtn);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int width = 0;
        for(Component line : lines) {
            int lineWidth = font.width(line);
            if(lineWidth>width) {
                width = lineWidth;
            }
        }
        int height = font.lineHeight * (lines.length+2);
        graphics.pose().pushMatrix();
        graphics.pose().translate((graphics.guiWidth()-width)*0.5f,(graphics.guiHeight()-height)*0.5f);
        graphics.fill(0,0,width,height,0xCA0D1530);
        for(int i=0;i<lines.length;i++) {
            graphics.centeredText(font,lines[i], width/2, font.lineHeight*(i+1), 0xFFFFFFFF);
        }
        graphics.pose().popMatrix();
        for(HudCog cog : IFEGUI.cogs) {
            cog.extractRenderState(graphics, Minecraft.getInstance().getDeltaTracker());
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if(super.mouseClicked(event,doubleClick)) {
            return true;
        }
        HudCog hit = firstHit(event.x(),event.y());
        if(hit!=null) {
            if(doubleClick) {
                hit.toggle();
            }
            holding = hit;
            xOffset = event.x()-hit.x();
            yOffset = event.y()-hit.y();
        }
        return true;
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if(super.keyPressed(event)) {
            return true;
        }
        if(event.key()== KeyMappingHelper.getBoundKeyOf(IFEGUI.EDIT_GUI_KEYBIND).getValue()) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if(holding != null) {
            double xCoord = event.x()-xOffset;
            double yCoord = event.y()-yOffset;
            float x = Math.clamp((float) (xCoord/(this.width-holding.width())),0f,1f);
            float y = Math.clamp((float) (yCoord/(this.height-holding.height())),0f,1f);
            holding.setPos(x, y);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (holding != null) {
            holding=null;
        }
        super.mouseReleased(event);
        this.setFocused(null);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        HudCog hit = firstHit(mouseX,mouseY);
        if(hit!=null) {
            float scaleVal = Math.clamp((float) (hit.getScale()*Math.pow(1.1,scrollY)),0.33f,3f);
            hit.setScale(scaleVal);
        }
        return true;
    }

    @Override
    public void onClose() {
        for(HudCog cog : IFEGUI.cogs) {
            cog.setDebugMode(false);
        }
        IFEConfig.save();
        super.onClose();
    }


    private HudCog firstHit(double x, double y) {
        for(HudCog cog : IFEGUI.cogs.reversed()) {
            if (
                    x > cog.x() &&
                            x < cog.x() + cog.width() &&
                            y > cog.y() &&
                            y < cog.y() + cog.height()
            ) {
                return cog;
            }
        }
        return null;
    }
}
