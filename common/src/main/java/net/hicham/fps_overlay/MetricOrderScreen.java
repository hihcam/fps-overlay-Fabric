package net.hicham.fps_overlay;

import net.minecraft.client.gui.GuiGraphics;
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
    private static final int ROW_HEIGHT = 30;
    private static final int LIST_TOP = 58;
    private static final int LIST_WIDTH = 540;
    private static final int TOGGLE_WIDTH = 72;
    private static final int RESET_WIDTH = 52;
    private static final int EDIT_WIDTH = 176;
    private static final int HANDLE_WIDTH = 56;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_MUTED = 0xFF93A8B7;
    private static final int TEXT_HEADER = 0xFFB7C6D1;
    private static final int TEXT_GREEN = 0xFF7CFFB2;
    private static final int TEXT_RED = 0xFFFF9999;

    private final Screen parent;
    private final ModConfig config;
    private final List<OverlayMetric> order;
    private final Map<OverlayMetric, MetricRowWidgets> rowWidgets = new EnumMap<>(OverlayMetric.class);

    private int draggingIndex = -1;
    private double dragPointerY = -1;

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

        layoutRowWidgets();

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.reset_all_metrics"),
                        button -> resetAllMetrics())
                .bounds(this.width / 2 - 160, this.height - 28, 110, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 + 10, this.height - 28, 150, 20).build());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) {
            return true;
        }

        int row = rowAt(click.x(), click.y());
        if (row >= 0 && row < order.size() && click.button() == 0 && isHandleHit(click.x())) {
            draggingIndex = row;
            dragPointerY = click.y();
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (draggingIndex >= 0 && click.button() == 0) {
            dragPointerY += deltaY;
            int targetIndex = rowAt(getListLeft() + LIST_WIDTH - 2, dragPointerY);
            if (targetIndex >= 0 && targetIndex < order.size() && targetIndex != draggingIndex) {
                OverlayMetric metric = order.remove(draggingIndex);
                order.add(targetIndex, metric);
                draggingIndex = targetIndex;
                layoutRowWidgets();
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == 0 && draggingIndex >= 0) {
            saveOrder();
            draggingIndex = -1;
            dragPointerY = -1;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void onClose() {
        saveOrder();
        ConfigManager.saveConfig();
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xC0182028, 0xD010141A);

        guiGraphics.drawCenteredString(font, title, this.width / 2, 18, TEXT_PRIMARY);
        guiGraphics.drawCenteredString(font, Component.translatable("text.fps_overlay.metric_order_hint"),
                this.width / 2, 31, TEXT_HEADER);

        int listLeft = getListLeft();
        int headerY = LIST_TOP - 11;
        guiGraphics.drawString(font, Component.translatable("text.fps_overlay.metric_column_visibility"),
                listLeft + 10, headerY, TEXT_MUTED, false);
        guiGraphics.drawString(font, Component.translatable("text.fps_overlay.metric_column_name"),
                listLeft + TOGGLE_WIDTH + 26, headerY, TEXT_MUTED, false);
        guiGraphics.drawString(font, Component.translatable("text.fps_overlay.metric_column_custom"),
                listLeft + LIST_WIDTH - HANDLE_WIDTH - RESET_WIDTH - EDIT_WIDTH - 12, headerY, TEXT_MUTED, false);
        guiGraphics.drawString(font, Component.translatable("text.fps_overlay.metric_column_drag"),
                listLeft + LIST_WIDTH - HANDLE_WIDTH + 8, headerY, TEXT_MUTED, false);

        for (int i = 0; i < order.size(); i++) {
            int rowY = LIST_TOP + (i * ROW_HEIGHT);
            OverlayMetric metric = order.get(i);
            boolean dragging = i == draggingIndex;
            String defaultName = Component.translatable(metric.getDisplayNameKey()).getString();
            String customName = config.hud.getCustomMetricName(metric);

            int bgColor = dragging ? 0xCC40627A : 0xAA1E2A33;
            int borderColor = dragging ? 0xFF8BC6E8 : 0xFF3B4A56;
            guiGraphics.fill(listLeft, rowY, listLeft + LIST_WIDTH, rowY + 24, bgColor);
            guiGraphics.fill(listLeft, rowY, listLeft + LIST_WIDTH, rowY + 1, borderColor);
            guiGraphics.fill(listLeft, rowY + 23, listLeft + LIST_WIDTH, rowY + 24, borderColor);
            guiGraphics.fill(listLeft, rowY, listLeft + 1, rowY + 24, borderColor);
            guiGraphics.fill(listLeft + LIST_WIDTH - 1, rowY, listLeft + LIST_WIDTH, rowY + 24, borderColor);

            guiGraphics.drawString(font, defaultName, listLeft + TOGGLE_WIDTH + 20, rowY + 4, TEXT_PRIMARY, true);
            if (!customName.isBlank()) {
                guiGraphics.drawString(font,
                        Component.translatable("text.fps_overlay.metric_rename_preview", customName).getString(),
                        listLeft + TOGGLE_WIDTH + 20, rowY + 14, TEXT_MUTED, false);
            }

            guiGraphics.drawString(font, dragging ? Component.translatable("text.fps_overlay.dragging").getString()
                            : Component.translatable("text.fps_overlay.drag_handle").getString(),
                    listLeft + LIST_WIDTH - HANDLE_WIDTH + 8, rowY + 8, TEXT_MUTED, false);
        }

        guiGraphics.drawCenteredString(font, Component.translatable("text.fps_overlay.metric_order_footer"),
                this.width / 2, this.height - 40, TEXT_MUTED);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
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

        Button resetButton = Button.builder(Component.translatable("button.fps_overlay.reset_metric_name"), button -> {
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
        for (int i = 0; i < order.size(); i++) {
            OverlayMetric metric = order.get(i);
            MetricRowWidgets widgets = rowWidgets.get(metric);
            if (widgets == null) {
                continue;
            }

            int rowY = LIST_TOP + (i * ROW_HEIGHT);
            widgets.toggleButton().setX(listLeft + 8);
            widgets.toggleButton().setY(rowY + 2);
            widgets.renameBox().setX(listLeft + LIST_WIDTH - HANDLE_WIDTH - RESET_WIDTH - EDIT_WIDTH - 8);
            widgets.renameBox().setY(rowY + 2);
            widgets.resetButton().setX(listLeft + LIST_WIDTH - HANDLE_WIDTH - RESET_WIDTH - 4);
            widgets.resetButton().setY(rowY + 2);
        }
    }

    private Component getVisibilityText(OverlayMetric metric) {
        boolean enabled = config.hud.isMetricEnabled(metric);
        return Component.literal(enabled
                        ? Component.translatable("button.fps_overlay.metric_visible").getString()
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
            MetricRowWidgets widgets = rowWidgets.get(metric);
            if (widgets != null) {
                widgets.renameBox().setValue("");
                widgets.toggleButton().setMessage(getVisibilityText(metric));
            }
        }

        saveOrder();
        layoutRowWidgets();
    }

    private int rowAt(double mouseX, double mouseY) {
        int listLeft = getListLeft();
        if (mouseX < listLeft || mouseX > listLeft + LIST_WIDTH) {
            return -1;
        }

        int relative = (int) mouseY - LIST_TOP;
        if (relative < 0) {
            return -1;
        }

        int row = relative / ROW_HEIGHT;
        return row < order.size() ? row : -1;
    }

    private boolean isHandleHit(double mouseX) {
        int handleLeft = getListLeft() + LIST_WIDTH - HANDLE_WIDTH;
        return mouseX >= handleLeft && mouseX <= handleLeft + HANDLE_WIDTH;
    }

    private int getListLeft() {
        return (this.width - LIST_WIDTH) / 2;
    }

    private void saveOrder() {
        List<String> ids = new ArrayList<>();
        for (OverlayMetric metric : order) {
            ids.add(metric.getId());
        }
        config.hud.metricOrder = ids;
    }

    private record MetricRowWidgets(Button toggleButton, EditBox renameBox, Button resetButton) {
    }
}
