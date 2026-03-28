package net.hicham.fps_overlay.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.hicham.fps_overlay.FpsOverlayMod;
import net.hicham.fps_overlay.OverlayMetric;
import net.hicham.fps_overlay.OverlayRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class FpsOverlayFabric implements ClientModInitializer {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category
            .register(Identifier.fromNamespaceAndPath(FpsOverlayMod.MOD_ID, "keys"));
    private final List<KeyBindingAction> keyBindingActions = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        FpsOverlayMod.init();
        registerKeyBindings();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FpsOverlayMod.onClientTick(client);

            if (FpsOverlayMod.getConfig() != null && FpsOverlayMod.getConfig().general.enableKeybindings) {
                handleKeyBindings();
            }
        });

        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            if (!FpsOverlayMod.shouldRenderOverlay()) {
                return;
            }

            Minecraft client = Minecraft.getInstance();
            if (client == null || client.font == null) {
                return;
            }

            OverlayRenderer.render(guiGraphics, client);
        });
    }

    private void registerKeyBindings() {
        if (!keyBindingActions.isEmpty()) {
            return;
        }

        registerKeyBinding("toggle_overlay", GLFW.GLFW_KEY_O, FpsOverlayMod::toggleOverlay);
        registerKeyBinding("toggle_fps", GLFW.GLFW_KEY_F8, () -> FpsOverlayMod.toggleMetric(OverlayMetric.FPS));
        registerKeyBinding("toggle_frame_time", GLFW.GLFW_KEY_F9,
                () -> FpsOverlayMod.toggleMetric(OverlayMetric.FRAME_TIME));
        registerKeyBinding("toggle_memory", GLFW.GLFW_KEY_F10, () -> FpsOverlayMod.toggleMetric(OverlayMetric.MEMORY));
        registerKeyBinding("toggle_ping", GLFW.GLFW_KEY_F11, () -> FpsOverlayMod.toggleMetric(OverlayMetric.PING));
        registerKeyBinding("toggle_coords", GLFW.GLFW_KEY_F7, () -> FpsOverlayMod.toggleMetric(OverlayMetric.COORDS));
        registerKeyBinding("toggle_graph", GLFW.GLFW_KEY_F5, FpsOverlayMod::toggleGraph);
        registerKeyBinding("open_config", GLFW.GLFW_KEY_P, () -> FpsOverlayMod.openConfig(Minecraft.getInstance()));
        registerKeyBinding("open_position_editor", GLFW.GLFW_KEY_F6,
                () -> FpsOverlayMod.openPositionEditor(Minecraft.getInstance()));
        registerKeyBinding("reset_stats", GLFW.GLFW_KEY_F4, FpsOverlayMod::resetStatistics);
    }

    private void registerKeyBinding(String id, int defaultKey, Runnable action) {
        KeyMapping binding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.fps_overlay." + id,
                InputConstants.Type.KEYSYM,
                defaultKey,
                KEY_CATEGORY));
        keyBindingActions.add(new KeyBindingAction(binding, action));
    }

    private void handleKeyBindings() {
        for (KeyBindingAction keyBindingAction : keyBindingActions) {
            while (keyBindingAction.binding().consumeClick()) {
                try {
                    keyBindingAction.action().run();
                } catch (Exception e) {
                    FpsOverlayMod.LOGGER.error("Error handling keybinding {}", keyBindingAction.binding().getName(), e);
                }
            }
        }
    }

    private record KeyBindingAction(KeyMapping binding, Runnable action) {
    }
}
