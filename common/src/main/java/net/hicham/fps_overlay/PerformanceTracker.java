package net.hicham.fps_overlay;

import net.minecraft.client.Minecraft;

import java.util.Arrays;

/**
 * Facade that coordinates all metric collection.
 * Frame timing is delegated to {@link FrameTracker},
 * data fetching to {@link MetricProvider}.
 * This class owns FPS graph sampling, min/max session stats,
 * and the update-interval gating.
 */
public class PerformanceTracker {
    private static final PerformanceTracker INSTANCE = new PerformanceTracker();
    private static final int GRAPH_SAMPLE_CAPACITY = 600;
    private static final long GRAPH_SAMPLE_INTERVAL_MS = 100L;

    private ModConfig config;
    private final FrameTracker frameTracker = new FrameTracker();

    // ── Cached metric values ────────────────────────────────────
    private int currentFps = 0;
    private double averageFps = 0;
    private int onePercentLow = 0;
    private int currentPing = 0;

    private long usedMemory = 0;
    private long maxMemory = 0;

    private double currentMspt = 0;
    private double currentTps = 20.0;

    private int loadedChunks = 0;
    private int visibleChunks = 0;
    private int completedChunks = 0;

    private String coordinatesText = "0 64 0";
    private String biomeText = "Unknown";

    // ── Session stats ───────────────────────────────────────────
    private int minFps = Integer.MAX_VALUE;
    private int maxFps = 0;
    private int minPing = Integer.MAX_VALUE;
    private int maxPing = 0;

    // ── Graph ───────────────────────────────────────────────────
    private final int[] fpsGraphBuffer = new int[GRAPH_SAMPLE_CAPACITY];
    private final int[] graphCopyBuffer = new int[GRAPH_SAMPLE_CAPACITY];
    private final Object graphLock = new Object();
    private int graphIndex = 0;
    private int graphSize = 0;

    private volatile long lastUpdateTime = 0;
    private volatile long lastGraphSampleTime = 0;

    private PerformanceTracker() {
    }

    public static PerformanceTracker getInstance() {
        return INSTANCE;
    }

    public void setConfig(ModConfig config) {
        this.config = config;
    }

    // ── Tick update ─────────────────────────────────────────────

    public void update(Minecraft client) {
        if (config == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < config.general.updateIntervalMs) {
            return;
        }
        lastUpdateTime = currentTime;

        // FPS & frame analysis
        currentFps = Math.max(0, client.getFps());
        averageFps = frameTracker.calculateAverageFps();
        onePercentLow = frameTracker.calculateOnePercentLow();

        // Network
        currentPing = MetricProvider.fetchPing(client);

        updateMinMaxStats();

        // Memory
        MetricProvider.MemoryData memory = MetricProvider.fetchMemory();
        usedMemory = memory.used();
        maxMemory = memory.max();

        // Tick
        MetricProvider.TickData tick = MetricProvider.fetchTickData(client);
        currentMspt = tick.mspt();
        currentTps = tick.tps();

        // Chunks
        MetricProvider.ChunkData chunks = MetricProvider.fetchChunks(client);
        loadedChunks = chunks.loaded();
        visibleChunks = chunks.visible();
        completedChunks = chunks.completed();

        // Location
        MetricProvider.LocationData location = MetricProvider.fetchLocation(client);
        coordinatesText = location.coordinates();
        biomeText = location.biome();

        sampleGraph(currentTime);
    }

    /**
     * Called once per rendered frame from the render thread.
     */
    public void recordFrame() {
        frameTracker.recordFrame();
    }

    // ── Reset ───────────────────────────────────────────────────

    public void clearAverageFpsData() {
        resetSessionStats();
    }

    public void resetSessionStats() {
        frameTracker.reset();
        averageFps = 0;

        synchronized (graphLock) {
            graphIndex = 0;
            graphSize = 0;
            Arrays.fill(fpsGraphBuffer, 0);
        }

        minFps = Integer.MAX_VALUE;
        maxFps = 0;
        minPing = Integer.MAX_VALUE;
        maxPing = 0;
    }

    // ── Graph ───────────────────────────────────────────────────

    public int[] copyGraphValues() {
        synchronized (graphLock) {
            int size = graphSize;
            for (int i = 0; i < size; i++) {
                int sourceIndex = (graphIndex - size + i + fpsGraphBuffer.length) % fpsGraphBuffer.length;
                graphCopyBuffer[i] = fpsGraphBuffer[sourceIndex];
            }
            if (size == graphCopyBuffer.length) {
                return graphCopyBuffer;
            }
            int[] values = new int[size];
            System.arraycopy(graphCopyBuffer, 0, values, 0, size);
            return values;
        }
    }

    // ── Getters ─────────────────────────────────────────────────

    public int getCurrentFps() {
        return currentFps;
    }

    public double getCurrentFrameTimeMs() {
        return frameTracker.getCurrentFrameTimeMs();
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public int getCurrentPing() {
        return currentPing;
    }

    public double getAverageFps() {
        return averageFps;
    }

    public int getOnePercentLow() {
        return onePercentLow;
    }

    public double getMspt() {
        return currentMspt;
    }

    public double getTps() {
        return currentTps;
    }

    public int getLoadedChunks() {
        return loadedChunks;
    }

    public int getVisibleChunks() {
        return visibleChunks;
    }

    public int getCompletedChunks() {
        return completedChunks;
    }

    public String getCoordinatesText() {
        return coordinatesText;
    }

    public String getBiomeText() {
        return biomeText;
    }

    public int getMinFps() {
        return minFps == Integer.MAX_VALUE ? currentFps : minFps;
    }

    public int getMaxFps() {
        return maxFps;
    }

    public int getMinPing() {
        return minPing == Integer.MAX_VALUE ? currentPing : minPing;
    }

    public int getMaxPing() {
        return maxPing;
    }

    // ── Private helpers ─────────────────────────────────────────

    private void updateMinMaxStats() {
        minFps = Math.min(minFps, currentFps);
        maxFps = Math.max(maxFps, currentFps);

        if (currentPing > 0) {
            minPing = Math.min(minPing, currentPing);
            maxPing = Math.max(maxPing, currentPing);
        }
    }

    private void sampleGraph(long currentTime) {
        if (currentTime - lastGraphSampleTime < GRAPH_SAMPLE_INTERVAL_MS) {
            return;
        }
        lastGraphSampleTime = currentTime;

        synchronized (graphLock) {
            fpsGraphBuffer[graphIndex] = currentFps;
            graphIndex = (graphIndex + 1) % fpsGraphBuffer.length;
            if (graphSize < fpsGraphBuffer.length) {
                graphSize++;
            }
        }
    }
}
