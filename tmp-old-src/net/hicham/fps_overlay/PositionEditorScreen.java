package net.hicham.fps_overlay;

import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5676;
import net.minecraft.class_7919;

public class PositionEditorScreen extends class_437 {
    private final class_437 parent;
    private final ModConfig config;

    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    private double previewScreenX;
    private double previewScreenY;
    private class_5676<ModConfig.OverlayPosition> anchorButton;

    public PositionEditorScreen(class_437 parent, ModConfig config) {
        super(class_2561.method_43471("screen.fps_overlay.position_editor"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void method_25426() {
        int footerY = this.field_22790 - 28;

        anchorButton = class_5676.<ModConfig.OverlayPosition>method_32606(ModConfig.OverlayPosition::getDisplayText,
                        config.appearance.position)
                .method_32624(ModConfig.OverlayPosition.values())
                .method_32617(20, 20, 160, 20, class_2561.method_43471("option.fps_overlay.position"),
                        (button, value) -> config.appearance.position = value);
        method_37063(anchorButton);

        method_37063(class_4185.method_46430(class_2561.method_43471("button.fps_overlay.reset_offset"), button -> {
            config.appearance.xOffset = 0;
            config.appearance.yOffset = 0;
        }).method_46434(190, 20, 110, 20).method_46436(class_7919.method_47407(class_2561.method_43471("tooltip.fps_overlay.resetOffset")))
                .method_46431());

        method_37063(class_4185.method_46430(class_2561.method_43471("gui.done"), button -> method_25419())
                .method_46434(this.field_22789 / 2 - 75, footerY, 150, 20).method_46431());
    }

    @Override
    public void method_25419() {
        ConfigManager.saveConfig();
        if (field_22787 != null) {
            field_22787.method_1507(parent);
        }
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        boolean handledByWidget = super.method_25402(click, doubled);
        if (handledByWidget) {
            return true;
        }

        if (click.method_74245() == 0) {
            OverlayRenderer.LayoutBounds bounds = OverlayRenderer.getPreviewBounds(this.field_22789, this.field_22790, config);
            if (bounds.contains(click.comp_4798(), click.comp_4799())) {
                dragging = true;
                dragOffsetX = (int) click.comp_4798() - bounds.x();
                dragOffsetY = (int) click.comp_4799() - bounds.y();
                previewScreenX = bounds.x();
                previewScreenY = bounds.y();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
        if (dragging && click.method_74245() == 0) {
            previewScreenX += deltaX;
            previewScreenY += deltaY;

            OverlayRenderer.LayoutBounds screenBounds = OverlayRenderer.getPreviewBounds(this.field_22789, this.field_22790, config);
            double maxX = Math.max(0, this.field_22789 - screenBounds.width());
            double maxY = Math.max(0, this.field_22790 - screenBounds.height());
            previewScreenX = Math.max(0, Math.min(previewScreenX, maxX));
            previewScreenY = Math.max(0, Math.min(previewScreenY, maxY));

            float scale = config.appearance.hudScale;
            OverlayRenderer.LayoutBounds logicalBounds = OverlayRenderer.getPreviewLogicalBounds(this.field_22789, this.field_22790, config);
            int logicalWidth = Math.max(1, Math.round(this.field_22789 / scale));
            int logicalHeight = Math.max(1, Math.round(this.field_22790 / scale));
            OverlayRenderer.AnchorPoint anchor = OverlayRenderer.getAnchorPoint(logicalWidth, logicalHeight,
                    logicalBounds.width(), logicalBounds.height(), config.appearance.position);

            int targetLogicalX = (int) Math.round(previewScreenX / scale);
            int targetLogicalY = (int) Math.round(previewScreenY / scale);
            config.appearance.xOffset = targetLogicalX - anchor.x();
            config.appearance.yOffset = targetLogicalY - anchor.y();
            return true;
        }
        return super.method_25403(click, deltaX, deltaY);
    }

    @Override
    public boolean method_25406(class_11909 click) {
        if (click.method_74245() == 0) {
            dragging = false;
        }
        return super.method_25406(click);
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        context.method_25296(0, 0, this.field_22789, this.field_22790, 0xC0182028, 0xD010141A);
        OverlayRenderer.renderPreview(context, field_22787, config, this.field_22789, this.field_22790);
        super.method_25394(context, mouseX, mouseY, delta);

        context.method_27534(field_22793, field_22785, this.field_22789 / 2, 48, 0xFFFFFFFF);
        context.method_27534(field_22793, class_2561.method_43471("text.fps_overlay.position_editor_hint"),
                this.field_22789 / 2, 62, 0xFFB7C6D1);
    }
}
