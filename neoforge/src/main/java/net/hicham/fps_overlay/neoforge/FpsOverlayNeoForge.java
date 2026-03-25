package net.hicham.fps_overlay.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import net.hicham.fps_overlay.FpsOverlayMod;
import net.hicham.fps_overlay.OverlayMetric;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod(FpsOverlayMod.MOD_ID)
public class FpsOverlayNeoForge {
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category
            .register(Identifier.fromNamespaceAndPath(FpsOverlayMod.MOD_ID, "keys"));
    private final List<KeyBindingAction> keyBindingActions = new ArrayList<>();

    public FpsOverlayNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            FpsOverlayMod.init();
            registerKeyBindings();
            modEventBus.addListener(this::onRegisterKeyMappings);
            NeoForge.EVENT_BUS.addListener(this::onClientTick);
            NeoForge.EVENT_BUS.addListener(this::onRenderGui);

            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (client, parent) -> net.hicham.fps_overlay.ConfigScreenFactory.createConfigScreen(parent));
        }
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(KEY_CATEGORY);
        for (KeyBindingAction keyBindingAction : keyBindingActions) {
            event.register(keyBindingAction.binding());
        }
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        FpsOverlayMod.onClientTick(client);

        if (FpsOverlayMod.getConfig() != null && FpsOverlayMod.getConfig().general.enableKeybindings) {
            handleKeyBindings();
        }
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        try {
            if (!FpsOverlayMod.shouldRenderOverlay()) {
                return;
            }

            Minecraft client = Minecraft.getInstance();
            if (client == null || client.font == null) {
                return;
            }

            net.hicham.fps_overlay.OverlayRenderer.render(event.getGuiGraphics(), client);
        } catch (Exception e) {
            FpsOverlayMod.LOGGER.error("Failed to render NeoForge HUD", e);
        }
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
        KeyMapping binding = new KeyMapping(
                "key.fps_overlay." + id,
                InputConstants.Type.KEYSYM,
                defaultKey,
                KEY_CATEGORY);
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
