package com.bowzin.ife.client.data;

import com.bowzin.ife.client.IFEClient;
import net.imaginefun.api.ImagineFunApi;
import net.imaginefun.api.model.RideStats;
import net.imaginefun.api.model.RideTotals;

import java.util.Map;

public class UserRideData {

    private RideTotals overall,yearly,weekly;
    private Map<String, RideStats> rides = Map.of();

    public RideTotals getRideOverall(String rideId) {
        if(rideId==null) {
            return new RideTotals(0,0);
        }
         RideStats rideStats = rides.get(rideId);
         if (rideStats == null) {
             return new RideTotals(0,0);
         }
         return rideStats.overall();
    }

    public RideTotals getOverall() {
        return overall;
    }

    public RideTotals getYearly() {
        return yearly;
    }

    public RideTotals getWeekly() {
        return weekly;
    }

    public void refresh() {
        ImagineFunApi.getSessionRides(IFEClient.MOD_ID).whenComplete((result, err)->{
            if(err == null) {
                rides = result.rides();
                overall = result.overall();
                yearly = result.yearly();
                weekly = result.weekly();
            }
        });
    }
}
