package net.hicham.fps_overlay;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PositionEditorScreen extends Screen {
    private static final int COL_BG_SCREEN = 0xEE0D1117;
    private static final int COL_CARD_BG = 0xCC141B22;
    private static final int COL_BORDER = 0xFF2A3A4A;
    private static final int TEXT_PRIMARY = 0xFFE8EEF2;
    private static final int TEXT_HEADER = 0xFFA8C0CE;
    private static final int TEXT_MUTED = 0xFF7A9AB0;
    private static final int TEXT_ACCENT = 0xFF5BBDE4;
    private static final int TEXT_GREEN = 0xFF4DE89A;
    private static final int COL_DRAG_BORDER = 0xFF6FB8E0;
    private static final int COL_GHOST_ACTIVE_BG = 0x285BBDE4;
    private static final int COL_GHOST_ACTIVE_BORDER = 0xFF5BBDE4;
    private static final int COL_GHOST_BG = 0x0EFFFFFF;
    private static final int COL_GHOST_BORDER = 0x387A9AB0;
    private static final int COL_CROSSHAIR = 0x205BBDE4;

    private final Screen parent;
    private final ModConfig config;

    private boolean dragging;
    private double previewScreenX;
    private double previewScreenY;
    private CycleButton<ModConfig.OverlayPosition> positionButton;

    public PositionEditorScreen(Screen parent, ModConfig config) {
        super(Component.translatable("screen.fps_overlay.position_editor"));
        this.parent = parent;
        this.config = config;
    }

    @Override
    protected void init() {
        int footerY = this.height - 28;

        this.positionButton = CycleButton.<ModConfig.OverlayPosition>builder(ModConfig.OverlayPosition::getDisplayText,
                        config.appearance.position)
                .withValues(ModConfig.OverlayPosition.values())
                .create(16, 14, 150, 20, Component.translatable("option.fps_overlay.position"),
                        (button, value) -> {
                            config.appearance.position = value;
                            config.appearance.xOffset = 0;
                            config.appearance.yOffset = 0;
                        });
        addRenderableWidget(this.positionButton);

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.reset_offset"), button -> {
            config.appearance.xOffset = 0;
            config.appearance.yOffset = 0;
        }).bounds(this.width / 2 - 125, footerY, 120, 20)
                .tooltip(Tooltip.create(Component.translatable("tooltip.fps_overlay.resetOffset")))
                .build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 + 5, footerY, 120, 20).build());
    }

    @Override
    public void onClose() {
        ConfigManager.saveConfig();
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        boolean handledByWidget = super.mouseClicked(click, doubled);
        if (handledByWidget) {
            return true;
        }

        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
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
        if (dragging && click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
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

            int targetLogicalX = (int) Math.round(previewScreenX / scale);
            int targetLogicalY = (int) Math.round(previewScreenY / scale);

            ModConfig.OverlayPosition closestPosition = config.appearance.position;
            double minDistanceSq = Double.MAX_VALUE;

            for (ModConfig.OverlayPosition pos : ModConfig.OverlayPosition.values()) {
                OverlayRenderer.AnchorPoint testAnchor = OverlayRenderer.getAnchorPoint(
                        logicalWidth, logicalHeight, logicalBounds.width(), logicalBounds.height(), pos);
                double dx = targetLogicalX - testAnchor.x();
                double dy = targetLogicalY - testAnchor.y();
                double distSq = dx * dx + dy * dy;
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestPosition = pos;
                }
            }

            if (closestPosition != config.appearance.position) {
                config.appearance.position = closestPosition;
                if (positionButton != null) {
                    positionButton.setValue(closestPosition);
                }
            }

            OverlayRenderer.AnchorPoint anchor = OverlayRenderer.getAnchorPoint(
                    logicalWidth, logicalHeight, logicalBounds.width(), logicalBounds.height(), config.appearance.position);
            config.appearance.xOffset = targetLogicalX - anchor.x();
            config.appearance.yOffset = targetLogicalY - anchor.y();
            return true;
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            dragging = false;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, COL_BG_SCREEN);

        if (dragging) {
            guiGraphics.fill(0, this.height / 2, this.width, this.height / 2 + 1, COL_CROSSHAIR);
            guiGraphics.fill(this.width / 2, 0, this.width / 2 + 1, this.height, COL_CROSSHAIR);

            renderSnapZoneGhosts(guiGraphics);
        }

        OverlayRenderer.renderPreview(guiGraphics, minecraft, config, this.width, this.height);

        OverlayRenderer.LayoutBounds currentBounds = OverlayRenderer.getPreviewBounds(this.width, this.height, config);
        if (dragging) {
            drawBorder(guiGraphics, currentBounds.x() - 1, currentBounds.y() - 1,
                    currentBounds.x() + currentBounds.width() + 1,
                    currentBounds.y() + currentBounds.height() + 1,
                    COL_DRAG_BORDER);
        } else if (currentBounds.contains(mouseX, mouseY)) {
            drawBorder(guiGraphics, currentBounds.x() - 1, currentBounds.y() - 1,
                    currentBounds.x() + currentBounds.width() + 1,
                    currentBounds.y() + currentBounds.height() + 1,
                    COL_BORDER);
        }

        guiGraphics.text(font, title, this.width / 2 - font.width(title) / 2, 14, TEXT_PRIMARY, true);
        Component hint = dragging
                ? Component.translatable("text.fps_overlay.position_editor_dragging_hint")
                : Component.translatable("text.fps_overlay.position_editor_idle_hint");
        int hintColor = dragging ? TEXT_GREEN : TEXT_HEADER;
        guiGraphics.text(font, hint, this.width / 2 - font.width(hint) / 2, 28, hintColor, false);

        renderCoordinateHud(guiGraphics);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderSnapZoneGhosts(GuiGraphicsExtractor guiGraphics) {
        float scale = config.appearance.hudScale;
        OverlayRenderer.LayoutBounds logicalBounds =
                OverlayRenderer.getPreviewLogicalBounds(this.width, this.height, config);
        int logicalWidth = Math.max(1, Math.round(this.width / scale));
        int logicalHeight = Math.max(1, Math.round(this.height / scale));

        for (ModConfig.OverlayPosition pos : ModConfig.OverlayPosition.values()) {
            OverlayRenderer.AnchorPoint anchor = OverlayRenderer.getAnchorPoint(
                    logicalWidth, logicalHeight, logicalBounds.width(), logicalBounds.height(), pos);
            int gx = Math.round(anchor.x() * scale);
            int gy = Math.round(anchor.y() * scale);
            int gw = Math.round(logicalBounds.width() * scale);
            int gh = Math.round(logicalBounds.height() * scale);

            boolean isCurrent = (pos == config.appearance.position);
            int bgCol = isCurrent ? COL_GHOST_ACTIVE_BG : COL_GHOST_BG;
            int borderCol = isCurrent ? COL_GHOST_ACTIVE_BORDER : COL_GHOST_BORDER;

            guiGraphics.fill(gx, gy, gx + gw, gy + gh, bgCol);
            drawBorder(guiGraphics, gx, gy, gx + gw, gy + gh, borderCol);

            if (isCurrent) {
                String posName = pos.getDisplayText().getString();
                int textX = gx + (gw - font.width(posName)) / 2;
                int textY = gy + (gh - font.lineHeight) / 2;
                guiGraphics.text(font, posName, textX, textY, TEXT_ACCENT, true);
            }
        }
    }

    private void renderCoordinateHud(GuiGraphicsExtractor guiGraphics) {
        int infoX = 16;
        int infoY = this.height - 62;
        int infoW = 168;
        int infoH = 30;

        guiGraphics.fill(infoX, infoY, infoX + infoW, infoY + infoH, COL_CARD_BG);
        drawBorder(guiGraphics, infoX, infoY, infoX + infoW, infoY + infoH, COL_BORDER);

        Component posComp = Component.translatable("text.fps_overlay.position_readout", config.appearance.position.getDisplayText());
        Component offsetComp = Component.translatable("text.fps_overlay.offset_readout", config.appearance.xOffset, config.appearance.yOffset);

        guiGraphics.text(font, posComp, infoX + 6, infoY + 5, TEXT_ACCENT, false);
        guiGraphics.text(font, offsetComp, infoX + 6, infoY + 16, TEXT_MUTED, false);
    }

    private void drawBorder(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }
}
