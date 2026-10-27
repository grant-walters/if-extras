package com.bowzin.ife.client.config;

import com.bowzin.ife.client.IFEClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class IFEConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static IFEConfig instance = new IFEConfig();
    private static final Path CONFIGPATH = FabricLoader.getInstance().getConfigDir().resolve("ife.json");
    // TODO(bug/MEDIUM): GSON.fromJson returns null (no exception) for an empty file, so instance becomes null and
    //  IFEGUI.initialize() crashes the game on startup. Fall back to new IFEConfig() when the result is null.
    public static void load() {
        if(!Files.exists(CONFIGPATH)) {
            save();
        }
        try (BufferedReader reader = Files.newBufferedReader(CONFIGPATH)) {
            instance = GSON.fromJson(reader, IFEConfig.class);
        } catch (Exception e) {
            IFEClient.LOGGER.error("!!! UNABLE TO LOAD ife.json !!!",e);
        }
    }
    // TODO(bug/MEDIUM): newBufferedWriter truncates the file before writing, so a crash or kill mid-save leaves an
    //  empty or partial ife.json (an empty one then crashes on load, see above). Write to ife.json.tmp, then
    //  Files.move(..., ATOMIC_MOVE, REPLACE_EXISTING).
    public static void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIGPATH)) {
            GSON.toJson(instance,writer);
        } catch (Exception e) {
            IFEClient.LOGGER.error("!!! UNABLE TO SAVE ife.json !!!",e);
        }
    }
    public static IFEConfig get() {
        return instance;
    }


    public CogSettings rideProgress = new CogSettings(0.5f, 0.01f, 1f, true);
    public CogSettings coinsAndKeys = new CogSettings(0.99f, 0.01f, 1f, true);
    public CogSettings playersAndMult = new CogSettings(0.01f, 0.01f, 1f, true);
    public CogSettings party = new CogSettings(0.99f, 0.5f, 1f, true);
    public CogSettings time = new CogSettings(0.99f, 0.99f, 1f, true);

    private IFEConfig() {}

    // TODO(LOW): reset() doesn't touch isEnabled, so "Reset GUI" won't bring back hidden elements (fine if intended).
    //  It also repeats every default from the field initializers above, which is why it fell out of sync. Consider
    //  copying from a fresh new IFEConfig() instead.
    public void reset() {
        rideProgress.set(0.5f, 0.01f, 1f);
        coinsAndKeys.set(0.99f, 0.01f, 1f);
        playersAndMult.set(0.01f, 0.01f, 1f);
        party.set(0.99f, 0.5f, 1f);
        time.set(0.99f, 0.99f, 1f);
    }

    public static class CogSettings {
        private float x=0.5f;
        private float y=0.5f;
        private float scale=1f;
        private boolean isEnabled=true;

        public CogSettings() {}

        public CogSettings(float x, float y, float scale, boolean isEnabled) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.isEnabled = isEnabled;
        }

        public float getX() {
            return x;
        }
        public float getY() {
            return y;
        }
        public float getScale() {
            return scale;
        }
        public boolean isEnabled() {return isEnabled;}

        public void set(float x, float y, float scale) {
            this.x = x;
            this.y = y;
            this.scale = scale;
        }
        public void setX(float x) {
            this.x = x;
        }
        public void setY(float y) {
            this.y = y;
        }
        public void setScale(float scale) {
            this.scale = scale;
        }
        public void toggle() {this.isEnabled = !this.isEnabled; }
    }
}

