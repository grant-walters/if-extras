package com.bowzin.ife.client.gui.cogs;

import com.bowzin.ife.client.IFEClient;
import com.bowzin.ife.client.config.IFEConfig;
import com.bowzin.ife.client.gui.HudCog;
import com.bowzin.ife.client.data.UserRideData;
import com.bowzin.ife.client.data.RideDurationsData;
import net.imaginefun.api.ImagineFunClientEvents;
import net.imaginefun.api.model.RideTotals;
import net.imaginefun.networking.RideStatusPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import static com.bowzin.ife.client.events.IFEEvents.IMPROPER_DISMOUNT_EVENT;

public class RideProgressCog extends HudCog {

    private final UserRideData rideStats;
    private String rideId;
    private String rideName;
    private long startTime = 0;
    private long duration = 0;

    public RideProgressCog(String id, IFEConfig.CogSettings settings, UserRideData rideStats) {
        super(id, settings, 120, false);
        this.rideStats = rideStats;
        ImagineFunClientEvents.RIDE_STATUS.register(this::onRideStatus);
        IMPROPER_DISMOUNT_EVENT.register(this::clear);
        //rendering setup
        row()
                .center(this::getTitle)
                .draw((g, w, h) -> g.fill(0, 0, w, h, TITLE_BOX_COLOR))
                .add();
        row()
                .left(this::formatRideCount)
                .right(this::formatRideTime)
                .center(this::getTimer)
                .draw(this::drawBar)
                .height(() -> getFont().lineHeight + 3)
                .add();
    }

    private void drawBar(GuiGraphicsExtractor g, int w, int h) {
        final int BAR_COLOR = 0x3552B8;
        final int BAR_ALPHA = 0xCC;

        float percent = duration > 0 ? Math.min(1.0f, (float) getRuntime() / duration) : 0f;
        float guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        float pixels = guiScale * w * percent;
        int fullPixels = (int) pixels;
        float bleedOver = pixels - fullPixels;
        g.pose().pushMatrix();
        g.pose().scale(1 / guiScale, 1f);
        g.fill(0, 0, fullPixels, h, (BAR_ALPHA << 24) | BAR_COLOR);
        g.fill(fullPixels, 0, fullPixels + 1, h, ((int) (BAR_ALPHA * bleedOver) << 24) | BAR_COLOR);
        g.pose().popMatrix();
    }

    private void onRideStatus(RideStatusPayload payload) {
        if (payload.riding()) {
            rideId = payload.rideId();
            rideName = payload.displayName();
            startTime = Util.getMillis();

            RideDurationsData.Ride ride = RideDurationsData.rides.get(rideId);
            duration = (ride == null || ride.durationMs() == null) ? 0 : ride.durationMs();
            setVisible(true);
        } else {
            IFEClient.LOGGER.info("[Ride Log] {} completed in {}\n[Ride Log] Recorded time: {}",payload.rideId(),payload.durationMs(),getRuntime());
            clear();
            rideStats.refresh();
            setVisible(false);
        }
    }

    private long getRuntime() {
        return Util.getMillis() - startTime;
    }

    private String formatClock(long time) {
        long totalSeconds = time / 1000;
        long seconds = totalSeconds % 60;
        long minutes = totalSeconds / 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private Component getTimer() {
        if (duration > 0) {
            return Component.literal(formatClock(getRuntime()) + "/" + formatClock(duration));
        } else {
            return Component.literal(formatClock(getRuntime()));
        }
    }

    private Component getTitle() {
        return Component.literal(rideName == null ? "Roaming the Park" : rideName)
                .withStyle(ChatFormatting.BOLD)
                .withColor(ACCENT_COLOR);
    }

    private Component formatRideCount() {
        RideTotals ride = rideStats.getRideOverall(rideId);
        String fString = String.format("x%,d", ride.count());
        return Component.literal(fString).withColor(SUB_TEXT_COLOR);
    }

    private Component formatRideTime() {
        RideTotals ride = rideStats.getRideOverall(rideId);
        long seconds = ride.time() / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        String fString = String.format("%dh%02dm", hours, minutes);
        return Component.literal(fString).withColor(SUB_TEXT_COLOR);
    }

    public void clear() {
        setVisible(false);
        rideId = null;
        rideName = null;
        startTime = Util.getMillis();
        duration = 0;
    }
}
