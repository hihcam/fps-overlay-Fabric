package net.hicham.fps_overlay;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverlayRenderer {
    private static ModConfig config;

    private static final int COLOR_EXCELLENT = 0xFF00FF00;
    private static final int COLOR_GOOD = 0xFFFFFF00;
    private static final int COLOR_WARNING = 0xFFFFA500;
    private static final int COLOR_CRITICAL = 0xFFFF0000;
    private static final int COLOR_NEUTRAL = 0xFFFFFFFF;
    private static final int COLOR_BACKGROUND = 0x80000000;

    private static final int COLOR_AVG_EXCELLENT = 0xFF00FFFF;
    private static final int COLOR_AVG_GOOD = 0xFF00FF00;
    private static final int COLOR_AVG_WARNING = 0xFFFFFF00;
    private static final int COLOR_AVG_CRITICAL = 0xFFFF0000;

    @SuppressWarnings("null")
    private static final ThreadLocal<DecimalFormat> MEMORY_FORMAT = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.0"));
    @SuppressWarnings("null")
    private static final ThreadLocal<DecimalFormat> PERCENT_FORMAT = ThreadLocal
            .withInitial(() -> new DecimalFormat("0"));
    @SuppressWarnings("null")
    private static final ThreadLocal<DecimalFormat> AVG_FPS_FORMAT = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.0"));

    private static final Map<String, Text> TEXT_CACHE = new HashMap<>();
    private static boolean textCacheInitialized = false;

    private static final List<OverlayLine> REUSABLE_LINES = new ArrayList<>(6);

    static {
        for (int i = 0; i < 6; i++) {
            REUSABLE_LINES.add(new OverlayLine());
        }
    }

    public static void setConfig(ModConfig configData) {
        config = configData;
    }

    public static void render(DrawContext ctx, MinecraftClient client) {
        if (config == null || !config.general.enabled)
            return;

        ensureTextCacheInitialized();

        List<OverlayLine> linesToRender = prepareLines();
        if (linesToRender.isEmpty())
            return;

        renderLines(ctx, client, linesToRender);
    }

    private static void ensureTextCacheInitialized() {
        if (!textCacheInitialized) {
            TEXT_CACHE.put("fps", Text.translatable("text.fps_overlay.fps"));
            TEXT_CACHE.put("avg_fps", Text.translatable("text.fps_overlay.avg_fps"));
            TEXT_CACHE.put("memory", Text.translatable("text.fps_overlay.memory"));
            TEXT_CACHE.put("ping", Text.translatable("text.fps_overlay.ping"));
            TEXT_CACHE.put("ms", Text.translatable("text.fps_overlay.ms"));
            TEXT_CACHE.put("gb", Text.translatable("text.fps_overlay.gb"));
            TEXT_CACHE.put("mb", Text.translatable("text.fps_overlay.mb"));
            TEXT_CACHE.put("used", Text.translatable("text.fps_overlay.used"));
            TEXT_CACHE.put("total", Text.translatable("text.fps_overlay.total"));

            textCacheInitialized = true;
        }
    }

    private static List<OverlayLine> prepareLines() {
        List<OverlayLine> activeLines = new ArrayList<>(4);
        PerformanceTracker tracker = PerformanceTracker.getInstance();
        int lineIndex = 0;

        if (config.hud.showFps) {
            OverlayLine line = REUSABLE_LINES.get(lineIndex++);
            line.reset();
            updateFpsLine(line, tracker);
            activeLines.add(line);
        }

        if (config.hud.showAverageFps) {
            OverlayLine line = REUSABLE_LINES.get(lineIndex++);
            line.reset();
            updateAverageFpsLine(line, tracker);
            activeLines.add(line);
        }

        if (config.hud.showMemory) {
            OverlayLine line = REUSABLE_LINES.get(lineIndex++);
            line.reset();
            updateMemoryLine(line, tracker);
            activeLines.add(line);
        }

        if (config.hud.showPing) {
            OverlayLine line = REUSABLE_LINES.get(lineIndex++);
            line.reset();
            updatePingLine(line, tracker);
            activeLines.add(line);
        }

        return activeLines;
    }

    private static void updateFpsLine(OverlayLine line, PerformanceTracker tracker) {
        int fps = tracker.getCurrentFps();
        int fpsColor = getColorForFps(fps);

        line.addPart(Text.literal(String.valueOf(fps)), fpsColor);
        line.addSpacer(Text.literal(" "));
        line.addPart(TEXT_CACHE.get("fps"), config.appearance.useAdaptiveColors ? fpsColor : getTextColor());
    }

    private static void updateAverageFpsLine(OverlayLine line, PerformanceTracker tracker) {
        double actualValue = tracker.getAverageFps();
        int avgFpsColor = getColorForAverageFps((int) Math.round(actualValue));

        line.addPart(Text.literal(AVG_FPS_FORMAT.get().format(actualValue)), avgFpsColor);
        line.addSpacer(Text.literal(" "));
        line.addPart(TEXT_CACHE.get("avg_fps"), config.appearance.useAdaptiveColors ? avgFpsColor : getTextColor());
    }

    private static void updateMemoryLine(OverlayLine line, PerformanceTracker tracker) {
        long used = tracker.getUsedMemory();
        long max = tracker.getMaxMemory();

        if (max == 0) {
            line.addPart(Text.literal("N/A"), getTextColor());
            return;
        }

        double usedGB = used / (1024.0 * 1024.0 * 1024.0);
        double maxGB = max / (1024.0 * 1024.0 * 1024.0);
        double usagePercent = (double) used / max * 100;

        int memoryColor = getColorForMemory(usagePercent);

        line.addPart(Text.literal(MEMORY_FORMAT.get().format(usedGB)), memoryColor);
        line.addSpacer(Text.literal("/"));
        line.addPart(Text.literal(MEMORY_FORMAT.get().format(maxGB)), getTextColor());
        line.addSpacer(Text.literal(" "));
        line.addPart(TEXT_CACHE.get("gb"), getTextColor());

        if (config.hud.showMemoryPercentage) {
            line.addSpacer(Text.literal(" ("));
            line.addPart(Text.literal(PERCENT_FORMAT.get().format(usagePercent) + "%"), memoryColor);
            line.addSpacer(Text.literal(")"));
        }
    }

    private static void updatePingLine(OverlayLine line, PerformanceTracker tracker) {
        int ping = tracker.getCurrentPing();
        int pingColor = getColorForPing(ping);

        line.addPart(Text.literal(String.valueOf(ping)), pingColor);
        line.addSpacer(Text.literal(" "));
        line.addPart(TEXT_CACHE.get("ms"), getTextColor());
    }

    private static void renderLines(DrawContext ctx, MinecraftClient client, List<OverlayLine> lines) {
        TextRenderer renderer = client.textRenderer;
        int verticalPadding = config.appearance.compactMode ? 2 : 4;
        int lineHeight = renderer.fontHeight + verticalPadding;
        int maxWidth = calculateMaxWidth(renderer, lines);
        int totalHeight = (lines.size() * lineHeight) - (config.appearance.compactMode ? 0 : 2);

        if (config.appearance.overlayStyle == ModConfig.OverlayStyle.MODERN) {
            maxWidth += 8;
            totalHeight += 4;
        }

        int screenWidth = ctx.getScaledWindowWidth();
        int screenHeight = ctx.getScaledWindowHeight();

        int[] position = calculatePosition(screenWidth, screenHeight, maxWidth, totalHeight);
        int x = position[0];
        int y = position[1];

        float scale = config.appearance.scale;
        Object matricesRaw = ctx.getMatrices();

        if (matricesRaw instanceof MatrixStack matrices) {
            matrices.push();
            if (scale != 1.0f) {
                matrices.scale(scale, scale, 1.0f);
            }

            int scaledX = (int) (x / scale);
            int scaledY = (int) (y / scale);

            drawBackground(ctx, scaledX, scaledY, maxWidth, totalHeight);
            drawTextLines(ctx, renderer, lines, scaledX, scaledY, lineHeight, maxWidth);

            matrices.pop();
        } else {
            drawBackground(ctx, x, y, maxWidth, totalHeight);
            drawTextLines(ctx, renderer, lines, x, y, lineHeight, maxWidth);
        }
    }

    private static void drawBackground(DrawContext ctx, int x, int y, int width, int height) {
        if (!config.appearance.showBackground)
            return;

        int padding = config.appearance.padding;
        int opacity = config.appearance.backgroundOpacity << 24;
        int bgColor = opacity | (COLOR_BACKGROUND & 0xFFFFFF);

        if (config.appearance.overlayStyle == ModConfig.OverlayStyle.MODERN) {
            int borderColor = opacity | 0x404040;

            ctx.fill(x - padding, y - padding,
                    x + width + padding, y + height + padding,
                    bgColor);

            ctx.fill(x - padding, y - padding,
                    x - padding + 3, y + height + padding,
                    0xFFFFA500);

            ctx.fill(x - padding + 3, y - padding,
                    x + width + padding, y - padding + 1,
                    borderColor);

            ctx.fill(x + width + padding - 1, y - padding,
                    x + width + padding, y + height + padding,
                    borderColor);

            ctx.fill(x - padding + 3, y + height + padding - 1,
                    x + width + padding, y + height + padding,
                    borderColor);

        } else if (config.appearance.overlayStyle == ModConfig.OverlayStyle.CLASSIC) {
            ctx.fill(x - padding, y - padding,
                    x + width + padding, y + height + padding,
                    bgColor);
        }
    }

    private static void drawTextLines(DrawContext ctx, TextRenderer renderer, List<OverlayLine> lines, int x, int y,
            int lineHeight, int fullWidth) {
        int startY = y;
        int startX = x;

        if (config.appearance.overlayStyle == ModConfig.OverlayStyle.MODERN) {
            startY += 2;
            startX += 4;
        }

        boolean shadow = config.appearance.useTextShadow;

        for (int i = 0; i < lines.size(); i++) {
            OverlayLine line = lines.get(i);
            int lineY = startY + (i * lineHeight);
            int currentX = startX;

            for (int j = 0; j < line.parts.size(); j++) {
                TextPart part = line.parts.get(j);
                ctx.drawText(renderer, part.text, currentX, lineY, part.color, shadow);
                currentX += renderer.getWidth(part.text);

                if (j < line.spacers.size()) {
                    Text spacer = line.spacers.get(j);
                    if (spacer != null) {
                        ctx.drawText(renderer, spacer, currentX, lineY, getTextColor(), shadow);
                        currentX += renderer.getWidth(spacer);
                    }
                }
            }
        }
    }

    private static int calculateMaxWidth(TextRenderer renderer, List<OverlayLine> lines) {
        int maxWidth = 0;
        for (OverlayLine line : lines) {
            maxWidth = Math.max(maxWidth, calculateLineWidth(renderer, line));
        }
        return maxWidth;
    }

    private static int calculateLineWidth(TextRenderer renderer, OverlayLine line) {
        int width = 0;

        for (int i = 0; i < line.parts.size(); i++) {
            width += renderer.getWidth(line.parts.get(i).text);
            if (i < line.spacers.size()) {
                Text spacer = line.spacers.get(i);
                if (spacer != null) {
                    width += renderer.getWidth(spacer);
                }
            }
        }

        return width;
    }

    private static int[] calculatePosition(int screenWidth, int screenHeight, int maxWidth, int totalHeight) {
        int padding = config.appearance.padding;
        int x = 0;
        int y = 0;

        if (config.appearance.position == null) {
            config.appearance.position = ModConfig.OverlayPosition.TOP_LEFT;
        }

        switch (config.appearance.position) {
            case TOP_LEFT -> {
                x = padding;
                y = padding;
            }
            case TOP_RIGHT -> {
                x = screenWidth - maxWidth - padding - 5;
                y = padding;
            }
            case BOTTOM_LEFT -> {
                x = padding;
                y = screenHeight - totalHeight - padding - 5;
            }
            case BOTTOM_RIGHT -> {
                x = screenWidth - maxWidth - padding - 5;
                y = screenHeight - totalHeight - padding - 5;
            }
        }

        x = Math.max(0, Math.min(x, screenWidth - maxWidth));
        y = Math.max(0, Math.min(y, screenHeight - totalHeight));

        return new int[] { x, y };
    }

    private static int getTextColor() {
        try {
            return Integer.parseInt(config.appearance.textColorHex.substring(1), 16) | 0xFF000000;
        } catch (Exception e) {
            return COLOR_NEUTRAL;
        }
    }

    private static int getColorForMemory(double usagePercent) {
        if (!config.appearance.useAdaptiveColors)
            return getTextColor();
        if (usagePercent <= 75)
            return COLOR_EXCELLENT;
        if (usagePercent <= 90)
            return COLOR_GOOD;
        if (usagePercent <= 95)
            return COLOR_WARNING;
        return COLOR_CRITICAL;
    }

    private static int getColorForPing(int ping) {
        if (!config.appearance.useAdaptiveColors)
            return getTextColor();
        if (ping <= 50)
            return COLOR_EXCELLENT;
        if (ping <= 100)
            return COLOR_GOOD;
        if (ping <= 200)
            return COLOR_WARNING;
        return COLOR_CRITICAL;
    }

    private static int getColorForFps(int fps) {
        if (!config.appearance.useAdaptiveColors)
            return getTextColor();
        if (fps >= 144)
            return COLOR_EXCELLENT;
        if (fps >= 60)
            return COLOR_GOOD;
        if (fps >= 30)
            return COLOR_WARNING;
        return COLOR_CRITICAL;
    }

    private static int getColorForAverageFps(int fps) {
        if (!config.appearance.useAdaptiveColors)
            return getTextColor();
        if (fps >= 120)
            return COLOR_AVG_EXCELLENT;
        if (fps >= 60)
            return COLOR_AVG_GOOD;
        if (fps >= 30)
            return COLOR_AVG_WARNING;
        return COLOR_AVG_CRITICAL;
    }

    private static class OverlayLine {
        final List<TextPart> parts = new ArrayList<>(5);
        final List<Text> spacers = new ArrayList<>(5);

        void reset() {
            parts.clear();
            spacers.clear();
        }

        void addPart(Text text, int color) {
            parts.add(new TextPart(text, color));
        }

        void addSpacer(Text text) {
            spacers.add(text);
        }
    }

    private static class TextPart {
        final Text text;
        final int color;

        TextPart(Text text, int color) {
            this.text = text;
            this.color = color;
        }
    }
}