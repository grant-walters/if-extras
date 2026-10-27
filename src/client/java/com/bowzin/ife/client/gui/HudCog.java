package com.bowzin.ife.client.gui;

import com.bowzin.ife.client.config.IFEConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.imaginefun.session.ApiSession;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public abstract class HudCog implements HudElement {

    //configs
    protected IFEConfig.CogSettings cogSettings;
    private float xCoord,yCoord;
    // set by cog type
    private final int minWidth;
    protected String id;

    protected boolean isDebugMode = false;

    private int width = 0, height = 0;
    private boolean shouldRender;
    private final ArrayList<Row> rows = new ArrayList<>();

    protected boolean hasBackground = true;

    protected static final int SPACING = 4;
    protected static final int BOX_COLOR = 0xCA0D1530;
    protected static final int TITLE_BOX_COLOR = 0xE61D2D63;
    protected static final int TEXT_COLOR = 0xFFF5F1E6;
    protected static final int SUB_TEXT_COLOR = 0xFF9AA5C9;
    protected static final int ACCENT_COLOR = 0xFFFFD66B;

    public HudCog(String id, IFEConfig.CogSettings settings, int minWidth, boolean renderByDefault) {
        this.id = id;
        this.cogSettings = settings;
        this.shouldRender = renderByDefault;
        this.minWidth = minWidth;
    }

    public String id() { return id; }

    public void resetRows(int rowsToKeep) {
        rows.subList(rowsToKeep,rows.size()).clear();
    }

    public boolean toggleVisible() {
        return shouldRender = !shouldRender;
    }

    public void setVisible(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }

    public float getScale() {
        return cogSettings.getScale();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker tickDelta) {
        if ( ((!shouldRender||!cogSettings.isEnabled())&&!isDebugMode)  || !ApiSession.isActive() ) {
            return;
        }
        //set width to biggest row
        width = minWidth;
        height = 0;
        for (Row row : rows) {
            width = Math.max(width, row.width().getAsInt());
            height += row.height().getAsInt();
        }
        //change position according to config settings
        graphics.pose().pushMatrix();
        xCoord = (graphics.guiWidth() - width * getScale()) * cogSettings.getX();
        yCoord = (graphics.guiHeight() - height * getScale()) * cogSettings.getY();
        graphics.pose().translate(xCoord, yCoord);
        graphics.pose().scale(getScale());
        //draw box and then each row
        if(hasBackground) {
            graphics.fill(0, 0, width, height, BOX_COLOR);
        }
        graphics.pose().pushMatrix();
        for (Row row : rows) {
            int rowHeight = row.height().getAsInt();
            row.renderer().render(graphics, width, rowHeight);
            graphics.pose().translate(0, rowHeight);
        }
        graphics.pose().popMatrix();
        if(isDebugMode) {
            float sinVal = ((float) Math.sin(Util.getMillis()/200f)+1f)/2f;
            int rgb;
            float satVal = (sinVal*0.4f)+0.3f;
            if(cogSettings.isEnabled()) {
                rgb = Mth.hsvToArgb(0.72f, satVal, 1.0f,255);
            } else {
                rgb = Mth.hsvToArgb(0.01f, satVal, 1.0f,255);
            }
            graphics.outline(-1,-1,width+2,height+2,rgb);
            graphics.outline(-2,-2,width+4,height+4,rgb);
        }
        graphics.pose().popMatrix();
    }

    public void setDebugMode(boolean status) {
        isDebugMode = status;
    }

    public void toggle() {
        cogSettings.toggle();
    }

    public float x() {return xCoord;}

    public float y() {return yCoord;}

    public float width() {return width*getScale();}

    public float height() {return height*getScale();}

    public void setX(float x) {
        cogSettings.setX(x);
    }

    public void setY(float y) {
        cogSettings.setY(y);
    }

    public void setScale(float scale) {cogSettings.setScale(scale);}

    public boolean isRendered() {
        return shouldRender;
    }

    protected Font getFont() { return Minecraft.getInstance().font; }

    public void setPos(float x, float y) {
        cogSettings.setX(x); cogSettings.setY(y);
    }

    public interface RowRenderer {
        void render(GuiGraphicsExtractor graphics, int width, int height);
    }

    private record Row(IntSupplier width, IntSupplier height, RowRenderer renderer) {
    }

    protected RowBuilder row() {
        return new RowBuilder();
    }

    protected class RowBuilder {
        private IntSupplier width, height;
        private Supplier<Component> leftFactory, centerFactory, rightFactory;
        private RowRenderer drawRenderer;


        public RowBuilder left(Supplier<Component> left) {
            leftFactory = left;
            return this;
        }

        public RowBuilder center(Supplier<Component> center) {
            centerFactory = center;
            return this;
        }

        public RowBuilder right(Supplier<Component> right) {
            rightFactory = right;
            return this;
        }

        public RowBuilder left(String left) {
            leftFactory = () -> Component.literal(left).withColor(TEXT_COLOR);
            return this;
        }

        public RowBuilder center(String center) {
            centerFactory = () -> Component.literal(center).withColor(TEXT_COLOR);
            return this;
        }

        public RowBuilder right(String right) {
            rightFactory = () -> Component.literal(right).withColor(TEXT_COLOR);
            return this;
        }

        public RowBuilder draw(RowRenderer renderer) {
            drawRenderer = renderer;
            return this;
        }

        public RowBuilder width(IntSupplier width) {
            this.width = width;
            return this;
        }

        public RowBuilder height(IntSupplier height) {
            this.height = height;
            return this;
        }

        public RowBuilder width(int width) {
            this.width = () -> width;
            return this;
        }

        public RowBuilder height(int height) {
            this.height = () -> height;
            return this;
        }

        public void add() {
            Supplier<Component> leftCopy = leftFactory;
            Supplier<Component> centerCopy = centerFactory;
            Supplier<Component> rightCopy = rightFactory;
            RowRenderer drawCopy = drawRenderer;
            // if width wasn't overridden, calculate based on text elements
            if (width == null) {
                width = () -> {
                    int lWidth, cWidth, rWidth;
                    lWidth = textWidth(leftCopy);
                    cWidth = textWidth(centerCopy);
                    rWidth = textWidth(rightCopy);
                    return combinedWidth(lWidth,cWidth,rWidth);
                };
            }
            if (height == null) {
                height = () -> getFont().lineHeight + SPACING;
            }
            RowRenderer finalRender = (g, w, h) -> {
                if (drawCopy != null) {
                    drawCopy.render(g, w, h);
                }
                int vSpacing = Math.ceilDiv(h-getFont().lineHeight+1,2);

                Component leftComp = leftCopy == null ? null : leftCopy.get();
                Component centerComp = centerCopy == null ? null : centerCopy.get();
                Component rightComp = rightCopy == null ? null : rightCopy.get();
                Font font = getFont();
                if (leftComp != null) {
                    g.text(font, leftComp, SPACING, vSpacing, TEXT_COLOR);
                }
                if (centerComp != null) {
                    g.centeredText(font, centerComp, w / 2, vSpacing, TEXT_COLOR);
                }
                if (rightComp != null) {
                    g.text(font, rightComp, w - (font.width(rightComp) + SPACING), vSpacing, TEXT_COLOR);
                }
            };
            rows.add(
                    new Row(width, height, finalRender)
            );
        }

        private int textWidth(Supplier<Component> componentSupplier) {
            if(componentSupplier==null) return 0;
            Component component = componentSupplier.get();
            if(component==null) return 0;
            return getFont().width(component)+SPACING*2;
        }

        private int combinedWidth(int left, int center, int right) {
            if(center ==0) {
                if(left ==0) return right;
                if(right ==0) return left;
            }
            return (Math.max(left, right)*2)+ center;
        }
    }
}


