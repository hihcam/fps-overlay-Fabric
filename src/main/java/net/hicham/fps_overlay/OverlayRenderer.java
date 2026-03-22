package net.hicham.fps_overlay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OverlayRenderer {
    private static ModConfig config;

    private static final ThreadLocal<DecimalFormat> ONE_DECIMAL = ThreadLocal.withInitial(() -> new DecimalFormat("0.0"));
    private static final ThreadLocal<DecimalFormat> WHOLE_NUMBER = ThreadLocal.withInitial(() -> new DecimalFormat("0"));
    private static final int GRAPH_HEIGHT = 24;

    public static void setConfig(ModConfig configData) {
        config = configData;
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (config == null || !config.general.enabled) {
            return;
        }

        PerformanceTracker tracker = PerformanceTracker.getInstance();
        tracker.recordFrame();

        if (config.appearance.autoHideF3 && client.getDebugHud().shouldShowDebugHud()) {
            return;
        }

        List<OverlayLine> lines = prepareLines(config, tracker, false);
        if (lines.isEmpty() && !config.hud.showGraph) {
            return;
        }

        float scale = config.appearance.hudScale;
        applyScale(context, scale, () -> renderScaled(context, client, config, lines, false));
    }

    public static void renderPreview(DrawContext context, MinecraftClient client, ModConfig previewConfig, int screenWidth,
            int screenHeight) {
        if (previewConfig == null) {
            return;
        }

        List<OverlayLine> lines = prepareLines(previewConfig, PerformanceTracker.getInstance(), true);
        float scale = previewConfig.appearance.hudScale;
        applyScale(context, scale, () -> renderScaled(context, client, previewConfig, lines, true));
    }

    public static LayoutBounds getPreviewBounds(int screenWidth, int screenHeight, ModConfig previewConfig) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer renderer = client != null ? client.textRenderer : null;
        List<OverlayLine> lines = prepareLines(previewConfig, PerformanceTracker.getInstance(), true);
        LayoutBounds logicalBounds = getPreviewLogicalBounds(screenWidth, screenHeight, previewConfig, renderer, lines);
        float scale = previewConfig.appearance.hudScale;
        return new LayoutBounds(
                Math.round(logicalBounds.x() * scale),
                Math.round(logicalBounds.y() * scale),
                Math.round(logicalBounds.width() * scale),
                Math.round(logicalBounds.height() * scale));
    }

    public static LayoutBounds getPreviewLogicalBounds(int screenWidth, int screenHeight, ModConfig previewConfig) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer renderer = client != null ? client.textRenderer : null;
        List<OverlayLine> lines = prepareLines(previewConfig, PerformanceTracker.getInstance(), true);
        return getPreviewLogicalBounds(screenWidth, screenHeight, previewConfig, renderer, lines);
    }

    private static LayoutBounds getPreviewLogicalBounds(int screenWidth, int screenHeight, ModConfig previewConfig,
            TextRenderer renderer, List<OverlayLine> lines) {
        float scale = previewConfig.appearance.hudScale;
        int logicalWidth = Math.max(1, Math.round(screenWidth / scale));
        int logicalHeight = Math.max(1, Math.round(screenHeight / scale));
        return measureBounds(renderer, previewConfig, lines, logicalWidth, logicalHeight);
    }

    public static AnchorPoint getAnchorPoint(int screenWidth, int screenHeight, int contentWidth, int contentHeight,
            ModConfig.OverlayPosition position) {
        int x = switch (position) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 4;
            case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - contentWidth) / 2;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> screenWidth - contentWidth - 4;
        };

        int y = switch (position) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 4;
            case CENTER_LEFT, CENTER_RIGHT -> (screenHeight - contentHeight) / 2;
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenHeight - contentHeight - 4;
        };

        return new AnchorPoint(x, y);
    }

    private static void renderScaled(DrawContext context, MinecraftClient client, ModConfig activeConfig,
            List<OverlayLine> lines, boolean preview) {
        TextRenderer renderer = client != null ? client.textRenderer : MinecraftClient.getInstance().textRenderer;
        if (renderer == null) {
            return;
        }

        int screenWidth = (int) (context.getScaledWindowWidth() / activeConfig.appearance.hudScale);
        int screenHeight = (int) (context.getScaledWindowHeight() / activeConfig.appearance.hudScale);
        LayoutBounds bounds = measureBounds(renderer, activeConfig, lines, screenWidth, screenHeight);

        if (activeConfig.appearance.showBackground) {
            drawRoundedRect(context, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 4,
                    getBackgroundColor(activeConfig));
        }

        if (activeConfig.appearance.overlayStyle == ModConfig.OverlayStyle.NAVBAR) {
            renderNavbar(context, renderer, activeConfig, lines, bounds);
        } else {
            renderVertical(context, renderer, activeConfig, lines, bounds);
        }

        if (activeConfig.hud.showGraph) {
            int[] graphValues = preview ? getPreviewGraphValues() : PerformanceTracker.getInstance().copyGraphValues();
            renderGraph(context, activeConfig, bounds, graphValues);
        }
    }

    private static void renderNavbar(DrawContext context, TextRenderer renderer, ModConfig activeConfig,
            List<OverlayLine> lines, LayoutBounds bounds) {
        int padding = 6;
        int currentX = bounds.x() + padding;
        int textY = bounds.y() + 4;

        for (int i = 0; i < lines.size(); i++) {
            OverlayLine line = lines.get(i);
            drawStyledText(context, renderer, activeConfig, line.label(), currentX, textY, getLabelColor(activeConfig));
            currentX += renderer.getWidth(line.label()) + 4;

            drawStyledText(context, renderer, activeConfig, line.value(), currentX, textY,
                    getAdaptiveColor(activeConfig, line));
            currentX += renderer.getWidth(line.value());

            if (!line.unit().isEmpty()) {
                currentX += 3;
                drawStyledText(context, renderer, activeConfig, line.unit(), currentX, textY, getUnitColor(activeConfig));
                currentX += renderer.getWidth(line.unit());
            }

            if (i < lines.size() - 1) {
                currentX += 6;
                drawStyledText(context, renderer, activeConfig, "|", currentX, textY, getDividerColor(activeConfig));
                currentX += renderer.getWidth("|") + 6;
            }
        }
    }

    private static void renderVertical(DrawContext context, TextRenderer renderer, ModConfig activeConfig,
            List<OverlayLine> lines, LayoutBounds bounds) {
        int padding = 6;
        int lineHeight = renderer.fontHeight + 2;
        int currentY = bounds.y() + padding;

        for (OverlayLine line : lines) {
            int currentX = bounds.x() + padding;
            drawStyledText(context, renderer, activeConfig, line.label(), currentX, currentY, getLabelColor(activeConfig));
            currentX += renderer.getWidth(line.label()) + 4;

            drawStyledText(context, renderer, activeConfig, line.value(), currentX, currentY,
                    getAdaptiveColor(activeConfig, line));
            currentX += renderer.getWidth(line.value());

            if (!line.unit().isEmpty()) {
                currentX += 3;
                drawStyledText(context, renderer, activeConfig, line.unit(), currentX, currentY, getUnitColor(activeConfig));
            }

            currentY += lineHeight;
        }
    }

    private static void renderGraph(DrawContext context, ModConfig activeConfig, LayoutBounds bounds, int[] values) {
        if (values.length < 2) {
            return;
        }

        int padding = 6;
        int graphTop = bounds.y() + bounds.height() - GRAPH_HEIGHT - padding;
        int graphLeft = bounds.x() + padding;
        int graphWidth = bounds.width() - (padding * 2);
        if (graphWidth < 2) {
            return;
        }
        int graphHeight = GRAPH_HEIGHT;
        int graphBottom = graphTop + graphHeight;

        context.fill(graphLeft, graphTop, graphLeft + graphWidth, graphBottom, 0x18000000);
        context.fill(graphLeft, graphTop, graphLeft + graphWidth, graphTop + 1, getDividerColor(activeConfig));

        int maxValue = 60;
        for (int value : values) {
            maxValue = Math.max(maxValue, value);
        }

        double step = (values.length - 1) > 0 ? (double) graphWidth / (values.length - 1) : graphWidth;
        for (int i = 1; i < values.length; i++) {
            int previousX = graphLeft + (int) Math.round((i - 1) * step);
            int currentX = graphLeft + (int) Math.round(i * step);

            int previousY = graphBottom - 1 - (int) Math.round((values[i - 1] / (double) maxValue) * (graphHeight - 3));
            int currentY = graphBottom - 1 - (int) Math.round((values[i] / (double) maxValue) * (graphHeight - 3));

            drawLine(context, previousX, previousY, currentX, currentY, getGoodColor(activeConfig));
        }
    }

    private static void drawLine(DrawContext context, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int steps = Math.max(dx, dy);
        if (steps == 0) {
            context.fill(x0, y0, x0 + 1, y0 + 1, color);
            return;
        }

        for (int i = 0; i <= steps; i++) {
            double progress = i / (double) steps;
            int x = x0 + (int) Math.round((x1 - x0) * progress);
            int y = y0 + (int) Math.round((y1 - y0) * progress);
            context.fill(x, y, x + 1, y + 1, color);
        }
    }

    private static LayoutBounds measureBounds(TextRenderer renderer, ModConfig activeConfig, List<OverlayLine> lines,
            int screenWidth, int screenHeight) {
        if (renderer == null) {
            AnchorPoint fallbackAnchor = getAnchorPoint(screenWidth, screenHeight, 160, 48, activeConfig.appearance.position);
            return new LayoutBounds(fallbackAnchor.x() + activeConfig.appearance.xOffset,
                    fallbackAnchor.y() + activeConfig.appearance.yOffset, 160, 48);
        }

        int padding = 6;
        int textWidth = 0;
        int lineHeight = renderer.fontHeight + 2;
        int lineCount = lines.size();

        if (activeConfig.appearance.overlayStyle == ModConfig.OverlayStyle.NAVBAR) {
            for (int i = 0; i < lines.size(); i++) {
                textWidth += measureLineWidth(renderer, lines.get(i));
                if (i < lines.size() - 1) {
                    textWidth += renderer.getWidth("|") + 12;
                }
            }
        } else {
            for (OverlayLine line : lines) {
                textWidth = Math.max(textWidth, measureLineWidth(renderer, line));
            }
        }

        int graphHeight = activeConfig.hud.showGraph ? GRAPH_HEIGHT + 6 : 0;
        int contentWidth = Math.max(textWidth, activeConfig.hud.showGraph ? 120 : 0);
        int width = contentWidth + (padding * 2);
        int height = (lineCount > 0 ? (lineHeight * Math.max(1, lineCount)) : lineHeight) + (padding * 2) + graphHeight;

        AnchorPoint anchor = getAnchorPoint(screenWidth, screenHeight, width, height, activeConfig.appearance.position);
        int x = anchor.x() + activeConfig.appearance.xOffset;
        int y = anchor.y() + activeConfig.appearance.yOffset;
        return new LayoutBounds(x, y, width, height);
    }

    private static int measureLineWidth(TextRenderer renderer, OverlayLine line) {
        int width = renderer.getWidth(line.label()) + 4 + renderer.getWidth(line.value());
        if (!line.unit().isEmpty()) {
            width += 3 + renderer.getWidth(line.unit());
        }
        return width;
    }

    private static List<OverlayLine> prepareLines(ModConfig activeConfig, PerformanceTracker tracker, boolean preview) {
        List<OverlayLine> lines = new ArrayList<>();
        for (OverlayMetric metric : OverlayMetric.sanitizeOrder(activeConfig.hud.metricOrder)) {
            if (!activeConfig.hud.isMetricEnabled(metric)) {
                continue;
            }

            OverlayLine line = preview ? createPreviewLine(metric) : createLiveLine(activeConfig, tracker, metric);
            if (line != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static OverlayLine createLiveLine(ModConfig activeConfig, PerformanceTracker tracker, OverlayMetric metric) {
        return switch (metric) {
            case FPS -> new OverlayLine(metric, translated(metric.getLabelKey()),
                    withMinMax(activeConfig, tracker.getCurrentFps(), tracker.getMinFps(), tracker.getMaxFps()),
                    translated(metric.getUnitKey()), Double.valueOf(tracker.getCurrentFps()));
            case AVG_FPS -> new OverlayLine(metric, translated(metric.getLabelKey()),
                    WHOLE_NUMBER.get().format(tracker.getAverageFps()), translated(metric.getUnitKey()),
                    tracker.getAverageFps());
            case FRAME_TIME -> new OverlayLine(metric, translated(metric.getLabelKey()),
                    ONE_DECIMAL.get().format(tracker.getCurrentFrameTimeMs()), translated(metric.getUnitKey()),
                    tracker.getCurrentFrameTimeMs());
            case LOW_1 -> new OverlayLine(metric, translated(metric.getLabelKey()),
                    String.valueOf(tracker.getOnePercentLow()), translated(metric.getUnitKey()),
                    Double.valueOf(tracker.getOnePercentLow()));
            case MEMORY -> new OverlayLine(metric, translated(metric.getLabelKey()),
                    tracker.getMaxMemory() > 0 ? ONE_DECIMAL.get().format(tracker.getUsedMemory() / (1024.0 * 1024.0 * 1024.0))
                            : "N/A",
                    translated(metric.getUnitKey()), tracker.getMaxMemory() > 0
                            ? (tracker.getUsedMemory() * 100.0 / tracker.getMaxMemory())
                            : null);
            case CPU -> new OverlayLine(metric, translated(metric.getLabelKey()), formatPercent(tracker.getCurrentCpuUsage()),
                    tracker.getCurrentCpuUsage() >= 0 ? translated(metric.getUnitKey()) : "", tracker.getCurrentCpuUsage());
            case GPU -> new OverlayLine(metric, translated(metric.getLabelKey()), formatPercent(tracker.getCurrentGpuUsage()),
                    tracker.getCurrentGpuUsage() >= 0 ? translated(metric.getUnitKey()) : "", tracker.getCurrentGpuUsage());
            case PING -> new OverlayLine(metric, translated(metric.getLabelKey()),
                    withMinMax(activeConfig, tracker.getCurrentPing(), tracker.getMinPing(), tracker.getMaxPing()),
                    translated(metric.getUnitKey()), Double.valueOf(tracker.getCurrentPing()));
            case MSPT -> new OverlayLine(metric, translated(metric.getLabelKey()),
                    ONE_DECIMAL.get().format(tracker.getMspt()), translated(metric.getUnitKey()), tracker.getMspt());
            case TPS -> new OverlayLine(metric, translated(metric.getLabelKey()),
                    ONE_DECIMAL.get().format(tracker.getTps()), translated(metric.getUnitKey()), tracker.getTps());
            case CHUNKS -> new OverlayLine(metric, translated(metric.getLabelKey()),
                    tracker.getVisibleChunks() > 0
                            ? tracker.getCompletedChunks() + "/" + tracker.getVisibleChunks()
                            : String.valueOf(tracker.getLoadedChunks()),
                    "", tracker.getVisibleChunks() > 0
                            ? (tracker.getCompletedChunks() * 100.0 / tracker.getVisibleChunks())
                            : null);
            case COORDS -> new OverlayLine(metric, translated(metric.getLabelKey()), tracker.getCoordinatesText(), "", null);
            case BIOME -> new OverlayLine(metric, translated(metric.getLabelKey()), tracker.getBiomeText(), "", null);
        };
    }

    private static OverlayLine createPreviewLine(OverlayMetric metric) {
        return switch (metric) {
            case FPS -> new OverlayLine(metric, translated(metric.getLabelKey()), "144", translated(metric.getUnitKey()),
                    144.0);
            case AVG_FPS -> new OverlayLine(metric, translated(metric.getLabelKey()), "138", translated(metric.getUnitKey()),
                    138.0);
            case FRAME_TIME -> new OverlayLine(metric, translated(metric.getLabelKey()), "6.9", translated(metric.getUnitKey()),
                    6.9);
            case LOW_1 -> new OverlayLine(metric, translated(metric.getLabelKey()), "92", translated(metric.getUnitKey()),
                    92.0);
            case MEMORY -> new OverlayLine(metric, translated(metric.getLabelKey()), "3.4", translated(metric.getUnitKey()),
                    58.0);
            case CPU -> new OverlayLine(metric, translated(metric.getLabelKey()), "34", translated(metric.getUnitKey()), 34.0);
            case GPU -> new OverlayLine(metric, translated(metric.getLabelKey()), "67", translated(metric.getUnitKey()), 67.0);
            case PING -> new OverlayLine(metric, translated(metric.getLabelKey()), "42", translated(metric.getUnitKey()), 42.0);
            case MSPT -> new OverlayLine(metric, translated(metric.getLabelKey()), "18.7", translated(metric.getUnitKey()),
                    18.7);
            case TPS -> new OverlayLine(metric, translated(metric.getLabelKey()), "20.0", translated(metric.getUnitKey()),
                    20.0);
            case CHUNKS -> new OverlayLine(metric, translated(metric.getLabelKey()), "324/361", "", 90.0);
            case COORDS -> new OverlayLine(metric, translated(metric.getLabelKey()), "128 64 -52", "", null);
            case BIOME -> new OverlayLine(metric, translated(metric.getLabelKey()), "Plains", "", null);
        };
    }

    private static String withMinMax(ModConfig activeConfig, int current, int min, int max) {
        if (!activeConfig.hud.showMinMaxStats) {
            return String.valueOf(current);
        }
        return current + " (" + min + "/" + max + ")";
    }

    private static String formatPercent(double value) {
        if (value < 0) {
            return "N/A";
        }
        return WHOLE_NUMBER.get().format(value);
    }

    private static String translated(String key) {
        return key == null || key.isEmpty() ? "" : Text.translatable(key).getString();
    }

    private static void drawStyledText(DrawContext context, TextRenderer renderer, ModConfig activeConfig, String text,
            int x, int y, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }

        ModConfig.TextEffect effect = activeConfig != null ? activeConfig.appearance.textEffect : ModConfig.TextEffect.NONE;
        switch (effect) {
            case SHADOW -> context.drawTextWithShadow(renderer, text, x, y, color);
            case OUTLINE -> {
                int outlineColor = 0xD0000000;
                context.drawText(renderer, text, x - 1, y, outlineColor, false);
                context.drawText(renderer, text, x + 1, y, outlineColor, false);
                context.drawText(renderer, text, x, y - 1, outlineColor, false);
                context.drawText(renderer, text, x, y + 1, outlineColor, false);
                context.drawText(renderer, text, x, y, color, false);
            }
            case NONE -> context.drawText(renderer, text, x, y, color, false);
        }
    }

    private static int getAdaptiveColor(ModConfig activeConfig, OverlayLine line) {
        if (!activeConfig.appearance.adaptiveColors || line.adaptiveValue() == null) {
            return getValueColor(activeConfig);
        }

        double value = line.adaptiveValue();
        return switch (line.metric()) {
            case FPS, AVG_FPS, LOW_1 -> value >= 60 ? getGoodColor(activeConfig)
                    : value >= 30 ? getWarningColor(activeConfig) : getBadColor(activeConfig);
            case FRAME_TIME, MSPT -> value < 16.7 ? getGoodColor(activeConfig)
                    : value < 33.3 ? getWarningColor(activeConfig) : getBadColor(activeConfig);
            case MEMORY, CPU, GPU, CHUNKS -> value < 75 ? getGoodColor(activeConfig)
                    : value < 90 ? getWarningColor(activeConfig) : getBadColor(activeConfig);
            case PING -> value < 60 ? getGoodColor(activeConfig)
                    : value < 150 ? getWarningColor(activeConfig) : getBadColor(activeConfig);
            case TPS -> value >= 19.5 ? getGoodColor(activeConfig)
                    : value >= 15 ? getWarningColor(activeConfig) : getBadColor(activeConfig);
            case COORDS, BIOME -> getValueColor(activeConfig);
        };
    }

    private static int getBackgroundColor(ModConfig activeConfig) {
        return ((activeConfig.appearance.backgroundOpacity & 0xFF) << 24)
                | (activeConfig.appearance.backgroundColor & 0xFFFFFF);
    }

    private static int getLabelColor(ModConfig activeConfig) {
        return activeConfig.appearance.labelColor;
    }

    private static int getValueColor(ModConfig activeConfig) {
        return activeConfig.appearance.valueColor;
    }

    private static int getUnitColor(ModConfig activeConfig) {
        return activeConfig.appearance.unitColor;
    }

    private static int getDividerColor(ModConfig activeConfig) {
        return activeConfig.appearance.dividerColor;
    }

    private static int getGoodColor(ModConfig activeConfig) {
        return activeConfig.appearance.goodColor;
    }

    private static int getWarningColor(ModConfig activeConfig) {
        return activeConfig.appearance.warningColor;
    }

    private static int getBadColor(ModConfig activeConfig) {
        return activeConfig.appearance.badColor;
    }

    private static int[] getPreviewGraphValues() {
        return new int[] { 120, 144, 140, 138, 147, 145, 141, 130, 136, 142, 144, 139, 148, 150, 143, 140, 137, 145,
                149, 146, 142, 141, 147, 144 };
    }

    private static void applyScale(DrawContext context, float scale, Runnable renderer) {
        Object rawMatrices = context.getMatrices();
        if (rawMatrices instanceof MatrixStack matrices) {
            matrices.push();
            matrices.scale(scale, scale, 1.0f);
            renderer.run();
            matrices.pop();
        } else if (rawMatrices instanceof Matrix3x2fStack matrices) {
            matrices.pushMatrix();
            matrices.scale(scale, scale);
            renderer.run();
            matrices.popMatrix();
        } else {
            renderer.run();
        }
    }

    private static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

        drawCorner(context, x, y, radius, radius, true, true, color);
        drawCorner(context, x + width - radius, y, radius, radius, false, true, color);
        drawCorner(context, x, y + height - radius, radius, radius, true, false, color);
        drawCorner(context, x + width - radius, y + height - radius, radius, radius, false, false, color);
    }

    private static void drawCorner(DrawContext context, int x, int y, int width, int height, boolean left, boolean top,
            int color) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int dx = left ? (width - 1 - i) : i;
                int dy = top ? (height - 1 - j) : j;
                if (dx * dx + dy * dy <= width * width) {
                    context.fill(x + i, y + j, x + i + 1, y + j + 1, color);
                }
            }
        }
    }

    private record OverlayLine(OverlayMetric metric, String label, String value, String unit, Double adaptiveValue) {
    }

    public record LayoutBounds(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    public record AnchorPoint(int x, int y) {
    }
}
