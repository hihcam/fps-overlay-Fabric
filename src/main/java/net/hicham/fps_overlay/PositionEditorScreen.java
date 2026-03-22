package net.hicham.fps_overlay;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class PositionEditorScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    private double previewScreenX;
    private double previewScreenY;
    private CyclingButtonWidget<ModConfig.OverlayPosition> anchorButton;

    public PositionEditorScreen(Screen parent, ModConfig config) {
        super(Text.translatable("screen.fps_overlay.position_editor"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        int footerY = this.height - 28;

        anchorButton = CyclingButtonWidget.<ModConfig.OverlayPosition>builder(ModConfig.OverlayPosition::getDisplayText,
                        config.appearance.position)
                .values(ModConfig.OverlayPosition.values())
                .build(20, 20, 160, 20, Text.translatable("option.fps_overlay.position"),
                        (button, value) -> config.appearance.position = value);
        addDrawableChild(anchorButton);

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.fps_overlay.reset_offset"), button -> {
            config.appearance.xOffset = 0;
            config.appearance.yOffset = 0;
        }).dimensions(190, 20, 110, 20).tooltip(Tooltip.of(Text.translatable("tooltip.fps_overlay.resetOffset")))
                .build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
                .dimensions(this.width / 2 - 75, footerY, 150, 20).build());
    }

    @Override
    public void close() {
        ConfigManager.saveConfig();
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            OverlayRenderer.LayoutBounds bounds = OverlayRenderer.getPreviewBounds(this.width, this.height, config);
            if (bounds.contains(click.x(), click.y())) {
                dragging = true;
                dragOffsetX = (int) click.x() - bounds.x();
                dragOffsetY = (int) click.y() - bounds.y();
                previewScreenX = bounds.x();
                previewScreenY = bounds.y();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging && click.button() == 0) {
            previewScreenX += deltaX;
            previewScreenY += deltaY;

            OverlayRenderer.LayoutBounds screenBounds = OverlayRenderer.getPreviewBounds(this.width, this.height, config);
            double maxX = Math.max(0, this.width - screenBounds.width());
            double maxY = Math.max(0, this.height - screenBounds.height());
            previewScreenX = Math.max(0, Math.min(previewScreenX, maxX));
            previewScreenY = Math.max(0, Math.min(previewScreenY, maxY));

            float scale = config.appearance.hudScale;
            OverlayRenderer.LayoutBounds logicalBounds = OverlayRenderer.getPreviewLogicalBounds(this.width, this.height, config);
            int logicalWidth = Math.max(1, Math.round(this.width / scale));
            int logicalHeight = Math.max(1, Math.round(this.height / scale));
            OverlayRenderer.AnchorPoint anchor = OverlayRenderer.getAnchorPoint(logicalWidth, logicalHeight,
                    logicalBounds.width(), logicalBounds.height(), config.appearance.position);

            int targetLogicalX = (int) Math.round(previewScreenX / scale);
            int targetLogicalY = (int) Math.round(previewScreenY / scale);
            config.appearance.xOffset = targetLogicalX - anchor.x();
            config.appearance.yOffset = targetLogicalY - anchor.y();
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) {
            dragging = false;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xC0182028, 0xD010141A);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, this.width / 2, 48, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("text.fps_overlay.position_editor_hint"),
                this.width / 2, 62, 0xFFB7C6D1);

        OverlayRenderer.renderPreview(context, client, config, this.width, this.height);
    }
}
