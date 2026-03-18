package net.hicham.fps_overlay;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;

import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.concurrent.atomic.AtomicBoolean;

public class FpsOverlayMod implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("FpsOverlay");
    private ModConfig config;

    private KeyBinding[] keyBindings;

    // State management
    private final AtomicBoolean modInitialized = new AtomicBoolean(false);
    private final Object INIT_LOCK = new Object();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Fps Overlay...");

        synchronized (INIT_LOCK) {
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
        if (!modInitialized.get())
            return;

        LOGGER.debug("Configuration changed - reloading settings");
        try {
            config = ConfigManager.getConfig();

            PerformanceTracker.getInstance().setConfig(config);
            OverlayRenderer.setConfig(config);

            // Re-register keybindings if enableKeybindings changed
            if (keyBindings != null && !config.general.enableKeybindings) {
                keyBindings = null;
            } else if (keyBindings == null && config.general.enableKeybindings) {
                registerKeyBindings();
            }

            if (!config.hud.showAverageFps) {
                PerformanceTracker.getInstance().clearAverageFpsData();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to handle config change", e);
        }
    }

    private void registerKeyBindings() {
        LOGGER.debug("Keybindings registration skipped (not implemented)");
    }

    @SuppressWarnings("deprecation")
    private void registerEventListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!modInitialized.get() || client.player == null)
                return;

            PerformanceTracker.getInstance().update(client);

            if (config.general.enableKeybindings && keyBindings != null) {
                handleKeyBindings(client);
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!shouldRenderOverlay())
                return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.textRenderer == null)
                return;

            OverlayRenderer.render(drawContext, client);
        });
    }

    private boolean shouldRenderOverlay() {
        MinecraftClient client = MinecraftClient.getInstance();
        return modInitialized.get() &&
                config != null &&
                config.general.enabled &&
                client != null &&
                client.player != null &&
                client.world != null &&
                client.textRenderer != null &&
                !client.options.hudHidden;
    }

    private void handleKeyBindings(MinecraftClient client) {
        if (keyBindings == null || client.player == null)
            return;

        try {
            if (keyBindings[0].wasPressed())
                toggleOverlay();
            if (keyBindings[1].wasPressed())
                toggleFps();
            if (keyBindings[2].wasPressed())
                toggleMemory();
            if (keyBindings[3].wasPressed())
                togglePing();
            if (keyBindings[4].wasPressed())
                openConfig(client);
            if (keyBindings[5].wasPressed())
                resetStatistics(client);
            if (keyBindings[6].wasPressed())
                toggleAverageFps();
        } catch (Exception e) {
            LOGGER.error("Error handling keybindings", e);
        }
    }

    private void toggleOverlay() {
        config.general.enabled = !config.general.enabled;
        ConfigManager.saveConfig();
        sendToggleMessage("Overlay", config.general.enabled);
    }

    private void toggleFps() {
        config.hud.showFps = !config.hud.showFps;
        ConfigManager.saveConfig();
        sendToggleMessage("FPS Display", config.hud.showFps);
    }

    private void toggleMemory() {
        config.hud.showMemory = !config.hud.showMemory;
        ConfigManager.saveConfig();
        sendToggleMessage("Memory Display", config.hud.showMemory);
    }

    private void togglePing() {
        config.hud.showPing = !config.hud.showPing;
        ConfigManager.saveConfig();
        sendToggleMessage("Ping Display", config.hud.showPing);
    }

    private void toggleAverageFps() {
        config.hud.showAverageFps = !config.hud.showAverageFps;
        ConfigManager.saveConfig();
        sendToggleMessage("Average FPS Display", config.hud.showAverageFps);

        if (!config.hud.showAverageFps) {
            PerformanceTracker.getInstance().clearAverageFpsData();
        }
    }

    private void openConfig(MinecraftClient client) {
        if (client != null) {
            try {
                Screen configScreen = ConfigScreenFactory.createConfigScreen(client.currentScreen);
                client.setScreen(configScreen);
            } catch (Exception e) {
                LOGGER.error("Failed to open config screen", e);
            }
        }
    }

    private void resetStatistics(MinecraftClient client) {
        PerformanceTracker.getInstance().clearAverageFpsData();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§7[§6FpsOverlay§7] §fStatistics reset"), false);
        }
    }

    @SuppressWarnings("null")
    private void sendToggleMessage(String feature, boolean enabled) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            String status = enabled ? "§aENABLED" : "§cDISABLED";
            String message = String.format("§7[§6FpsOverlay§7] §f%s §7is now %s", feature, status);
            client.player.sendMessage(Text.literal(message), true);
        }
    }
}
