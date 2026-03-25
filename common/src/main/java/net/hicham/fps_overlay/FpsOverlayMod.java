package net.hicham.fps_overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class FpsOverlayMod {
    public static final String MOD_ID = "fps_overlay";
    public static final Logger LOGGER = LogManager.getLogger("FpsOverlay");

    private static final AtomicBoolean MOD_INITIALIZED = new AtomicBoolean(false);
    private static final Object INIT_LOCK = new Object();

    private static ModConfig config;

    public static void init() {
        LOGGER.info("Initializing Fps Overlay...");

        synchronized (INIT_LOCK) {
            if (MOD_INITIALIZED.get()) {
                LOGGER.warn("Mod already initialized");
                return;
            }

            try {
                ConfigManager.initialize();
                config = ConfigManager.getConfig();

                PerformanceTracker.getInstance().setConfig(config);
                OverlayRenderer.setConfig(config);
                ConfigManager.registerConfigListener(FpsOverlayMod::onConfigChanged);

                MOD_INITIALIZED.set(true);
                LOGGER.info("Fps Overlay initialized successfully");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Fps Overlay", e);
                MOD_INITIALIZED.set(false);
            }
        }
    }

    public static boolean shouldRenderOverlay() {
        Minecraft client = Minecraft.getInstance();
        return MOD_INITIALIZED.get()
                && config != null
                && config.general.enabled
                && client != null
                && client.player != null
                && client.level != null
                && client.font != null
                && !client.options.hideGui;
    }

    public static void onClientTick(Minecraft client) {
        if (!MOD_INITIALIZED.get() || client.player == null) {
            return;
        }

        PerformanceTracker.getInstance().update(client);
    }

    public static ModConfig getConfig() {
        return config;
    }

    public static void toggleOverlay() {
        if (config == null) {
            return;
        }

        config.general.enabled = !config.general.enabled;
        ConfigManager.saveConfig();
        sendInfoMessage("Overlay " + enabledText(config.general.enabled));
    }

    public static void toggleMetric(OverlayMetric metric) {
        if (config == null) {
            return;
        }

        boolean enabled = !config.hud.isMetricEnabled(metric);
        config.hud.setMetricEnabled(metric, enabled);
        ConfigManager.saveConfig();
        sendInfoMessage(Component.translatable(metric.getLabelKey()).getString() + " " + enabledText(enabled));
    }

    public static void toggleGraph() {
        if (config == null) {
            return;
        }

        config.hud.showGraph = !config.hud.showGraph;
        ConfigManager.saveConfig();
        sendInfoMessage("Graph " + enabledText(config.hud.showGraph));
    }

    public static void openConfig(Minecraft client) {
        if (client == null) {
            return;
        }

        try {
            Screen configScreen = ConfigScreenFactory.createConfigScreen(client.screen);
            client.setScreen(configScreen);
        } catch (Exception e) {
            LOGGER.error("Failed to open config screen", e);
        }
    }

    public static void openPositionEditor(Minecraft client) {
        if (client == null) {
            return;
        }

        client.setScreen(new PositionEditorScreen(client.screen, ConfigManager.getConfig()));
    }

    public static void resetStatistics() {
        PerformanceTracker.getInstance().resetSessionStats();
        sendInfoMessage("Session statistics reset");
    }

    private static void onConfigChanged() {
        if (!MOD_INITIALIZED.get()) {
            return;
        }

        config = ConfigManager.getConfig();
        PerformanceTracker.getInstance().setConfig(config);
        OverlayRenderer.setConfig(config);

        if (!config.hud.showAverageFps) {
            PerformanceTracker.getInstance().clearAverageFpsData();
        }
    }

    private static void sendInfoMessage(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.player != null) {
            client.player.displayClientMessage(Component.literal("[FpsOverlay] " + message), true);
        }
    }

    private static String enabledText(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }
}
