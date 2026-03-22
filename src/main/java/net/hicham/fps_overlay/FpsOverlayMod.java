package net.hicham.fps_overlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FpsOverlayMod implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("FpsOverlay");
    private static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category
            .create(Identifier.of("fps_overlay", "keys"));

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
        registerKeyBinding("open_config", GLFW.GLFW_KEY_P, () -> openConfig(MinecraftClient.getInstance()));
        registerKeyBinding("open_position_editor", GLFW.GLFW_KEY_F6,
                () -> openPositionEditor(MinecraftClient.getInstance()));
        registerKeyBinding("reset_stats", GLFW.GLFW_KEY_F4, this::resetStatistics);
    }

    private void registerKeyBinding(String id, int defaultKey, Runnable action) {
        KeyBinding binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fps_overlay." + id,
                InputUtil.Type.KEYSYM,
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

            if (client.player != null) {
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

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.textRenderer == null) {
                return;
            }

            OverlayRenderer.render(drawContext, client);
        });
    }

    private boolean shouldRenderOverlay() {
        MinecraftClient client = MinecraftClient.getInstance();
        return modInitialized.get()
                && config != null
                && config.general.enabled
                && client != null
                && client.player != null
                && client.world != null
                && client.textRenderer != null
                && !client.options.hudHidden;
    }

    private void handleKeyBindings() {
        for (KeyBindingAction keyBindingAction : keyBindingActions) {
            while (keyBindingAction.binding().wasPressed()) {
                try {
                    keyBindingAction.action().run();
                } catch (Exception e) {
                    LOGGER.error("Error handling keybinding {}", keyBindingAction.binding().getId(), e);
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
        sendInfoMessage(Text.translatable(metric.getLabelKey()).getString() + " " + enabledText(enabled));
    }

    private void toggleBooleanMetric(BooleanSupplier getter, BooleanConsumer setter, String label) {
        boolean enabled = !getter.getAsBoolean();
        setter.accept(enabled);
        ConfigManager.saveConfig();
        sendInfoMessage(label + " " + enabledText(enabled));
    }

    private void openConfig(MinecraftClient client) {
        if (client == null) {
            return;
        }

        try {
            Screen configScreen = ConfigScreenFactory.createConfigScreen(client.currentScreen);
            client.setScreen(configScreen);
        } catch (Exception e) {
            LOGGER.error("Failed to open config screen", e);
        }
    }

    private void openPositionEditor(MinecraftClient client) {
        if (client == null) {
            return;
        }

        client.setScreen(new PositionEditorScreen(client.currentScreen, ConfigManager.getConfig()));
    }

    private void resetStatistics() {
        PerformanceTracker.getInstance().resetSessionStats();
        sendInfoMessage("Session statistics reset");
    }

    private void sendInfoMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("[FpsOverlay] " + message), true);
        }
    }

    private String enabledText(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }

    private record KeyBindingAction(KeyBinding binding, Runnable action) {
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
