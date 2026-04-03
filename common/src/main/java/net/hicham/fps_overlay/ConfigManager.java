package net.hicham.fps_overlay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigManager {
    private static final Logger LOGGER = LogManager.getLogger("fps_overlay/ConfigManager");
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private static File configFile;
    private static ModConfig config;

    @FunctionalInterface
    public interface ConfigChangedCallback {
        void onConfigChanged();
    }

    private static final List<ConfigChangedCallback> LISTENERS = new CopyOnWriteArrayList<>();

    public static void initialize() {
        if (INITIALIZED.get()) {
            return;
        }

        try {
            Path configDir = Paths.get("config", "fps_overlay");
            File resolvedConfigFile = configDir.resolve("config.json").toFile();

            if (!configDir.toFile().exists()) {
                configDir.toFile().mkdirs();
            }

            configFile = resolvedConfigFile;
            config = ModConfig.load(configFile);
            INITIALIZED.set(true);
            LOGGER.info("Configuration manager initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize configuration", e);
            INITIALIZED.set(true);
        }
    }

    public static ModConfig getConfig() {
        ensureInitialized();
        if (config == null) {
            config = new ModConfig();
        }
        return config;
    }

    public static void saveConfig() {
        ensureInitialized();
        ModConfig.save(configFile, getConfig());
        notifyListeners();
    }

    public static void resetToDefaults() {
        ensureInitialized();
        getConfig().resetToDefaults();
        saveConfig();
        LOGGER.info("Configuration reset to defaults");
    }

    public static void registerConfigListener(ConfigChangedCallback listener) {
        LISTENERS.add(listener);
    }

    public static void unregisterConfigListener(ConfigChangedCallback listener) {
        LISTENERS.remove(listener);
    }

    public static boolean isInitialized() {
        return INITIALIZED.get();
    }

    public static void cleanup() {
        INITIALIZED.set(false);
        config = null;
        configFile = null;
        LISTENERS.clear();
    }

    private static void notifyListeners() {
        for (ConfigChangedCallback listener : LISTENERS) {
            try {
                listener.onConfigChanged();
            } catch (Exception e) {
                LOGGER.error("Error in config change listener", e);
            }
        }
    }

    private static void ensureInitialized() {
        if (!INITIALIZED.get()) {
            LOGGER.warn("ConfigManager accessed before initialization, initializing now...");
            initialize();
        }
    }
}
