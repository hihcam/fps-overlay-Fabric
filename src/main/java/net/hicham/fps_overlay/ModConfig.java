package net.hicham.fps_overlay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {

        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private static File configFile;
        private static ModConfig instance;

        public static ModConfig get() {
                if (instance == null) {
                        instance = new ModConfig();
                }
                return instance;
        }

        public enum OverlayPosition {
                TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
        }

        public enum OverlayStyle {
                CLASSIC, MODERN
        }

        public General general = new General();

        public static class General {
                public boolean enabled = true;
                public boolean enableKeybindings = true;
                public int updateIntervalMs = 250;
                public int configVersion = 4;
        }

        public HUD hud = new HUD();

        public static class HUD {
                public boolean showFps = true;
                public boolean showAverageFps = true;
                public int averageWindowMs = 10000;
                public boolean showMemory = true;
                public boolean showMemoryPercentage = true;
                public boolean showPing = true;
        }

        public Appearance appearance = new Appearance();

        public static class Appearance {
                public OverlayPosition position = OverlayPosition.TOP_LEFT;
                public OverlayStyle overlayStyle = OverlayStyle.MODERN;
                public float scale = 1.0f;
                public int padding = 5;
                public boolean compactMode = false;
                public boolean showBackground = true;
                public int backgroundOpacity = 180;
                public String textColorHex = "#FFFFFF";
                public boolean useTextShadow = true;
                public boolean useAdaptiveColors = true;
        }

        public void validate() {
                general.updateIntervalMs = Math.max(16, Math.min(1000, general.updateIntervalMs));
                hud.averageWindowMs = Math.max(500, Math.min(30000, hud.averageWindowMs));

                appearance.backgroundOpacity = Math.max(0, Math.min(255, appearance.backgroundOpacity));
                appearance.padding = Math.max(0, Math.min(50, appearance.padding));
                appearance.scale = Math.max(0.5f, Math.min(3.0f, appearance.scale));

                if (appearance.position == null)
                        appearance.position = OverlayPosition.TOP_LEFT;
                if (appearance.overlayStyle == null)
                        appearance.overlayStyle = OverlayStyle.MODERN;

                if (!appearance.textColorHex.matches("^#[0-9A-Fa-f]{6}$")) {
                        appearance.textColorHex = "#FFFFFF";
                }
        }

        public void resetToDefaults() {
                this.general = new General();
                this.hud = new HUD();
                this.appearance = new Appearance();
                validate();
        }

        public static void init(File file) {
                configFile = file;
                load();
        }

        @SuppressWarnings("unused")
        public static void load() {
                if (configFile.exists()) {
                        try (FileReader reader = new FileReader(configFile)) {
                                instance = GSON.fromJson(reader, ModConfig.class);
                                if (instance == null)
                                        instance = new ModConfig();
                        } catch (Exception e) {
                                System.err.println("Failed to load FPS Overlay config, reverting to defaults.");
                                instance = new ModConfig();
                        }
                } else {
                        instance = new ModConfig();
                }
                instance.validate();
                save();
        }

        public static void save() {
                if (instance != null && configFile != null) {
                        instance.validate();
                        try (FileWriter writer = new FileWriter(configFile)) {
                                GSON.toJson(instance, writer);
                        } catch (IOException e) {
                                System.err.println("Failed to save FPS Overlay config!");
                        }
                }
        }
}