package net.hicham.fps_overlay;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfigHubScreen extends Screen {
    private final Screen parent;

    public ConfigHubScreen(Screen parent) {
        super(Text.translatable("screen.fps_overlay.config_hub"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 48;
        int buttonWidth = 180;
        int buttonHeight = 20;
        int gap = 24;

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.fps_overlay.open_settings"), button -> {
            if (client != null) {
                client.setScreen(ConfigScreenFactory.createSettingsScreen(this));
            }
        }).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.fps_overlay.edit_position"), button -> {
            if (client != null) {
                client.setScreen(new PositionEditorScreen(this, ConfigManager.getConfig()));
            }
        }).dimensions(centerX - buttonWidth / 2, startY + gap, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.fps_overlay.arrange_metrics"), button -> {
            if (client != null) {
                client.setScreen(new MetricOrderScreen(this, ConfigManager.getConfig()));
            }
        }).dimensions(centerX - buttonWidth / 2, startY + (gap * 2), buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
                .dimensions(centerX - buttonWidth / 2, startY + (gap * 4), buttonWidth, buttonHeight).build());
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xC0182028, 0xD010141A);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, this.width / 2, this.height / 2 - 84, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("text.fps_overlay.config_hub_hint"),
                this.width / 2, this.height / 2 - 68, 0xFFB7C6D1);
    }
}
