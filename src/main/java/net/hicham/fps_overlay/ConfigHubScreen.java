package net.hicham.fps_overlay;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigHubScreen extends Screen {
    private final Screen parent;

    public ConfigHubScreen(Screen parent) {
        super(Component.translatable("screen.fps_overlay.config_hub"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;
        int buttonWidth = 180;
        int buttonHeight = 20;
        int gap = 24;

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.open_settings"), button -> {
            if (minecraft != null) {
                minecraft.setScreenAndShow(ConfigScreenFactory.createSettingsScreen(this));
            }
        }).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.edit_position"), button -> {
            if (minecraft != null) {
                minecraft.setScreenAndShow(new PositionEditorScreen(this, ConfigManager.getConfig()));
            }
        }).bounds(centerX - buttonWidth / 2, startY + gap, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.arrange_metrics"), button -> {
            if (minecraft != null) {
                minecraft.setScreenAndShow(new MetricOrderScreen(this, ConfigManager.getConfig()));
            }
        }).bounds(centerX - buttonWidth / 2, startY + (gap * 2), buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.keybinds"), button -> {
            if (minecraft != null) {
                minecraft.setScreenAndShow(new KeybindSettingsScreen(this));
            }
        }).bounds(centerX - buttonWidth / 2, startY + (gap * 3), buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(centerX - buttonWidth / 2, startY + (gap * 4), buttonWidth, buttonHeight).build());
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xC0182028, 0xD010141A);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.text(font, title, this.width / 2 - font.width(title) / 2, this.height / 2 - 100, 0xFFFFFFFF, false);
        Component hint = Component.translatable("text.fps_overlay.config_hub_hint");
        guiGraphics.text(font, hint, this.width / 2 - font.width(hint) / 2, this.height / 2 - 84, 0xFFB7C6D1, false);
    }
}
