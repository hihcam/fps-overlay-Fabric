package net.hicham.fps_overlay;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public class MetricOrderScreen extends class_437 {
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 48;
    private static final int LIST_WIDTH = 320;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_MUTED = 0xFF93A8B7;
    private static final int TEXT_ENABLED = 0xFF7CFFB2;
    private static final int TEXT_DISABLED = 0xFFFF9999;

    private final class_437 parent;
    private final ModConfig config;
    private final List<OverlayMetric> order;

    private int draggingIndex = -1;
    private double dragPointerY = -1;

    public MetricOrderScreen(class_437 parent, ModConfig config) {
        super(class_2561.method_43471("screen.fps_overlay.metric_order"));
        this.parent = parent;
        this.config = config;
        this.order = new ArrayList<>(OverlayMetric.sanitizeOrder(config.hud.metricOrder));
    }

    @Override
    protected void method_25426() {
        method_37067();
        method_37063(class_4185.method_46430(class_2561.method_43471("gui.done"), button -> method_25419())
                .method_46434(this.field_22789 / 2 - 75, this.field_22790 - 28, 150, 20).method_46431());
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        int row = rowAt(click.comp_4798(), click.comp_4799());
        if (row >= 0 && row < order.size()) {
            if (click.method_74245() == 0) {
                draggingIndex = row;
                dragPointerY = click.comp_4799();
                return true;
            }
            if (click.method_74245() == 1) {
                OverlayMetric metric = order.get(row);
                config.hud.setMetricEnabled(metric, !config.hud.isMetricEnabled(metric));
                ConfigManager.saveConfig();
                return true;
            }
        }
        return super.method_25402(click, doubled);
    }

    @Override
    public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
        if (draggingIndex >= 0 && click.method_74245() == 0) {
            dragPointerY += deltaY;
            int targetIndex = rowAt(getListLeft() + 1, dragPointerY);
            if (targetIndex >= 0 && targetIndex < order.size() && targetIndex != draggingIndex) {
                OverlayMetric metric = order.remove(draggingIndex);
                order.add(targetIndex, metric);
                draggingIndex = targetIndex;
            }
            return true;
        }
        return super.method_25403(click, deltaX, deltaY);
    }

    @Override
    public boolean method_25406(class_11909 click) {
        if (click.method_74245() == 0 && draggingIndex >= 0) {
            saveOrder();
            draggingIndex = -1;
            dragPointerY = -1;
            return true;
        }
        return super.method_25406(click);
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
        return (this.field_22789 - LIST_WIDTH) / 2;
    }

    private void saveOrder() {
        List<String> ids = new ArrayList<>();
        for (OverlayMetric metric : order) {
            ids.add(metric.getId());
        }
        config.hud.metricOrder = ids;
    }

    @Override
    public void method_25419() {
        saveOrder();
        ConfigManager.saveConfig();
        if (field_22787 != null) {
            field_22787.method_1507(parent);
        }
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        context.method_25296(0, 0, this.field_22789, this.field_22790, 0xC0182028, 0xD010141A);
        super.method_25394(context, mouseX, mouseY, delta);

        context.method_27534(field_22793, field_22785, this.field_22789 / 2, 18, TEXT_PRIMARY);
        context.method_27534(field_22793, class_2561.method_43471("text.fps_overlay.metric_order_hint"),
                this.field_22789 / 2, 30, 0xFFB7C6D1);

        int listLeft = getListLeft();
        for (int i = 0; i < order.size(); i++) {
            int rowY = LIST_TOP + (i * ROW_HEIGHT);
            OverlayMetric metric = order.get(i);
            boolean enabled = config.hud.isMetricEnabled(metric);
            boolean dragging = i == draggingIndex;

            int bgColor = dragging ? 0xCC40627A : 0xAA1E2A33;
            context.method_25294(listLeft, rowY, listLeft + LIST_WIDTH, rowY + 18, bgColor);
            int borderColor = dragging ? 0xFF8BC6E8 : 0xFF3B4A56;
            context.method_25294(listLeft, rowY, listLeft + LIST_WIDTH, rowY + 1, borderColor);
            context.method_25294(listLeft, rowY + 17, listLeft + LIST_WIDTH, rowY + 18, borderColor);
            context.method_25294(listLeft, rowY, listLeft + 1, rowY + 18, borderColor);
            context.method_25294(listLeft + LIST_WIDTH - 1, rowY, listLeft + LIST_WIDTH, rowY + 18, borderColor);

            String visibility = enabled ? "[ON]" : "[OFF]";
            context.method_25303(field_22793, visibility, listLeft + 8, rowY + 5, enabled ? TEXT_ENABLED : TEXT_DISABLED);
            context.method_25303(field_22793, class_2561.method_43471(metric.getLabelKey()).getString(), listLeft + 64,
                    rowY + 5, TEXT_PRIMARY);
            context.method_25303(field_22793, dragging ? "dragging" : "drag", listLeft + LIST_WIDTH - 58,
                    rowY + 5, TEXT_MUTED);
        }
    }
}
