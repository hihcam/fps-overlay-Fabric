package net.hicham.fps_overlay;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MetricOrderScreen extends Screen {

    // ── Layout constants ─────────────────────────────────────────────────────
    private static final int ROW_HEIGHT = 30;
    private static final int LIST_TOP = 72;
    private static final int DEFAULT_LIST_WIDTH = 480;
    private static final int PREVIEW_PANEL_WIDTH = 210;
    private static final int PANEL_GAP = 14;
    private static final int TOGGLE_WIDTH = 64;
    private static final int RESET_WIDTH = 44;
    private static final int EDIT_WIDTH = 145;
    private static final int HANDLE_WIDTH = 54;
    private static final int ORDER_WIDTH = 26;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final int TEXT_PRIMARY = 0xFFE8EEF2;
    private static final int TEXT_MUTED = 0xFF7A9AB0;
    private static final int TEXT_HEADER = 0xFFA8C0CE;
    private static final int TEXT_GREEN = 0xFF4DE89A;
    private static final int TEXT_RED = 0xFFFF7777;
    private static final int TEXT_ACCENT = 0xFF5BBDE4;

    private static final int COL_BG_NORMAL = 0xAA141B22;
    private static final int COL_BG_HOVER = 0xAA1A2535;
    private static final int COL_BG_DRAG = 0xCC2A4A62;
    private static final int COL_BORDER = 0xFF2A3A4A;
    private static final int COL_BORDER_HI = 0xFF3D5A72;
    private static final int COL_BORDER_ACC = 0xFF6FB8E0;
    private static final int COL_BG_SCREEN = 0xFF0D1117;

    // ── Toast ─────────────────────────────────────────────────────────────────
    private static final int TOAST_DURATION_TICKS = 40;
    private String toastMessage = "";
    private int toastTicks = 0;

    // ── State ─────────────────────────────────────────────────────────────────
    private final Screen parent;
    private final ModConfig config;
    private final List<OverlayMetric> order;
    private final Map<OverlayMetric, MetricRowWidgets> rowWidgets = new EnumMap<>(OverlayMetric.class);

    private int draggingIndex = -1;
    private int hoverIndex = -1;
    private double scrollAmount = 0;
    private int maxScroll = 0;
    private Button resetAllButton;
    private Button doneButton;

    public MetricOrderScreen(Screen parent, ModConfig config) {
        super(Component.translatable("screen.fps_overlay.metric_order"));
        this.parent = parent;
        this.config = config;
        this.order = new ArrayList<>(OverlayMetric.sanitizeOrder(config.hud.metricOrder));
    }

    @Override
    protected void init() {
        clearWidgets();
        rowWidgets.clear();

        for (OverlayMetric metric : OverlayMetric.values()) {
            rowWidgets.put(metric, createRowWidgets(metric));
        }

        updateMaxScroll();
        clampScroll();
        layoutRowWidgets();

        resetAllButton = addRenderableWidget(
                Button.builder(Component.translatable("button.fps_overlay.reset_all_metrics"),
                        button -> resetAllMetrics())
                        .bounds(this.width / 2 - 145, this.height - 28, 130, 20)
                        .build());

        doneButton = addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), button -> onClose())
                        .bounds(this.width / 2 + 15, this.height - 28, 130, 20)
                        .build());
    }

    private int viewportTop() {
        return LIST_TOP;
    }

    private int viewportBottom() {
        return this.height - 52;
    }

    private int getListWidth() {
        if (hasPreviewPanel()) {
            return DEFAULT_LIST_WIDTH;
        }
        return Math.min(DEFAULT_LIST_WIDTH + 40, this.width - 24);
    }

    private boolean hasPreviewPanel() {
        return this.width >= 720;
    }

    private int getListLeft() {
        if (hasPreviewPanel()) {
            int total = getListWidth() + PANEL_GAP + PREVIEW_PANEL_WIDTH;
            return (this.width - total) / 2;
        }
        return (this.width - getListWidth()) / 2;
    }

    private int getPreviewLeft() {
        return getListLeft() + getListWidth() + PANEL_GAP;
    }

    private void updateMaxScroll() {
        int listHeight = order.size() * ROW_HEIGHT;
        int viewportHeight = viewportBottom() - viewportTop();
        this.maxScroll = Math.max(0, listHeight - viewportHeight);
    }

    private void clampScroll() {
        this.scrollAmount = Math.max(0, Math.min(this.scrollAmount, this.maxScroll));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
            double horizontalAmount, double verticalAmount) {
        this.scrollAmount -= verticalAmount * 15;
        clampScroll();
        layoutRowWidgets();
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.y() < viewportTop() || click.y() > viewportBottom()) {
            if (handleFooterButtonClick(resetAllButton, click, doubled)
                    || handleFooterButtonClick(doneButton, click, doubled)) {
                return true;
            }
            setFocused(null);
            return false;
        }

        if (super.mouseClicked(click, doubled)) {
            return true;
        }

        setFocused(null);

        int row = rowAt(click.x(), click.y());
        if (row >= 0 && row < order.size()
                && click.button() == InputConstants.MOUSE_BUTTON_LEFT
                && isHandleHit(click.x())) {
            draggingIndex = row;
            return true;
        }
        return false;
    }

    private boolean handleFooterButtonClick(Button button, MouseButtonEvent click, boolean doubled) {
        if (button != null && button.mouseClicked(click, doubled)) {
            if (button.shouldTakeFocusAfterInteraction()) {
                setFocused(button);
            }
            if (click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                setDragging(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click,
            double deltaX, double deltaY) {
        if (draggingIndex >= 0 && click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            int target = rowAt(getListLeft() + getListWidth() - 2, click.y());
            if (target >= 0 && target < order.size() && target != draggingIndex) {
                OverlayMetric metric = order.remove(draggingIndex);
                order.add(target, metric);
                draggingIndex = target;
                layoutRowWidgets();
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == InputConstants.MOUSE_BUTTON_LEFT && draggingIndex >= 0) {
            saveOrder();
            draggingIndex = -1;
            showToast("screen.fps_overlay.toast_saved");
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        hoverIndex = rowAt(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void onClose() {
        saveOrder();
        ConfigManager.saveConfig();
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (toastTicks > 0) {
            toastTicks--;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, COL_BG_SCREEN);

        int listLeft = getListLeft();
        int listWidth = getListWidth();
        int vTop = viewportTop();
        int vBottom = viewportBottom();
        int headerY = LIST_TOP - 13;

        // ── Rows ─────────────────────────────────────────────────────────────
        g.enableScissor(0, vTop, this.width, vBottom);
        for (int i = 0; i < order.size(); i++) {
            int rowY = (int) (LIST_TOP + (i * ROW_HEIGHT) - scrollAmount);
            if (rowY + ROW_HEIGHT < vTop || rowY > vBottom) {
                continue;
            }

            OverlayMetric metric = order.get(i);
            boolean isDragging = (i == draggingIndex);
            boolean isHovered = (i == hoverIndex && draggingIndex < 0);
            boolean enabled = config.hud.isMetricEnabled(metric);
            String defaultName = Component.translatable(metric.getDisplayNameKey()).getString();
            String customName = config.hud.getCustomMetricName(metric);

            int bgColor = isDragging ? COL_BG_DRAG
                    : isHovered ? COL_BG_HOVER
                            : COL_BG_NORMAL;
            int borderColor = isDragging ? COL_BORDER_ACC
                    : isHovered ? COL_BORDER_HI
                            : COL_BORDER;

            g.fill(listLeft, rowY, listLeft + listWidth, rowY + 24, bgColor);
            drawBorder(g, listLeft, rowY, listLeft + listWidth, rowY + 24, borderColor);

            // Left status indicator bar
            g.fill(listLeft, rowY, listLeft + 3, rowY + 24, enabled ? TEXT_GREEN : TEXT_RED);

            // Order badge
            String badge = String.valueOf(i + 1);
            int badgeX = listLeft + 6;
            g.fill(badgeX, rowY + 6, badgeX + 14, rowY + 18, COL_BORDER);
            drawCenteredText(g, badge, badgeX + 7, rowY + 8, TEXT_MUTED);

            // Metric display name
            g.text(font, defaultName,
                    listLeft + ORDER_WIDTH + TOGGLE_WIDTH + 6, rowY + 4, TEXT_PRIMARY, true);

            // Custom name preview
            if (!customName.isBlank()) {
                String preview = Component.translatable(
                        "text.fps_overlay.metric_rename_preview", customName).getString();
                g.text(font, preview,
                        listLeft + ORDER_WIDTH + TOGGLE_WIDTH + 6, rowY + 14,
                        TEXT_MUTED, false);
            }

            // Grip handle
            int handleLeft = listLeft + listWidth - HANDLE_WIDTH;
            boolean handleHovered = (hoverIndex == i && mouseX >= handleLeft);
            int hx = handleLeft + HANDLE_WIDTH / 2;
            int hy = rowY + 7;
            int hCol = isDragging ? TEXT_ACCENT : (handleHovered ? TEXT_PRIMARY : TEXT_MUTED);

            if (isDragging) {
                drawCenteredText(g, Component.translatable("text.fps_overlay.dragging").getString(),
                        hx, hy, TEXT_ACCENT);
            } else {
                g.fill(hx - 7, hy, hx + 7, hy + 2, hCol);
                g.fill(hx - 7, hy + 4, hx + 7, hy + 6, hCol);
                g.fill(hx - 7, hy + 8, hx + 7, hy + 10, hCol);
            }
        }

        // Drop target indicator line
        if (draggingIndex >= 0 && hoverIndex >= 0 && hoverIndex < order.size()) {
            int dropY = (int) (LIST_TOP + (hoverIndex * ROW_HEIGHT) - scrollAmount);
            g.fill(listLeft, dropY - 1, listLeft + listWidth, dropY + 1, TEXT_ACCENT);
        }

        g.disableScissor();

        // ── Corner accent brackets for list ──────────────────────────────────
        drawCornerBrackets(g, listLeft, vTop - 2, listWidth, vBottom - vTop + 4, 8, TEXT_ACCENT);

        // ── Scrollbar ─────────────────────────────────────────────────────────
        if (maxScroll > 0) {
            int trackX = listLeft + listWidth + 3;
            int trackTop = vTop;
            int trackH = vBottom - vTop;
            float ratio = (float) trackH / (order.size() * ROW_HEIGHT);
            int thumbH = Math.max(16, (int) (trackH * ratio));
            int thumbY = trackTop + (int) ((scrollAmount / maxScroll) * (trackH - thumbH));
            g.fill(trackX, trackTop, trackX + 3, trackTop + trackH, COL_BORDER);
            g.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, COL_BORDER_ACC);
        }

        // ── Live Preview Panel ────────────────────────────────────────────────
        if (hasPreviewPanel()) {
            int pLeft = getPreviewLeft();
            int pWidth = PREVIEW_PANEL_WIDTH;
            int pHeight = vBottom - vTop;

            g.fill(pLeft, vTop, pLeft + pWidth, vBottom, COL_BG_NORMAL);
            drawBorder(g, pLeft, vTop, pLeft + pWidth, vBottom, COL_BORDER);
            drawCornerBrackets(g, pLeft, vTop, pWidth, pHeight, 8, TEXT_ACCENT);

            Component previewLabel = Component.translatable("text.fps_overlay.metric_order_preview_label");
            drawCenteredText(g, previewLabel, pLeft + pWidth / 2, vTop + 8, TEXT_ACCENT);
            g.fill(pLeft + 8, vTop + 20, pLeft + pWidth - 8, vTop + 21, COL_BORDER);

            OverlayRenderer.renderPreviewCard(g, minecraft, config, pLeft + 8, vTop + 26, pWidth - 16, pHeight - 34);
        }

        // ── Widgets (scissored so buttons/editboxes clip to viewport) ─────────
        g.enableScissor(0, vTop, this.width, vBottom);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.disableScissor();

        // ── Redraw fade overlays on top of widgets ────────────────────────────
        g.fill(0, 0, this.width, LIST_TOP - 14, COL_BG_SCREEN);
        g.fillGradient(0, LIST_TOP - 14, this.width, vTop, COL_BG_SCREEN, 0x0010141A);
        g.fillGradient(0, vBottom, this.width, vBottom + 7, 0x0010141A, COL_BG_SCREEN);
        g.fill(0, vBottom + 7, this.width, this.height, COL_BG_SCREEN);

        // ── Title & subtitle ─────────────────────────────────────────────────
        drawCenteredText(g, title, this.width / 2, 16, TEXT_PRIMARY);
        drawCenteredText(g,
                Component.translatable("text.fps_overlay.metric_order_hint"),
                this.width / 2, 28, TEXT_HEADER);

        // ── Column headers ────────────────────────────────────────────────────
        g.text(font, "#",
                listLeft + 6, headerY, TEXT_MUTED, false);
        g.text(font,
                Component.translatable("text.fps_overlay.metric_column_visibility").getString(),
                listLeft + ORDER_WIDTH + 8, headerY, TEXT_MUTED, false);
        g.text(font,
                Component.translatable("text.fps_overlay.metric_column_name").getString(),
                listLeft + ORDER_WIDTH + TOGGLE_WIDTH + 10, headerY, TEXT_MUTED, false);
        g.text(font,
                Component.translatable("text.fps_overlay.metric_column_custom").getString(),
                listLeft + listWidth - HANDLE_WIDTH - RESET_WIDTH - EDIT_WIDTH - 6,
                headerY, TEXT_MUTED, false);
        g.text(font,
                Component.translatable("text.fps_overlay.metric_column_drag").getString(),
                listLeft + listWidth - HANDLE_WIDTH + 6, headerY, TEXT_MUTED, false);

        // ── Footer hint ───────────────────────────────────────────────────────
        drawCenteredText(g,
                Component.translatable("text.fps_overlay.metric_order_footer"),
                this.width / 2, this.height - 40, TEXT_MUTED);

        // ── Footer buttons ───────────────────────────────────────────────────
        resetAllButton.extractRenderState(g, mouseX, mouseY, partialTick);
        doneButton.extractRenderState(g, mouseX, mouseY, partialTick);

        // ── Toast ─────────────────────────────────────────────────────────────
        if (toastTicks > 0) {
            float alpha = Math.min(1f, toastTicks / 8f);
            int toastA = (int) (alpha * 0xFF) << 24;
            String msg = Component.translatable(toastMessage).getString();
            int tw = font.width(msg) + 20;
            int tx = (this.width - tw) / 2;
            int ty = this.height - 60;
            g.fill(tx, ty, tx + tw, ty + 14, (toastA & 0xFF000000) | (COL_BG_DRAG & 0x00FFFFFF));
            drawBorder(g, tx, ty, tx + tw, ty + 14, (toastA & 0xFF000000) | (TEXT_ACCENT & 0x00FFFFFF));
            drawCenteredText(g, msg, this.width / 2, ty + 3,
                    (toastA & 0xFF000000) | (TEXT_ACCENT & 0x00FFFFFF));
        }
    }

    private MetricRowWidgets createRowWidgets(OverlayMetric metric) {
        Button toggleButton = Button.builder(getVisibilityText(metric), button -> {
            config.hud.setMetricEnabled(metric, !config.hud.isMetricEnabled(metric));
            button.setMessage(getVisibilityText(metric));
        }).bounds(0, 0, TOGGLE_WIDTH, 20).build();

        EditBox renameBox = new EditBox(this.font, 0, 0, EDIT_WIDTH, 20,
                Component.translatable("text.fps_overlay.custom_label"));
        renameBox.setMaxLength(20);
        renameBox.setHint(Component.translatable("text.fps_overlay.custom_label_placeholder"));
        renameBox.setValue(config.hud.getCustomMetricName(metric));
        renameBox.setResponder(value -> config.hud.setCustomMetricName(metric, value));

        Button resetButton = Button.builder(
                Component.translatable("button.fps_overlay.reset_metric_name"), button -> {
                    config.hud.setCustomMetricName(metric, "");
                    renameBox.setValue("");
                }).bounds(0, 0, RESET_WIDTH, 20).build();

        addRenderableWidget(toggleButton);
        addRenderableWidget(renameBox);
        addRenderableWidget(resetButton);
        return new MetricRowWidgets(toggleButton, renameBox, resetButton);
    }

    private void layoutRowWidgets() {
        int listLeft = getListLeft();
        int listWidth = getListWidth();
        int vTop = viewportTop();
        int vBottom = viewportBottom();

        for (int i = 0; i < order.size(); i++) {
            OverlayMetric metric = order.get(i);
            MetricRowWidgets wids = rowWidgets.get(metric);
            if (wids == null) {
                continue;
            }

            int rowY = (int) (LIST_TOP + (i * ROW_HEIGHT) - scrollAmount);
            boolean visible = rowY + ROW_HEIGHT > vTop && rowY < vBottom;

            int widgetY = visible ? rowY + 2 : -200;

            wids.toggleButton().setX(listLeft + ORDER_WIDTH + 4);
            wids.toggleButton().setY(widgetY);
            wids.toggleButton().visible = visible;

            wids.renameBox().setX(
                    listLeft + listWidth - HANDLE_WIDTH - RESET_WIDTH - EDIT_WIDTH - 6);
            wids.renameBox().setY(widgetY);
            wids.renameBox().visible = visible;
            if (!visible && getFocused() == wids.renameBox()) {
                setFocused(null);
            }

            wids.resetButton().setX(
                    listLeft + listWidth - HANDLE_WIDTH - RESET_WIDTH - 2);
            wids.resetButton().setY(widgetY);
            wids.resetButton().visible = visible;
        }
    }

    private Component getVisibilityText(OverlayMetric metric) {
        boolean enabled = config.hud.isMetricEnabled(metric);
        return Component.literal(
                enabled ? Component.translatable("button.fps_overlay.metric_visible").getString()
                        : Component.translatable("button.fps_overlay.metric_hidden").getString())
                .withColor(enabled ? TEXT_GREEN : TEXT_RED);
    }

    private void resetAllMetrics() {
        ModConfig defaults = new ModConfig();
        order.clear();
        order.addAll(OverlayMetric.sanitizeOrder(defaults.hud.metricOrder));
        config.hud.metricDisplayNames.clear();

        for (OverlayMetric metric : OverlayMetric.values()) {
            config.hud.setMetricEnabled(metric, defaults.hud.isMetricEnabled(metric));
            MetricRowWidgets wids = rowWidgets.get(metric);
            if (wids != null) {
                wids.renameBox().setValue("");
                wids.toggleButton().setMessage(getVisibilityText(metric));
            }
        }

        saveOrder();
        layoutRowWidgets();
        showToast("screen.fps_overlay.toast_reset");
    }

    private void drawBorder(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private void drawCornerBrackets(GuiGraphicsExtractor g, int bx, int byt, int bw, int bh, int cs, int color) {
        int byb = byt + bh;
        // top-left
        g.fill(bx, byt, bx + cs, byt + 1, color);
        g.fill(bx, byt, bx + 1, byt + cs, color);
        // top-right
        g.fill(bx + bw - cs, byt, bx + bw, byt + 1, color);
        g.fill(bx + bw - 1, byt, bx + bw, byt + cs, color);
        // bottom-left
        g.fill(bx, byb - cs, bx + 1, byb, color);
        g.fill(bx, byb - 1, bx + cs, byb, color);
        // bottom-right
        g.fill(bx + bw - 1, byb - cs, bx + bw, byb, color);
        g.fill(bx + bw - cs, byb - 1, bx + bw, byb, color);
    }

    private void drawCenteredText(GuiGraphicsExtractor g, String text, int x, int y, int color) {
        g.text(font, text, x - font.width(text) / 2, y, color, false);
    }

    private void drawCenteredText(GuiGraphicsExtractor g, Component text, int x, int y, int color) {
        g.text(font, text, x - font.width(text) / 2, y, color, false);
    }

    private int rowAt(double mouseX, double mouseY) {
        int listLeft = getListLeft();
        int listWidth = getListWidth();
        if (mouseX < listLeft || mouseX > listLeft + listWidth) {
            return -1;
        }
        if (mouseY < viewportTop() || mouseY > viewportBottom()) {
            return -1;
        }

        int relative = (int) (mouseY - LIST_TOP + scrollAmount);
        if (relative < 0) {
            return -1;
        }

        int row = relative / ROW_HEIGHT;
        return row < order.size() ? row : -1;
    }

    private boolean isHandleHit(double mouseX) {
        int handleLeft = getListLeft() + getListWidth() - HANDLE_WIDTH;
        return mouseX >= handleLeft && mouseX <= handleLeft + HANDLE_WIDTH;
    }

    private void saveOrder() {
        List<String> ids = new ArrayList<>();
        for (OverlayMetric m : order) {
            ids.add(m.getId());
        }
        config.hud.metricOrder = ids;
    }

    private void showToast(String translationKey) {
        this.toastMessage = translationKey;
        this.toastTicks = TOAST_DURATION_TICKS;
    }

    private record MetricRowWidgets(Button toggleButton, EditBox renameBox, Button resetButton) {
    }
}