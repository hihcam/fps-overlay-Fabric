package net.hicham.fps_overlay;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigManager {
    private static final Logger LOGGER = LogManager.getLogger("fps_overlay/ConfigManager");
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    @SuppressWarnings("null")
    public static final Event<ConfigChangedCallback> CONFIG_CHANGED = EventFactory.createArrayBacked(
            ConfigChangedCallback.class,
            (listeners) -> () -> {
                for (ConfigChangedCallback listener : listeners) {
                    try {
                        listener.onConfigChanged();
                    } catch (Exception e) {
                        LOGGER.error("Error in config change listener", e);
                    }
                }
            }
    );

    @FunctionalInterface
    public interface ConfigChangedCallback {
        void onConfigChanged();
    }

    public static void initialize() {
        if (initialized.get()) {
            return;
        }

        try {
            Path configDir = Paths.get("config", "fps_overlay");
            File configFile = configDir.resolve("config.json").toFile();
            
            if (!configDir.toFile().exists()) {
                configDir.toFile().mkdirs();
            }

            ModConfig.init(configFile);
            initialized.set(true);
            LOGGER.info("Configuration manager initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize configuration", e);
            initialized.set(true);
        }
    }

    public static ModConfig getConfig() {
        ensureInitialized();
        return ModConfig.get();
    }

    public static void saveConfig() {
        ensureInitialized();
        ModConfig.save();
        CONFIG_CHANGED.invoker().onConfigChanged();
    }

    public static void resetToDefaults() {
        ensureInitialized();
        ModConfig.get().resetToDefaults();
        saveConfig();
        LOGGER.info("Configuration reset to defaults");
    }

    public static void registerConfigListener(ConfigChangedCallback listener) {
        CONFIG_CHANGED.register(listener);
    }

    private static void ensureInitialized() {
        if (!initialized.get()) {
            LOGGER.warn("ConfigManager accessed before initialization, initializing now...");
            initialize();
        }
    }

    public static boolean isInitialized() {
        return initialized.get();
    }

    public static void cleanup() {
        initialized.set(false);
    }
}