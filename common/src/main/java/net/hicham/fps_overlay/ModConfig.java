package net.hicham.fps_overlay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum OverlayPosition {
        TOP_LEFT("enum.fps_overlay.overlayposition.top_left"),
        TOP_CENTER("enum.fps_overlay.overlayposition.top_center"),
        TOP_RIGHT("enum.fps_overlay.overlayposition.top_right"),
        CENTER_LEFT("enum.fps_overlay.overlayposition.center_left"),
        CENTER_RIGHT("enum.fps_overlay.overlayposition.center_right"),
        BOTTOM_LEFT("enum.fps_overlay.overlayposition.bottom_left"),
        BOTTOM_CENTER("enum.fps_overlay.overlayposition.bottom_center"),
        BOTTOM_RIGHT("enum.fps_overlay.overlayposition.bottom_right");

        private final String translationKey;

        OverlayPosition(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component getDisplayText() {
            return Component.translatable(translationKey);
        }
    }

    public enum OverlayStyle {
        DEFAULT,
        NAVBAR
    }

    public enum TextEffect {
        NONE,
        SHADOW,
        OUTLINE
    }

    public enum ThemePreset {
        CLASSIC_DARK,
        LIGHT,
        GLASS,
        CUSTOM
    }

    public General general = new General();
    public HUD hud = new HUD();
    public Appearance appearance = new Appearance();

    public static class General {
        public boolean enabled = true;
        public boolean enableKeybindings = true;
        public int updateIntervalMs = 250;
        public int configVersion = 5;
    }

    public static class HUD {
        public boolean showFps = true;
        public boolean showAverageFps = true;
        public boolean showFrameTime = true;
        public boolean show1PercentLow = true;

        public boolean showMemory = true;
        public boolean showCpuUsage = false;
        public boolean showGpuUsage = false;

        public boolean showPing = true;
        public boolean showMspt = true;
        public boolean showTps = true;
        public boolean showChunks = false;
        public boolean showCoordinates = false;
        public boolean showBiome = false;
        public boolean showGraph = false;
        public boolean showMinMaxStats = false;

        public List<String> metricOrder = new ArrayList<>(OverlayMetric.defaultOrderIds());
        public Map<String, String> metricDisplayNames = new LinkedHashMap<>();

        public boolean isMetricEnabled(OverlayMetric metric) {
            return switch (metric) {
                case FPS -> showFps;
                case AVG_FPS -> showAverageFps;
                case FRAME_TIME -> showFrameTime;
                case LOW_1 -> show1PercentLow;
                case MEMORY -> showMemory;
                case CPU -> showCpuUsage;
                case GPU -> showGpuUsage;
                case PING -> showPing;
                case MSPT -> showMspt;
                case TPS -> showTps;
                case CHUNKS -> showChunks;
                case COORDS -> showCoordinates;
                case BIOME -> showBiome;
            };
        }

        public void setMetricEnabled(OverlayMetric metric, boolean enabled) {
            switch (metric) {
                case FPS -> showFps = enabled;
                case AVG_FPS -> showAverageFps = enabled;
                case FRAME_TIME -> showFrameTime = enabled;
                case LOW_1 -> show1PercentLow = enabled;
                case MEMORY -> showMemory = enabled;
                case CPU -> showCpuUsage = enabled;
                case GPU -> showGpuUsage = enabled;
                case PING -> showPing = enabled;
                case MSPT -> showMspt = enabled;
                case TPS -> showTps = enabled;
                case CHUNKS -> showChunks = enabled;
                case COORDS -> showCoordinates = enabled;
                case BIOME -> showBiome = enabled;
            }
        }

        public String getMetricDisplayName(OverlayMetric metric) {
            String customName = getCustomMetricName(metric);
            return customName.isBlank() ? Component.translatable(metric.getLabelKey()).getString() : customName;
        }

        public String getCustomMetricName(OverlayMetric metric) {
            if (metricDisplayNames == null) {
                return "";
            }

            String value = metricDisplayNames.get(metric.getId());
            return value == null ? "" : value.trim();
        }

        public void setCustomMetricName(OverlayMetric metric, String value) {
            if (metricDisplayNames == null) {
                metricDisplayNames = new LinkedHashMap<>();
            }

            if (value == null || value.trim().isEmpty()) {
                metricDisplayNames.remove(metric.getId());
                return;
            }

            metricDisplayNames.put(metric.getId(), value.trim());
        }
    }

    public static class Appearance {
        public OverlayPosition position = OverlayPosition.TOP_CENTER;
        public OverlayStyle overlayStyle = OverlayStyle.NAVBAR;
        public TextEffect textEffect = TextEffect.NONE;
        public ThemePreset themePreset = ThemePreset.CLASSIC_DARK;

        public boolean showBackground = true;
        public int backgroundOpacity = 180;
        public int backgroundColor = 0x212B36;
        public int labelColor = 0xFF839DB1;
        public int valueColor = 0xFFFFFFFF;
        public int unitColor = 0xFFC99566;
        public int dividerColor = 0xFF354451;
        public int goodColor = 0xFF2ED177;
        public int warningColor = 0xFFFFD100;
        public int badColor = 0xFFFF4545;

        public float hudScale = 0.65f;
        public boolean adaptiveColors = true;
        public boolean autoHideF3 = true;
        public int xOffset = 0;
        public int yOffset = 0;

        public void applyThemePreset(ThemePreset preset) {
            switch (preset) {
                case CLASSIC_DARK -> {
                    backgroundColor = 0x212B36;
                    labelColor = 0xFF839DB1;
                    valueColor = 0xFFFFFFFF;
                    unitColor = 0xFFC99566;
                    dividerColor = 0xFF354451;
                }
                case LIGHT -> {
                    backgroundColor = 0xF0F4F8;
                    labelColor = 0xFF334E68;
                    valueColor = 0xFF102A43;
                    unitColor = 0xFFB35C1E;
                    dividerColor = 0xFFBCCCDC;
                }
                case GLASS -> {
                    backgroundColor = 0x0E1A24;
                    labelColor = 0xFF8FD3FF;
                    valueColor = 0xFFEAF8FF;
                    unitColor = 0xFFFFD39B;
                    dividerColor = 0xFF2D475B;
                }
                case CUSTOM -> {
                }
            }
            themePreset = preset;
        }
    }

    public void validate() {
        general.updateIntervalMs = Math.max(16, Math.min(1000, general.updateIntervalMs));

        appearance.backgroundOpacity = Math.max(0, Math.min(255, appearance.backgroundOpacity));
        appearance.hudScale = Math.max(0.2f, Math.min(1.5f, appearance.hudScale));
        appearance.xOffset = Math.max(-2000, Math.min(2000, appearance.xOffset));
        appearance.yOffset = Math.max(-2000, Math.min(2000, appearance.yOffset));

        if (appearance.position == null) {
            appearance.position = OverlayPosition.TOP_CENTER;
        }
        if (appearance.textEffect == null) {
            appearance.textEffect = TextEffect.NONE;
        }
        if (appearance.themePreset == null) {
            appearance.themePreset = ThemePreset.CLASSIC_DARK;
        }

        if (hud.metricOrder == null || hud.metricOrder.isEmpty()) {
            hud.metricOrder = new ArrayList<>(OverlayMetric.defaultOrderIds());
        } else {
            List<String> sanitized = new ArrayList<>();
            for (OverlayMetric metric : OverlayMetric.sanitizeOrder(hud.metricOrder)) {
                sanitized.add(metric.getId());
            }
            hud.metricOrder = sanitized;
        }

        if (hud.metricDisplayNames == null) {
            hud.metricDisplayNames = new LinkedHashMap<>();
        } else {
            Map<String, String> sanitizedNames = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : hud.metricDisplayNames.entrySet()) {
                OverlayMetric metric = OverlayMetric.fromId(entry.getKey());
                if (metric == null || entry.getValue() == null) {
                    continue;
                }

                String trimmed = entry.getValue().trim();
                if (!trimmed.isEmpty()) {
                    sanitizedNames.put(metric.getId(), trimmed);
                }
            }
            hud.metricDisplayNames = sanitizedNames;
        }
    }

    public void resetToDefaults() {
        this.general = new General();
        this.hud = new HUD();
        this.appearance = new Appearance();
        validate();
    }

    public static ModConfig load(File file) {
        ModConfig loadedConfig;
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                loadedConfig = GSON.fromJson(reader, ModConfig.class);
                if (loadedConfig == null) {
                    loadedConfig = new ModConfig();
                }
            } catch (Exception e) {
                System.err.println("Failed to load FPS Overlay config, reverting to defaults.");
                loadedConfig = new ModConfig();
            }
        } else {
            loadedConfig = new ModConfig();
        }

        loadedConfig.validate();
        save(file, loadedConfig);
        return loadedConfig;
    }

    public static void save(File file, ModConfig config) {
        if (config != null && file != null) {
            config.validate();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(config, writer);
            } catch (IOException e) {
                System.err.println("Failed to save FPS Overlay config!");
            }
        }
    }
}
