package net.hicham.fps_overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PositionEditorScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    private boolean dragging;
    private double previewScreenX;
    private double previewScreenY;

    public PositionEditorScreen(Screen parent, ModConfig config) {
        super(Component.translatable("screen.fps_overlay.position_editor"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        int footerY = this.height - 28;

        addRenderableWidget(CycleButton.<ModConfig.OverlayPosition>builder(ModConfig.OverlayPosition::getDisplayText,
                        config.appearance.position)
                .withValues(ModConfig.OverlayPosition.values())
                .create(20, 20, 160, 20, Component.translatable("option.fps_overlay.position"),
                        (button, value) -> config.appearance.position = value));

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.reset_offset"), button -> {
            config.appearance.xOffset = 0;
            config.appearance.yOffset = 0;
        }).bounds(190, 20, 110, 20).tooltip(Tooltip.create(Component.translatable("tooltip.fps_overlay.resetOffset")))
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 - 75, footerY, 150, 20).build());
    }

    @Override
    public void onClose() {
        ConfigManager.saveConfig();
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        boolean handledByWidget = super.mouseClicked(click, doubled);
        if (handledByWidget) {
            return true;
        }

        if (click.button() == 0) {
            OverlayRenderer.LayoutBounds bounds = OverlayRenderer.getPreviewBounds(this.width, this.height, config);
            if (bounds.contains(click.x(), click.y())) {
                dragging = true;
                previewScreenX = bounds.x();
                previewScreenY = bounds.y();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (dragging && click.button() == 0) {
            previewScreenX += deltaX;
            previewScreenY += deltaY;

            OverlayRenderer.LayoutBounds screenBounds = OverlayRenderer.getPreviewBounds(this.width, this.height, config);
            double maxX = Math.max(0, this.width - screenBounds.width());
            double maxY = Math.max(0, this.height - screenBounds.height());
            previewScreenX = Math.max(0, Math.min(previewScreenX, maxX));
            previewScreenY = Math.max(0, Math.min(previewScreenY, maxY));

            float scale = config.appearance.hudScale;
            OverlayRenderer.LayoutBounds logicalBounds =
                    OverlayRenderer.getPreviewLogicalBounds(this.width, this.height, config);
            int logicalWidth = Math.max(1, Math.round(this.width / scale));
            int logicalHeight = Math.max(1, Math.round(this.height / scale));
            OverlayRenderer.AnchorPoint anchor = OverlayRenderer.getAnchorPoint(
                    logicalWidth, logicalHeight, logicalBounds.width(), logicalBounds.height(), config.appearance.position);

            int targetLogicalX = (int) Math.round(previewScreenX / scale);
            int targetLogicalY = (int) Math.round(previewScreenY / scale);
            config.appearance.xOffset = targetLogicalX - anchor.x();
            config.appearance.yOffset = targetLogicalY - anchor.y();
            return true;
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == 0) {
            dragging = false;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xC0182028, 0xD010141A);
        OverlayRenderer.renderPreview(guiGraphics, minecraft, config, this.width, this.height);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(font, title, this.width / 2, 48, 0xFFFFFFFF);
        guiGraphics.drawCenteredString(font, Component.translatable("text.fps_overlay.position_editor_hint"),
                this.width / 2, 62, 0xFFB7C6D1);
    }
}
