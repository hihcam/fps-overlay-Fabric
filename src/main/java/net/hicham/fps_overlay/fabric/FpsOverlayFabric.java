package net.hicham.fps_overlay.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.hicham.fps_overlay.FpsOverlayMod;
import net.hicham.fps_overlay.OverlayMetric;
import net.hicham.fps_overlay.OverlayRenderer;
import net.hicham.fps_overlay.ServerTickMetrics;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class FpsOverlayFabric implements ClientModInitializer {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category
            .register(Identifier.fromNamespaceAndPath(FpsOverlayMod.MOD_ID, "keys"));
    private final List<KeyMappingAction> keyMappingActions = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        FpsOverlayMod.init();
        registerKeyMappings();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FpsOverlayMod.onClientTick(client);

            if (FpsOverlayMod.getConfig() != null && FpsOverlayMod.getConfig().general.enableKeybindings) {
                handleKeyMappings();
            }
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(FpsOverlayMod.MOD_ID, "overlay"),
                (guiGraphics, deltaTracker) -> {
                    if (!FpsOverlayMod.shouldRenderOverlay()) {
                        return;
                    }

                    Minecraft client = Minecraft.getInstance();
                    if (client == null || client.font == null) {
                        return;
                    }

                    OverlayRenderer.render(guiGraphics, client);
                }
        );

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerTickMetrics.onJoinServer(handler.getConnection());
        });
    }

    private void registerKeyMappings() {
        if (!keyMappingActions.isEmpty()) {
            return;
        }

        registerKeyMapping("toggle_overlay", InputConstants.KEY_O, FpsOverlayMod::toggleOverlay);
        registerKeyMapping("toggle_fps", InputConstants.KEY_F8, () -> FpsOverlayMod.toggleMetric(OverlayMetric.FPS));
        registerKeyMapping("toggle_frame_time", InputConstants.KEY_F9,
                () -> FpsOverlayMod.toggleMetric(OverlayMetric.FRAME_TIME));
        registerKeyMapping("toggle_memory", InputConstants.KEY_F10, () -> FpsOverlayMod.toggleMetric(OverlayMetric.MEMORY));
        registerKeyMapping("toggle_ping", InputConstants.KEY_F11, () -> FpsOverlayMod.toggleMetric(OverlayMetric.PING));
        registerKeyMapping("toggle_coords", InputConstants.KEY_F7, () -> FpsOverlayMod.toggleMetric(OverlayMetric.COORDS));
        registerKeyMapping("toggle_graph", InputConstants.KEY_F5, FpsOverlayMod::toggleGraph);
        registerKeyMapping("open_config", InputConstants.KEY_P, () -> FpsOverlayMod.openConfig(Minecraft.getInstance()));
        registerKeyMapping("open_position_editor", InputConstants.KEY_F6,
                () -> FpsOverlayMod.openPositionEditor(Minecraft.getInstance()));
        registerKeyMapping("reset_stats", InputConstants.KEY_F4, FpsOverlayMod::resetStatistics);
    }

    private void registerKeyMapping(String id, int defaultKey, Runnable action) {
        KeyMapping binding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fps_overlay." + id,
                InputConstants.Type.KEYBOARD,
                defaultKey,
                KEY_CATEGORY));
        keyMappingActions.add(new KeyMappingAction(binding, action));
        FpsOverlayMod.registerKeyMapping(binding);
    }

    private void handleKeyMappings() {
        for (KeyMappingAction keyMappingAction : keyMappingActions) {
            while (keyMappingAction.binding().consumeClick()) {
                try {
                    keyMappingAction.action().run();
                } catch (Exception e) {
                    FpsOverlayMod.LOGGER.error("Error handling keybinding {}", keyMappingAction.binding().getName(), e);
                }
            }
        }
    }

    private record KeyMappingAction(KeyMapping binding, Runnable action) {
    }
}
