package net.hicham.fps_overlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public enum OverlayMetric {
    FPS("fps", "text.fps_overlay.fps_short", "text.fps_overlay.fps"),
    AVG_FPS("avg_fps", "text.fps_overlay.avg_fps_short", "text.fps_overlay.fps"),
    FRAME_TIME("frame_time", "text.fps_overlay.frame_time_short", "text.fps_overlay.ms"),
    LOW_1("one_percent_low", "text.fps_overlay.1percent_low_short", "text.fps_overlay.fps"),
    MEMORY("memory", "text.fps_overlay.memory_short", "text.fps_overlay.gb"),
    CPU("cpu", "text.fps_overlay.cpu_short", "text.fps_overlay.percent"),
    GPU("gpu", "text.fps_overlay.gpu_short", "text.fps_overlay.percent"),
    PING("ping", "text.fps_overlay.ping_short", "text.fps_overlay.ms"),
    MSPT("mspt", "text.fps_overlay.mspt_short", "text.fps_overlay.ms"),
    TPS("tps", "text.fps_overlay.tps_short", "text.fps_overlay.tps_unit"),
    CHUNKS("chunks", "text.fps_overlay.chunks_short", ""),
    COORDS("coords", "text.fps_overlay.coords_short", ""),
    BIOME("biome", "text.fps_overlay.biome_short", "");

    private final String id;
    private final String labelKey;
    private final String unitKey;

    OverlayMetric(String id, String labelKey, String unitKey) {
        this.id = id;
        this.labelKey = labelKey;
        this.unitKey = unitKey;
    }

    public String getId() {
        return id;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public String getUnitKey() {
        return unitKey;
    }

    public static OverlayMetric fromId(String id) {
        for (OverlayMetric metric : values()) {
            if (metric.id.equalsIgnoreCase(id)) {
                return metric;
            }
        }
        return null;
    }

    public static List<String> defaultOrderIds() {
        List<String> order = new ArrayList<>(values().length);
        for (OverlayMetric metric : values()) {
            order.add(metric.id);
        }
        return order;
    }

    public static List<OverlayMetric> sanitizeOrder(List<String> configuredOrder) {
        Set<OverlayMetric> ordered = new LinkedHashSet<>();
        if (configuredOrder != null) {
            for (String id : configuredOrder) {
                OverlayMetric metric = fromId(id);
                if (metric != null) {
                    ordered.add(metric);
                }
            }
        }

        ordered.addAll(Arrays.asList(values()));
        return new ArrayList<>(ordered);
    }
}
