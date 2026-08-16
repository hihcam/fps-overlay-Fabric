package net.hicham.fps_overlay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModConfig {
    private static final Logger LOGGER = LogManager.getLogger("fps_overlay/ModConfig");
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
    public Thresholds thresholds = new Thresholds();
    public AutoHideRules autoHide = new AutoHideRules();
    public String selectedProfile = "Default";
    public Map<String, ConfigProfile> configProfiles = new LinkedHashMap<>();

    public static class General {
        public boolean enabled = true;
        public boolean enableKeybindings = true;
        public int updateIntervalMs = 250;
        public int configVersion = 9;
    }

    public static class HUD {
        public boolean showFps = true;
        public boolean showAverageFps = true;
        public boolean showFrameTime = false;
        public boolean show1PercentLow = true;

        public boolean showMemory = true;

        public boolean showPing = true;
        public boolean showMspt = true;
        public boolean showTps = true;
        public boolean showChunks = false;
        public boolean showCoordinates = false;
        public boolean showBiome = false;
        public boolean showGraph = false;
        public boolean showMinMaxStats = false;
        public boolean showLabels = false;

        public List<String> metricOrder = new CopyOnWriteArrayList<>(OverlayMetric.defaultOrderIds());
        public Map<String, String> metricDisplayNames = new LinkedHashMap<>();

        public boolean isMetricEnabled(OverlayMetric metric) {
            return switch (metric) {
                case FPS -> showFps;
                case AVG_FPS -> showAverageFps;
                case FRAME_TIME -> showFrameTime;
                case LOW_1 -> show1PercentLow;
                case MEMORY -> showMemory;
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

    public static class Thresholds {
        public int fpsGood = 60;
        public int fpsWarning = 30;
        public double frameTimeGood = 16.7;
        public double frameTimeWarning = 33.3;
        public int memoryGood = 75;
        public int memoryWarning = 90;
        public int pingGood = 60;
        public int pingWarning = 150;
        public double tpsGood = 19.5;
        public double tpsWarning = 15.0;
    }

    public static class AutoHideRules {
        public boolean hideInChat = false;
        public boolean hideInInventory = false;
        public boolean hideInScreenshots = false;
        public boolean hideWithF3 = true;
        public boolean fadeOnIdle = false;
        public int fadeDelaySeconds = 30;
        public float fadeOpacity = 0.35f;
    }

    public static class ConfigProfile {
        public General general = new General();
        public HUD hud = new HUD();
        public Appearance appearance = new Appearance();
        public Thresholds thresholds = new Thresholds();
        public AutoHideRules autoHide = new AutoHideRules();

        public void validate() {
            if (general == null) general = new General();
            if (hud == null) hud = new HUD();
            if (appearance == null) appearance = new Appearance();
            if (thresholds == null) thresholds = new Thresholds();
            if (autoHide == null) autoHide = new AutoHideRules();
        }
    }

    public Set<String> getProfileNames() {
        return configProfiles != null ? configProfiles.keySet() : Set.of();
    }

    public void saveProfile(String name) {
        saveProfile(name, this);
    }

    public void saveProfile(String name, ModConfig sourceConfig) {
        String sanitizedName = sanitizeProfileName(name);
        if (sanitizedName.isEmpty()) {
            return;
        }

        if (configProfiles == null) {
            configProfiles = new LinkedHashMap<>();
        }

        ConfigProfile profile = createProfileFrom(sourceConfig == null ? this : sourceConfig);
        configProfiles.put(sanitizedName, profile);
        selectedProfile = sanitizedName;
    }

    public void renameProfile(String oldName, String newName) {
        String sanitizedOld = sanitizeProfileName(oldName);
        String sanitizedNew = sanitizeProfileName(newName);
        if (sanitizedOld.isEmpty() || sanitizedNew.isEmpty() || configProfiles == null || !configProfiles.containsKey(sanitizedOld)) {
            return;
        }

        ConfigProfile profile = configProfiles.remove(sanitizedOld);
        configProfiles.put(sanitizedNew, profile);
        if (sanitizedOld.equals(selectedProfile)) {
            selectedProfile = sanitizedNew;
        }
    }

    public void deleteProfile(String name) {
        String sanitizedName = sanitizeProfileName(name);
        if (sanitizedName.isEmpty() || configProfiles == null) {
            return;
        }

        configProfiles.remove(sanitizedName);
        if (sanitizedName.equals(selectedProfile)) {
            selectedProfile = configProfiles.containsKey("Default") ? "Default" : (configProfiles.isEmpty() ? "" : configProfiles.keySet().iterator().next());
        }
    }

    public void applyProfile(String name) {
        String sanitizedName = sanitizeProfileName(name);
        if (configProfiles == null) {
            selectedProfile = "";
            return;
        }

        ConfigProfile profile = configProfiles.get(sanitizedName);
        if (profile == null) {
            selectedProfile = "";
            return;
        }

        profile.validate();
        general = copySection(profile.general, General.class);
        hud = copySection(profile.hud, HUD.class);
        appearance = copySection(profile.appearance, Appearance.class);
        thresholds = copySection(profile.thresholds, Thresholds.class);
        autoHide = copySection(profile.autoHide, AutoHideRules.class);
        selectedProfile = sanitizedName;
        validate();
    }

    public static ConfigProfile createProfileFrom(ModConfig sourceConfig) {
        ConfigProfile profile = new ConfigProfile();
        profile.general = copySection(sourceConfig.general, General.class);
        profile.hud = copySection(sourceConfig.hud, HUD.class);
        profile.appearance = copySection(sourceConfig.appearance, Appearance.class);
        profile.thresholds = copySection(sourceConfig.thresholds, Thresholds.class);
        profile.autoHide = copySection(sourceConfig.autoHide, AutoHideRules.class);
        profile.validate();
        return profile;
    }

    private static <T> T copySection(T source, Class<T> sectionClass) {
        if (source == null) {
            try {
                return sectionClass.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to instantiate config section " + sectionClass.getSimpleName(), e);
            }
        }
        T copy = GSON.fromJson(GSON.toJson(source), sectionClass);
        if (copy != null) {
            return copy;
        }
        try {
            return sectionClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to copy config section " + sectionClass.getSimpleName(), e);
        }
    }

    public static String sanitizeProfileName(String name) {
        return name == null ? "" : name.trim();
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

        if (thresholds == null) {
            thresholds = new Thresholds();
        }
        thresholds.fpsGood = Math.max(1, Math.min(1000, thresholds.fpsGood));
        thresholds.fpsWarning = Math.max(1, Math.min(thresholds.fpsGood, thresholds.fpsWarning));

        thresholds.frameTimeGood = Math.max(0.1, Math.min(500.0, thresholds.frameTimeGood));
        thresholds.frameTimeWarning = Math.max(thresholds.frameTimeGood, Math.min(1000.0, thresholds.frameTimeWarning));

        thresholds.memoryGood = Math.max(1, Math.min(100, thresholds.memoryGood));
        thresholds.memoryWarning = Math.max(thresholds.memoryGood, Math.min(100, thresholds.memoryWarning));

        thresholds.pingGood = Math.max(0, Math.min(2000, thresholds.pingGood));
        thresholds.pingWarning = Math.max(thresholds.pingGood, Math.min(5000, thresholds.pingWarning));

        thresholds.tpsGood = Math.max(0.1, Math.min(20.0, thresholds.tpsGood));
        thresholds.tpsWarning = Math.max(0.1, Math.min(thresholds.tpsGood, thresholds.tpsWarning));

        if (autoHide == null) {
            autoHide = new AutoHideRules();
        }
        autoHide.fadeDelaySeconds = Math.max(1, Math.min(600, autoHide.fadeDelaySeconds));
        autoHide.fadeOpacity = Math.max(0.0f, Math.min(1.0f, autoHide.fadeOpacity));
        appearance.autoHideF3 = autoHide.hideWithF3;

        if (selectedProfile == null) {
            selectedProfile = "";
        }
        if (configProfiles == null) {
            configProfiles = new LinkedHashMap<>();
        }
        Map<String, ConfigProfile> sanitizedProfiles = new LinkedHashMap<>();
        for (Map.Entry<String, ConfigProfile> entry : configProfiles.entrySet()) {
            String name = sanitizeProfileName(entry.getKey());
            ConfigProfile profile = entry.getValue();
            if (name.isEmpty() || profile == null) {
                continue;
            }
            profile.validate();
            sanitizedProfiles.put(name, profile);
        }
        configProfiles = sanitizedProfiles;
        selectedProfile = sanitizeProfileName(selectedProfile);
        if (!selectedProfile.isEmpty() && !configProfiles.containsKey(selectedProfile)) {
            selectedProfile = configProfiles.containsKey("Default") ? "Default" : (configProfiles.isEmpty() ? "" : configProfiles.keySet().iterator().next());
        }

        if (general.configVersion < 6) {
            // CPU and GPU metrics removed in v5.1
            hud.metricOrder.remove("cpu");
            hud.metricOrder.remove("gpu");
            general.configVersion = 6;
        }

        if (hud.metricOrder == null || hud.metricOrder.isEmpty()) {
            hud.metricOrder = new CopyOnWriteArrayList<>(OverlayMetric.defaultOrderIds());
        } else {
            List<String> sanitized = new ArrayList<>();
            for (OverlayMetric metric : OverlayMetric.sanitizeOrder(hud.metricOrder)) {
                sanitized.add(metric.getId());
            }
            if (!(hud.metricOrder instanceof CopyOnWriteArrayList) || !hud.metricOrder.equals(sanitized)) {
                hud.metricOrder = new CopyOnWriteArrayList<>(sanitized);
            }
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
        this.thresholds = new Thresholds();
        this.autoHide = new AutoHideRules();
        this.selectedProfile = "Default";
        this.configProfiles = new LinkedHashMap<>();
        this.configProfiles.put("Default", createProfileFrom(this));
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
                LOGGER.error("Failed to load FPS Overlay config, reverting to defaults.", e);
                loadedConfig = new ModConfig();
            }
        } else {
            loadedConfig = new ModConfig();
        }

        loadedConfig.migrate();
        loadedConfig.validate();
        save(file, loadedConfig);
        return loadedConfig;
    }

    private void migrate() {
        if (this.general == null) return;

        if (this.general.configVersion < 5) {
            LOGGER.info("Migrating config from version {} to 5", this.general.configVersion);
            // Ensure all current metrics exist in the list during migration
            if (this.hud != null && this.hud.metricOrder != null) {
                for (OverlayMetric metric : OverlayMetric.values()) {
                    if (!this.hud.metricOrder.contains(metric.getId())) {
                        this.hud.metricOrder.add(metric.getId());
                    }
                }
            }
            this.general.configVersion = 5;
        }

        if (this.general.configVersion < 7) {
            LOGGER.info("Migrating config from version {} to 7", this.general.configVersion);
            if (this.thresholds == null) {
                this.thresholds = new Thresholds();
            }
            this.general.configVersion = 7;
        }

        if (this.general.configVersion < 8) {
            LOGGER.info("Migrating config from version {} to 8", this.general.configVersion);
            if (this.autoHide == null) {
                this.autoHide = new AutoHideRules();
            }
            if (this.appearance != null) {
                this.autoHide.hideWithF3 = this.appearance.autoHideF3;
            }
            this.general.configVersion = 8;
        }

        if (this.general.configVersion < 9) {
            LOGGER.info("Migrating config from version {} to 9", this.general.configVersion);
            if (this.selectedProfile == null || this.selectedProfile.trim().isEmpty()) {
                this.selectedProfile = "Default";
            }
            if (this.configProfiles == null) {
                this.configProfiles = new LinkedHashMap<>();
            }
            if (!this.configProfiles.containsKey("Default")) {
                this.configProfiles.put("Default", createProfileFrom(this));
            }
            this.general.configVersion = 9;
        }
    }

    public static void save(File file, ModConfig config) {
        if (config != null && file != null) {
            config.validate();
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(config, writer);
            } catch (IOException e) {
                LOGGER.error("Failed to save FPS Overlay config!", e);
            }
        }
    }
}
