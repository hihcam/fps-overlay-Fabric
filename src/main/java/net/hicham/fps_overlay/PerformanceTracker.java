package net.hicham.fps_overlay;

import net.minecraft.client.MinecraftClient;

public class PerformanceTracker {
    private static final PerformanceTracker INSTANCE = new PerformanceTracker();

    private ModConfig config;

    // Performance data - simple primitives
    private int currentFps = 0;
    private long usedMemory = 0;
    private long maxMemory = 0;
    private int currentPing = 0;

    // Average FPS tracking - Circular Buffer
    private double averageFps = 0;
    private final int[] fpsBuffer = new int[200]; // Stores up to 200 samples
    private int bufferIndex = 0;
    private int bufferSize = 0;

    // Timing
    private long lastUpdateTime = 0;
    private long lastAverageUpdateTime = 0;

    private PerformanceTracker() {}

    public static PerformanceTracker getInstance() {
        return INSTANCE;
    }

    public void setConfig(ModConfig config) {
        this.config = config;
    }

    public void update(MinecraftClient client) {
        if (config == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < config.general.updateIntervalMs) return;
        lastUpdateTime = currentTime;

        // 1. Update Current FPS
        this.currentFps = Math.max(0, client.getCurrentFps());

        // 2. Update Average FPS (Circular Buffer)
        this.averageFps = calculateAverageFps(this.currentFps, currentTime);

        // 3. Update Memory
        updateMemoryData();

        // 4. Update Ping
        this.currentPing = fetchCurrentPing(client);
    }

    private double calculateAverageFps(int newFps, long currentTime) {
        // Add to circular buffer
        fpsBuffer[bufferIndex] = newFps;
        bufferIndex = (bufferIndex + 1) % fpsBuffer.length;
        if (bufferSize < fpsBuffer.length) bufferSize++;

        // Calculate average every interval (e.g. 1 second or based on config)
        if (currentTime - lastAverageUpdateTime >= 1000 || lastAverageUpdateTime == 0) {
            lastAverageUpdateTime = currentTime;
            
            if (bufferSize == 0) return 0;
            
            long sum = 0;
            for (int i = 0; i < bufferSize; i++) {
                sum += fpsBuffer[i];
            }
            return sum / (double) bufferSize;
        }
        
        return this.averageFps; // Return current average if not time to recalculate
    }

    private void updateMemoryData() {
        try {
            Runtime runtime = Runtime.getRuntime();
            this.usedMemory = runtime.totalMemory() - runtime.freeMemory();
            this.maxMemory = runtime.maxMemory();
        } catch (Exception e) {
            // Ignore
        }
    }

    private int fetchCurrentPing(MinecraftClient client) {
        var handler = client.getNetworkHandler();
        var player = client.player;
        if (handler == null || player == null) return 0;
        try {
            var entry = handler.getPlayerListEntry(player.getUuid());
            return entry != null ? Math.max(0, entry.getLatency()) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public void clearAverageFpsData() {
        bufferIndex = 0;
        bufferSize = 0;
        averageFps = 0;
        lastAverageUpdateTime = 0;
    }

    // Simple Getters
    public int getCurrentFps() { return currentFps; }
    public long getUsedMemory() { return usedMemory; }
    public long getMaxMemory() { return maxMemory; }
    public int getCurrentPing() { return currentPing; }
    public double getAverageFps() { return averageFps; }
}
