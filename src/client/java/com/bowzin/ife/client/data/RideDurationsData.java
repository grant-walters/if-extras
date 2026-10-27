package com.bowzin.ife.client.data;

import com.bowzin.ife.client.IFEClient;
import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


public class RideDurationsData {

    public static Map<String,Ride> rides = Map.of();

    public static void initialize() {
        Path ridesPath = FabricLoader.getInstance().getModContainer(IFEClient.MOD_ID).get().findPath("rides.json").get();
        try (BufferedReader reader = Files.newBufferedReader(ridesPath)) {
            rides = new Gson().fromJson(reader,Rides.class).rides();
        } catch (Exception e) {
            IFEClient.LOGGER.error("!!! UNABLE TO LOAD rides.json !!!",e);
        }
    }

    record Rides(Map<String,Ride> rides) {}
    public record Ride(String name, Long durationMs) {}
}
