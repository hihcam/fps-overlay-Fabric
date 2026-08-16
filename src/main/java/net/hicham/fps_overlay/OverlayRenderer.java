package net.hicham.fps_overlay;

import org.joml.Matrix3x2fStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OverlayRenderer {
    private static ModConfig config;

    private static final ThreadLocal<DecimalFormat> ONE_DECIMAL =
            ThreadLocal.withInitial(() -> new DecimalFormat("0.0"));
    private static final ThreadLocal<DecimalFormat> WHOLE_NUMBER =
            ThreadLocal.withInitial(() -> new DecimalFormat("0"));
    private static final int GRAPH_HEIGHT = 24;
    private static float smoothGraphMax = 60f;

    public static void resetGraphScale() {
        smoothGraphMax = 60f;
    }

    public static void setConfig(ModConfig configData) {
        config = configData;
    }

    public static void render(GuiGraphicsExtractor context, Minecraft client) {
        if (config == null || !config.general.enabled) {
            return;
        }

        PerformanceTracker tracker = PerformanceTracker.getInstance();

        if (config.appearance.autoHideF3 && client.getDebugOverlay().showDebugScreen()) {
            return;
        }

        List<OverlayLine> lines = prepareLines(config, tracker, false);
        if (lines.isEmpty() && !config.hud.showGraph) {
            return;
        }

        float scale = config.appearance.hudScale;
        applyScale(context, scale, () -> renderScaled(context, client, config, lines, false));
    }

    public static void renderPreview(GuiGraphicsExtractor context, Minecraft client, ModConfig previewConfig, int screenWidth,
            int screenHeight) {
        if (previewConfig == null) {
            return;
        }

        List<OverlayLine> lines = prepareLines(previewConfig, PerformanceTracker.getInstance(), true);
        float scale = previewConfig.appearance.hudScale;
        applyScale(context, scale, () -> renderScaled(context, client, previewConfig, lines, true));
    }

    public static LayoutBounds getPreviewBounds(int screenWidth, int screenHeight, ModConfig previewConfig) {
        Minecraft client = Minecraft.getInstance();
        Font renderer = client != null ? client.font : null;
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
        Minecraft client = Minecraft.getInstance();
        Font renderer = client != null ? client.font : null;
        List<OverlayLine> lines = prepareLines(previewConfig, PerformanceTracker.getInstance(), true);
        return getPreviewLogicalBounds(screenWidth, screenHeight, previewConfig, renderer, lines);
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

    private static LayoutBounds getPreviewLogicalBounds(int screenWidth, int screenHeight, ModConfig previewConfig,
            Font renderer, List<OverlayLine> lines) {
        float scale = previewConfig.appearance.hudScale;
        int logicalWidth = Math.max(1, Math.round(screenWidth / scale));
        int logicalHeight = Math.max(1, Math.round(screenHeight / scale));
        return measureBounds(renderer, previewConfig, lines, logicalWidth, logicalHeight);
    }

    private static void renderScaled(GuiGraphicsExtractor context, Minecraft client, ModConfig activeConfig,
            List<OverlayLine> lines, boolean preview) {
        Font renderer = client != null ? client.font : null;
        if (renderer == null) {
            return;
        }

        int screenWidth = (int) (context.guiWidth() / activeConfig.appearance.hudScale);
        int screenHeight = (int) (context.guiHeight() / activeConfig.appearance.hudScale);
        OverlayLayout layout = measureLayout(renderer, activeConfig, lines, screenWidth, screenHeight);
        LayoutBounds bounds = layout.bounds();

        if (activeConfig.appearance.showBackground) {
            drawRoundedRect(context, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 4,
                    getBackgroundColor(activeConfig));
        }

        if (activeConfig.appearance.overlayStyle == ModConfig.OverlayStyle.NAVBAR) {
            renderNavbar(context, renderer, activeConfig, layout.navbarRows(), bounds);
        } else {
            renderVertical(context, renderer, activeConfig, lines, bounds);
        }

        if (activeConfig.hud.showGraph) {
            int[] graphValues = preview ? getPreviewGraphValues() : PerformanceTracker.getInstance().copyGraphValues();
            renderGraph(context, activeConfig, bounds, graphValues);
        }
    }

    private static void renderNavbar(GuiGraphicsExtractor context, Font renderer, ModConfig activeConfig,
            List<NavbarRow> rows, LayoutBounds bounds) {
        int padding = 6;
        int lineHeight = renderer.lineHeight + 2;

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            NavbarRow row = rows.get(rowIndex);
            int currentX = bounds.x() + padding;
            int textY = bounds.y() + padding + (rowIndex * lineHeight);

            for (int i = 0; i < row.lines().size(); i++) {
                OverlayLine line = row.lines().get(i);
                if (activeConfig.hud.showLabels && !line.label().isEmpty()) {
                    drawStyledText(context, renderer, activeConfig, line.label(), currentX, textY, getLabelColor(activeConfig));
                    currentX += renderer.width(line.label()) + 4;
                }

                drawStyledText(context, renderer, activeConfig, line.value(), currentX, textY,
                        getAdaptiveColor(activeConfig, line));
                currentX += renderer.width(line.value());

                if (!line.unit().isEmpty()) {
                    currentX += 3;
                    drawStyledText(context, renderer, activeConfig, line.unit(), currentX, textY,
                            getUnitColor(activeConfig));
                    currentX += renderer.width(line.unit());
                }

                if (i < row.lines().size() - 1) {
                    currentX += 6;
                    drawStyledText(context, renderer, activeConfig, "|", currentX, textY, getDividerColor(activeConfig));
                    currentX += renderer.width("|") + 6;
                }
            }
        }
    }

    private static void renderVertical(GuiGraphicsExtractor context, Font renderer, ModConfig activeConfig,
            List<OverlayLine> lines, LayoutBounds bounds) {
        int padding = 6;
        int lineHeight = renderer.lineHeight + 2;
        int currentY = bounds.y() + padding;

        for (OverlayLine line : lines) {
            int currentX = bounds.x() + padding;
            if (activeConfig.hud.showLabels && !line.label().isEmpty()) {
                drawStyledText(context, renderer, activeConfig, line.label(), currentX, currentY, getLabelColor(activeConfig));
                currentX += renderer.width(line.label()) + 4;
            }

            drawStyledText(context, renderer, activeConfig, line.value(), currentX, currentY,
                    getAdaptiveColor(activeConfig, line));
            currentX += renderer.width(line.value());

            if (!line.unit().isEmpty()) {
                currentX += 3;
                drawStyledText(context, renderer, activeConfig, line.unit(), currentX, currentY, getUnitColor(activeConfig));
            }

            currentY += lineHeight;
        }
    }

    private static void renderGraph(GuiGraphicsExtractor context, ModConfig activeConfig, LayoutBounds bounds, int[] values) {
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

        float targetMax = 60f;
        for (int value : values) {
            targetMax = Math.max(targetMax, (float) value);
        }
        smoothGraphMax += (targetMax - smoothGraphMax) * 0.1f;

        int graphColor = getGoodColor(activeConfig);
        double step = (double) graphWidth / (values.length - 1);
        for (int i = 0; i < values.length; i++) {
            int barHeight = (int) Math.round((values[i] / (double) smoothGraphMax) * (graphHeight - 3));
            if (barHeight <= 0) {
                continue;
            }

            int x = graphLeft + (int) Math.round(i * step);
            int nextX = (i < values.length - 1)
                    ? graphLeft + (int) Math.round((i + 1) * step)
                    : x + 1;
            int barWidth = Math.max(1, nextX - x);
            context.fill(x, graphBottom - 1 - barHeight, x + barWidth, graphBottom - 1, graphColor);
        }
    }

    private static LayoutBounds measureBounds(Font renderer, ModConfig activeConfig, List<OverlayLine> lines,
            int screenWidth, int screenHeight) {
        return measureLayout(renderer, activeConfig, lines, screenWidth, screenHeight).bounds();
    }

    private static OverlayLayout measureLayout(Font renderer, ModConfig activeConfig, List<OverlayLine> lines,
            int screenWidth, int screenHeight) {
        if (renderer == null) {
            AnchorPoint fallbackAnchor = getAnchorPoint(screenWidth, screenHeight, 160, 48, activeConfig.appearance.position);
            return new OverlayLayout(
                    new LayoutBounds(fallbackAnchor.x() + activeConfig.appearance.xOffset,
                            fallbackAnchor.y() + activeConfig.appearance.yOffset, 160, 48),
                    List.of());
        }

        int padding = 6;
        int textWidth = 0;
        int lineHeight = renderer.lineHeight + 2;
        int lineCount = lines.size();
        List<NavbarRow> navbarRows = List.of();

        if (activeConfig.appearance.overlayStyle == ModConfig.OverlayStyle.NAVBAR) {
            int maxContentWidth = Math.max(40, screenWidth - 8 - (padding * 2));
            navbarRows = layoutNavbarRows(renderer, lines, maxContentWidth, activeConfig);
            lineCount = Math.max(1, navbarRows.size());
            for (NavbarRow row : navbarRows) {
                textWidth = Math.max(textWidth, row.width());
            }
        } else {
            for (OverlayLine line : lines) {
                textWidth = Math.max(textWidth, measureLineWidth(renderer, line, activeConfig));
            }
        }

        int graphHeight = activeConfig.hud.showGraph ? GRAPH_HEIGHT + 6 : 0;
        int contentWidth = Math.max(textWidth, activeConfig.hud.showGraph ? 120 : 0);
        int width = contentWidth + (padding * 2);
        int contentHeight = activeConfig.appearance.overlayStyle == ModConfig.OverlayStyle.NAVBAR
                ? (lineHeight * Math.max(1, lineCount))
                : (lineCount > 0 ? (lineHeight * Math.max(1, lineCount)) : lineHeight);
        int height = contentHeight + (padding * 2) + graphHeight;

        AnchorPoint anchor = getAnchorPoint(screenWidth, screenHeight, width, height, activeConfig.appearance.position);
        int maxX = Math.max(0, screenWidth - width);
        int maxY = Math.max(0, screenHeight - height);
        int x = Math.max(0, Math.min(anchor.x() + activeConfig.appearance.xOffset, maxX));
        int y = Math.max(0, Math.min(anchor.y() + activeConfig.appearance.yOffset, maxY));
        return new OverlayLayout(new LayoutBounds(x, y, width, height), navbarRows);
    }

    private static List<NavbarRow> layoutNavbarRows(Font renderer, List<OverlayLine> lines, int maxContentWidth, ModConfig activeConfig) {
        List<NavbarRow> rows = new ArrayList<>();
        if (lines.isEmpty()) {
            return rows;
        }

        int separatorWidth = renderer.width("|") + 12;
        List<OverlayLine> currentRow = new ArrayList<>();
        int currentWidth = 0;

        for (OverlayLine line : lines) {
            int lineWidth = measureLineWidth(renderer, line, activeConfig);
            int additionalWidth = currentRow.isEmpty() ? lineWidth : separatorWidth + lineWidth;

            if (!currentRow.isEmpty() && currentWidth + additionalWidth > maxContentWidth) {
                rows.add(new NavbarRow(List.copyOf(currentRow), currentWidth));
                currentRow.clear();
                currentWidth = 0;
                additionalWidth = lineWidth;
            }

            currentRow.add(line);
            currentWidth += additionalWidth;
        }

        if (!currentRow.isEmpty()) {
            rows.add(new NavbarRow(List.copyOf(currentRow), currentWidth));
        }

        return rows;
    }

    private static int measureLineWidth(Font renderer, OverlayLine line, ModConfig activeConfig) {
        int width = 0;
        if (activeConfig.hud.showLabels && !line.label().isEmpty()) {
            width += renderer.width(line.label()) + 4;
        }
        width += renderer.width(line.value());
        if (!line.unit().isEmpty()) {
            width += 3 + renderer.width(line.unit());
        }
        return width;
    }

    private static List<OverlayLine> prepareLines(ModConfig activeConfig, PerformanceTracker tracker, boolean preview) {
        List<OverlayLine> lines = new ArrayList<>();
        for (OverlayMetric metric : OverlayMetric.sanitizeOrder(activeConfig.hud.metricOrder)) {
            if (!activeConfig.hud.isMetricEnabled(metric)) {
                continue;
            }

            OverlayLine line = preview ? createPreviewLine(activeConfig, metric) : createLiveLine(activeConfig, tracker, metric);
            if (line != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static OverlayLine createLiveLine(ModConfig activeConfig, PerformanceTracker tracker, OverlayMetric metric) {
        String metricLabel = activeConfig.hud.getMetricDisplayName(metric);
        return switch (metric) {
            case FPS -> new OverlayLine(metric, metricLabel,
                    withMinMax(activeConfig, tracker.getCurrentFps(), tracker.getMinFps(), tracker.getMaxFps()),
                    translated(metric.getUnitKey()), (double) tracker.getCurrentFps());
            case AVG_FPS -> new OverlayLine(metric, metricLabel,
                    WHOLE_NUMBER.get().format(tracker.getAverageFps()), translated(metric.getUnitKey()),
                    tracker.getAverageFps());
            case FRAME_TIME -> new OverlayLine(metric, metricLabel,
                    ONE_DECIMAL.get().format(tracker.getCurrentFrameTimeMs()), translated(metric.getUnitKey()),
                    tracker.getCurrentFrameTimeMs());
            case LOW_1 -> new OverlayLine(metric, metricLabel,
                    String.valueOf(tracker.getOnePercentLow()), translated(metric.getUnitKey()),
                    (double) tracker.getOnePercentLow());
            case MEMORY -> new OverlayLine(metric, metricLabel,
                    tracker.getMaxMemory() > 0
                            ? ONE_DECIMAL.get().format(tracker.getUsedMemory() / (1024.0 * 1024.0 * 1024.0))
                            : "N/A",
                    translated(metric.getUnitKey()), tracker.getMaxMemory() > 0
                            ? (tracker.getUsedMemory() * 100.0 / tracker.getMaxMemory())
                            : null);
            case PING -> new OverlayLine(metric, metricLabel,
                    withMinMax(activeConfig, tracker.getCurrentPing(), tracker.getMinPing(), tracker.getMaxPing()),
                    translated(metric.getUnitKey()), (double) tracker.getCurrentPing());
            case MSPT -> new OverlayLine(metric, metricLabel,
                    tracker.getMspt() >= 0 ? ONE_DECIMAL.get().format(tracker.getMspt()) : "N/A",
                    translated(metric.getUnitKey()), tracker.getMspt());
            case TPS -> new OverlayLine(metric, metricLabel,
                    tracker.getTps() >= 0 ? ONE_DECIMAL.get().format(tracker.getTps()) : "N/A",
                    translated(metric.getUnitKey()), tracker.getTps());
            case CHUNKS -> new OverlayLine(metric, metricLabel,
                    tracker.getVisibleChunks() > 0
                            ? tracker.getCompletedChunks() + "/" + tracker.getVisibleChunks()
                            : String.valueOf(tracker.getLoadedChunks()),
                    "", tracker.getVisibleChunks() > 0
                            ? (tracker.getCompletedChunks() * 100.0 / tracker.getVisibleChunks())
                            : null);
            case COORDS -> new OverlayLine(metric, metricLabel, tracker.getCoordinatesText(), "", null);
            case BIOME -> new OverlayLine(metric, metricLabel, tracker.getBiomeText(), "", null);
        };
    }

    private static OverlayLine createPreviewLine(ModConfig activeConfig, OverlayMetric metric) {
        String metricLabel = activeConfig.hud.getMetricDisplayName(metric);
        return switch (metric) {
            case FPS -> new OverlayLine(metric, metricLabel, "144", translated(metric.getUnitKey()), 144.0);
            case AVG_FPS -> new OverlayLine(metric, metricLabel, "138", translated(metric.getUnitKey()), 138.0);
            case FRAME_TIME -> new OverlayLine(metric, metricLabel, "6.9", translated(metric.getUnitKey()), 6.9);
            case LOW_1 -> new OverlayLine(metric, metricLabel, "92", translated(metric.getUnitKey()), 92.0);
            case MEMORY -> new OverlayLine(metric, metricLabel, "3.4", translated(metric.getUnitKey()), 58.0);
            case PING -> new OverlayLine(metric, metricLabel, "42", translated(metric.getUnitKey()), 42.0);
            case MSPT -> new OverlayLine(metric, metricLabel, "18.7", translated(metric.getUnitKey()), 18.7);
            case TPS -> new OverlayLine(metric, metricLabel, "20.0", translated(metric.getUnitKey()), 20.0);
            case CHUNKS -> new OverlayLine(metric, metricLabel, "324/361", "", 90.0);
            case COORDS -> new OverlayLine(metric, metricLabel, "128 64 -52", "", null);
            case BIOME -> new OverlayLine(metric, metricLabel, "Plains", "", null);
        };
    }

    private static String withMinMax(ModConfig activeConfig, int current, int min, int max) {
        if (!activeConfig.hud.showMinMaxStats) {
            return String.valueOf(current);
        }
        return current + " (" + min + "/" + max + ")";
    }

    private static String translated(String key) {
        return key == null || key.isEmpty() ? "" : Component.translatable(key).getString();
    }

    private static void drawStyledText(GuiGraphicsExtractor context, Font renderer, ModConfig activeConfig, String text,
            int x, int y, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }

        ModConfig.TextEffect effect = activeConfig != null ? activeConfig.appearance.textEffect : ModConfig.TextEffect.NONE;
        switch (effect) {
            case SHADOW -> context.text(renderer, text, x, y, color, true);
            case OUTLINE -> {
                int outlineColor = 0xD0000000;
                context.text(renderer, text, x - 1, y, outlineColor, false);
                context.text(renderer, text, x + 1, y, outlineColor, false);
                context.text(renderer, text, x, y - 1, outlineColor, false);
                context.text(renderer, text, x, y + 1, outlineColor, false);
                context.text(renderer, text, x, y, color, false);
            }
            case NONE -> context.text(renderer, text, x, y, color, false);
        }
    }

    private static int getAdaptiveColor(ModConfig activeConfig, OverlayLine line) {
        if (!activeConfig.appearance.adaptiveColors || line.adaptiveValue() == null) {
            return getValueColor(activeConfig);
        }

        double value = line.adaptiveValue();
        ModConfig.Thresholds thresholds = activeConfig.thresholds != null ? activeConfig.thresholds : new ModConfig.Thresholds();

        return switch (line.metric()) {
            case FPS, AVG_FPS, LOW_1 -> value >= thresholds.fpsGood ? getGoodColor(activeConfig)
                    : value >= thresholds.fpsWarning ? getWarningColor(activeConfig) : getBadColor(activeConfig);
            case FRAME_TIME, MSPT -> value < thresholds.frameTimeGood ? getGoodColor(activeConfig)
                    : value < thresholds.frameTimeWarning ? getWarningColor(activeConfig) : getBadColor(activeConfig);
            case MEMORY, CHUNKS -> value < thresholds.memoryGood ? getGoodColor(activeConfig)
                    : value < thresholds.memoryWarning ? getWarningColor(activeConfig) : getBadColor(activeConfig);
            case PING -> value < thresholds.pingGood ? getGoodColor(activeConfig)
                    : value < thresholds.pingWarning ? getWarningColor(activeConfig) : getBadColor(activeConfig);
            case TPS -> value >= thresholds.tpsGood ? getGoodColor(activeConfig)
                    : value >= thresholds.tpsWarning ? getWarningColor(activeConfig) : getBadColor(activeConfig);
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

    private static void applyScale(GuiGraphicsExtractor context, float scale, Runnable renderer) {
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.scale(scale, scale);
        renderer.run();
        matrices.popMatrix();
    }

    private static void drawRoundedRect(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

        drawCorner(context, x, y, radius, radius, true, true, color);
        drawCorner(context, x + width - radius, y, radius, radius, false, true, color);
        drawCorner(context, x, y + height - radius, radius, radius, true, false, color);
        drawCorner(context, x + width - radius, y + height - radius, radius, radius, false, false, color);
    }

    private static void drawCorner(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean left, boolean top,
            int color) {
        int radius = Math.max(width, height);
        int r2 = radius * radius;
        for (int j = 0; j < height; j++) {
            int dy = top ? (height - 1 - j) : j;
            // Compute horizontal span directly from circle equation: dx² + dy² <= r²
            int maxDx2 = r2 - dy * dy;
            if (maxDx2 < 0) continue;
            int maxDx = (int) Math.sqrt(maxDx2);
            int fillWidth = Math.min(maxDx + 1, width);
            int spanStart = left ? (width - fillWidth) : 0;
            int spanEnd = left ? width : fillWidth;
            context.fill(x + spanStart, y + j, x + spanEnd, y + j + 1, color);
        }
    }

    private record OverlayLine(OverlayMetric metric, String label, String value, String unit, Double adaptiveValue) {
    }

    private record NavbarRow(List<OverlayLine> lines, int width) {
    }

    private record OverlayLayout(LayoutBounds bounds, List<NavbarRow> navbarRows) {
    }

    public record LayoutBounds(int x, int y, int width, int height) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    public record AnchorPoint(int x, int y) {
    }
}
