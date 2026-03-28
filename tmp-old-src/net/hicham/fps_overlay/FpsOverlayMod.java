package net.hicham.fps_overlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FpsOverlayMod implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("FpsOverlay");
    private static final class_304.class_11900 KEY_CATEGORY = class_304.class_11900
            .method_74698(class_2960.method_60655("fps_overlay", "keys"));

    private final AtomicBoolean modInitialized = new AtomicBoolean(false);
    private final Object initLock = new Object();
    private final List<KeyBindingAction> keyBindingActions = new ArrayList<>();

    private ModConfig config;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Fps Overlay...");

        synchronized (initLock) {
            if (modInitialized.get()) {
                LOGGER.warn("Mod already initialized");
                return;
            }

            try {
                ConfigManager.initialize();
                config = ConfigManager.getConfig();

                PerformanceTracker.getInstance().setConfig(config);
                OverlayRenderer.setConfig(config);

                registerKeyBindings();
                registerEventListeners();
                ConfigManager.registerConfigListener(this::onConfigChanged);

                modInitialized.set(true);
                LOGGER.info("Fps Overlay initialized successfully");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Fps Overlay", e);
                modInitialized.set(false);
            }
        }
    }

    private void onConfigChanged() {
        if (!modInitialized.get()) {
            return;
        }

        config = ConfigManager.getConfig();
        PerformanceTracker.getInstance().setConfig(config);
        OverlayRenderer.setConfig(config);

        if (!config.hud.showAverageFps) {
            PerformanceTracker.getInstance().clearAverageFpsData();
        }
    }

    private void registerKeyBindings() {
        if (!keyBindingActions.isEmpty()) {
            return;
        }

        registerKeyBinding("toggle_overlay", GLFW.GLFW_KEY_O, this::toggleOverlay);
        registerKeyBinding("toggle_fps", GLFW.GLFW_KEY_F8, () -> toggleMetric(OverlayMetric.FPS));
        registerKeyBinding("toggle_frame_time", GLFW.GLFW_KEY_F9, () -> toggleMetric(OverlayMetric.FRAME_TIME));
        registerKeyBinding("toggle_memory", GLFW.GLFW_KEY_F10, () -> toggleMetric(OverlayMetric.MEMORY));
        registerKeyBinding("toggle_ping", GLFW.GLFW_KEY_F11, () -> toggleMetric(OverlayMetric.PING));
        registerKeyBinding("toggle_coords", GLFW.GLFW_KEY_F7, () -> toggleMetric(OverlayMetric.COORDS));
        registerKeyBinding("toggle_graph", GLFW.GLFW_KEY_F5, () -> toggleBooleanMetric(
                () -> config.hud.showGraph,
                value -> config.hud.showGraph = value,
                "Graph"));
        registerKeyBinding("open_config", GLFW.GLFW_KEY_P, () -> openConfig(class_310.method_1551()));
        registerKeyBinding("open_position_editor", GLFW.GLFW_KEY_F6,
                () -> openPositionEditor(class_310.method_1551()));
        registerKeyBinding("reset_stats", GLFW.GLFW_KEY_F4, this::resetStatistics);
    }

    private void registerKeyBinding(String id, int defaultKey, Runnable action) {
        class_304 binding = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.fps_overlay." + id,
                class_3675.class_307.field_1668,
                defaultKey,
                KEY_CATEGORY));
        keyBindingActions.add(new KeyBindingAction(binding, action));
    }

    @SuppressWarnings("deprecation")
    private void registerEventListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!modInitialized.get()) {
                return;
            }

            if (client.field_1724 != null) {
                PerformanceTracker.getInstance().update(client);
            }

            if (config.general.enableKeybindings) {
                handleKeyBindings();
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (!shouldRenderOverlay()) {
                return;
            }

            class_310 client = class_310.method_1551();
            if (client == null || client.field_1772 == null) {
                return;
            }

            OverlayRenderer.render(drawContext, client);
        });
    }

    private boolean shouldRenderOverlay() {
        class_310 client = class_310.method_1551();
        return modInitialized.get()
                && config != null
                && config.general.enabled
                && client != null
                && client.field_1724 != null
                && client.field_1687 != null
                && client.field_1772 != null
                && !client.field_1690.field_1842;
    }

    private void handleKeyBindings() {
        for (KeyBindingAction keyBindingAction : keyBindingActions) {
            while (keyBindingAction.binding().method_1436()) {
                try {
                    keyBindingAction.action().run();
                } catch (Exception e) {
                    LOGGER.error("Error handling keybinding {}", keyBindingAction.binding().method_1431(), e);
                }
            }
        }
    }

    private void toggleOverlay() {
        config.general.enabled = !config.general.enabled;
        ConfigManager.saveConfig();
        sendInfoMessage("Overlay " + enabledText(config.general.enabled));
    }

    private void toggleMetric(OverlayMetric metric) {
        boolean enabled = !config.hud.isMetricEnabled(metric);
        config.hud.setMetricEnabled(metric, enabled);
        ConfigManager.saveConfig();
        sendInfoMessage(class_2561.method_43471(metric.getLabelKey()).getString() + " " + enabledText(enabled));
    }

    private void toggleBooleanMetric(BooleanSupplier getter, BooleanConsumer setter, String label) {
        boolean enabled = !getter.getAsBoolean();
        setter.accept(enabled);
        ConfigManager.saveConfig();
        sendInfoMessage(label + " " + enabledText(enabled));
    }

    private void openConfig(class_310 client) {
        if (client == null) {
            return;
        }

        try {
            class_437 configScreen = ConfigScreenFactory.createConfigScreen(client.field_1755);
            client.method_1507(configScreen);
        } catch (Exception e) {
            LOGGER.error("Failed to open config screen", e);
        }
    }

    private void openPositionEditor(class_310 client) {
        if (client == null) {
            return;
        }

        client.method_1507(new PositionEditorScreen(client.field_1755, ConfigManager.getConfig()));
    }

    private void resetStatistics() {
        PerformanceTracker.getInstance().resetSessionStats();
        sendInfoMessage("Session statistics reset");
    }

    private void sendInfoMessage(String message) {
        class_310 client = class_310.method_1551();
        if (client != null && client.field_1724 != null) {
            client.field_1724.method_7353(class_2561.method_43470("[FpsOverlay] " + message), true);
        }
    }

    private String enabledText(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }

    private record KeyBindingAction(class_304 binding, Runnable action) {
    }

    @FunctionalInterface
    private interface BooleanSupplier {
        boolean getAsBoolean();
    }

    @FunctionalInterface
    private interface BooleanConsumer {
        void accept(boolean value);
    }
}
