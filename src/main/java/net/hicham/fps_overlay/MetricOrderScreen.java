package net.hicham.fps_overlay;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MetricOrderScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 48;
    private static final int LIST_WIDTH = 320;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_MUTED = 0xFF93A8B7;
    private static final int TEXT_ENABLED = 0xFF7CFFB2;
    private static final int TEXT_DISABLED = 0xFFFF9999;

    private final Screen parent;
    private final ModConfig config;
    private final List<OverlayMetric> order;

    private int draggingIndex = -1;
    private double dragPointerY = -1;

    public MetricOrderScreen(Screen parent, ModConfig config) {
        super(Text.translatable("screen.fps_overlay.metric_order"));
        this.parent = parent;
        this.config = config;
        this.order = new ArrayList<>(OverlayMetric.sanitizeOrder(config.hud.metricOrder));
    }

    @Override
    protected void init() {
        clearChildren();
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> close())
                .dimensions(this.width / 2 - 75, this.height - 28, 150, 20).build());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int row = rowAt(click.x(), click.y());
        if (row >= 0 && row < order.size()) {
            if (click.button() == 0) {
                draggingIndex = row;
                dragPointerY = click.y();
                return true;
            }
            if (click.button() == 1) {
                OverlayMetric metric = order.get(row);
                config.hud.setMetricEnabled(metric, !config.hud.isMetricEnabled(metric));
                ConfigManager.saveConfig();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (draggingIndex >= 0 && click.button() == 0) {
            dragPointerY += deltaY;
            int targetIndex = rowAt(getListLeft() + 1, dragPointerY);
            if (targetIndex >= 0 && targetIndex < order.size() && targetIndex != draggingIndex) {
                OverlayMetric metric = order.remove(draggingIndex);
                order.add(targetIndex, metric);
                draggingIndex = targetIndex;
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && draggingIndex >= 0) {
            saveOrder();
            draggingIndex = -1;
            dragPointerY = -1;
            return true;
        }
        return super.mouseReleased(click);
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

    @Override
    public void close() {
        saveOrder();
        ConfigManager.saveConfig();
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xC0182028, 0xD010141A);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, title, this.width / 2, 18, TEXT_PRIMARY);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("text.fps_overlay.metric_order_hint"),
                this.width / 2, 30, 0xFFB7C6D1);

        int listLeft = getListLeft();
        for (int i = 0; i < order.size(); i++) {
            int rowY = LIST_TOP + (i * ROW_HEIGHT);
            OverlayMetric metric = order.get(i);
            boolean enabled = config.hud.isMetricEnabled(metric);
            boolean dragging = i == draggingIndex;

            int bgColor = dragging ? 0xCC40627A : 0xAA1E2A33;
            context.fill(listLeft, rowY, listLeft + LIST_WIDTH, rowY + 18, bgColor);
            int borderColor = dragging ? 0xFF8BC6E8 : 0xFF3B4A56;
            context.fill(listLeft, rowY, listLeft + LIST_WIDTH, rowY + 1, borderColor);
            context.fill(listLeft, rowY + 17, listLeft + LIST_WIDTH, rowY + 18, borderColor);
            context.fill(listLeft, rowY, listLeft + 1, rowY + 18, borderColor);
            context.fill(listLeft + LIST_WIDTH - 1, rowY, listLeft + LIST_WIDTH, rowY + 18, borderColor);

            String visibility = enabled ? "[ON]" : "[OFF]";
            context.drawTextWithShadow(textRenderer, visibility, listLeft + 8, rowY + 5, enabled ? TEXT_ENABLED : TEXT_DISABLED);
            context.drawTextWithShadow(textRenderer, Text.translatable(metric.getLabelKey()).getString(), listLeft + 64,
                    rowY + 5, TEXT_PRIMARY);
            context.drawTextWithShadow(textRenderer, dragging ? "dragging" : "drag", listLeft + LIST_WIDTH - 58,
                    rowY + 5, TEXT_MUTED);
        }
    }
}
