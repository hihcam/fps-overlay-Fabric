package net.hicham.fps_overlay;

import com.sun.management.OperatingSystemMXBean;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Arrays;

public class PerformanceTracker {
    private static final PerformanceTracker INSTANCE = new PerformanceTracker();
    private static final int GRAPH_SAMPLE_CAPACITY = 600;
    private static final long GRAPH_SAMPLE_INTERVAL_MS = 100L;

    private final OperatingSystemMXBean operatingSystemBean =
            ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean bean ? bean : null;

    private ModConfig config;

    private int currentFps = 0;
    private double currentFrameTimeMs = 0;
    private long usedMemory = 0;
    private long maxMemory = 0;
    private double currentCpuUsage = -1;
    private double currentGpuUsage = -1;
    private boolean hasSeenRealGpuSample = false;
    private int consecutiveZeroGpuSamples = 0;
    private int currentPing = 0;
    private int onePercentLow = 0;
    private double currentMspt = 0;
    private double currentTps = 20.0;
    private int loadedChunks = 0;
    private int visibleChunks = 0;
    private int completedChunks = 0;
    private String coordinatesText = "0 64 0";
    private String biomeText = "Unknown";

    private int minFps = Integer.MAX_VALUE;
    private int maxFps = 0;
    private int minPing = Integer.MAX_VALUE;
    private int maxPing = 0;

    private double averageFps = 0;
    private final long[] frameTimeBuffer = new long[1000];
    private long sumOfDeltasNanos = 0;
    private int frameBufferIndex = 0;
    private int frameBufferSize = 0;

    private final int[] fpsGraphBuffer = new int[GRAPH_SAMPLE_CAPACITY];
    private int graphIndex = 0;
    private int graphSize = 0;

    private long lastUpdateTime = 0;
    private long lastFrameTimeNano = 0;
    private long lastGraphSampleTime = 0;

    private PerformanceTracker() {
    }

    public static PerformanceTracker getInstance() {
        return INSTANCE;
    }

    public void setConfig(ModConfig config) {
        this.config = config;
    }

    public void update(Minecraft client) {
        if (config == null) {
            return;
        }

        LocationData location = fetchLocationData(client);
        coordinatesText = location.coordinates();
        biomeText = location.biome();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < config.general.updateIntervalMs) {
            return;
        }
        lastUpdateTime = currentTime;

        currentFps = Math.max(0, client.getFps());
        averageFps = calculateAverageFps();
        currentPing = fetchCurrentPing(client);
        onePercentLow = calculateOnePercentLow();

        updateMinMaxStats();

        MemoryData memory = fetchMemoryData();
        usedMemory = memory.used();
        maxMemory = memory.max();

        currentCpuUsage = fetchCpuUsage();
        currentGpuUsage = fetchGpuUsage(client);

        TickData tick = fetchTickData(client);
        currentMspt = tick.mspt();
        currentTps = tick.tps();

        ChunkData chunks = fetchChunkData(client);
        loadedChunks = chunks.loaded();
        visibleChunks = chunks.visible();
        completedChunks = chunks.completed();

        sampleGraph(currentTime);
    }

    public void recordFrame() {
        long currentNano = System.nanoTime();
        if (lastFrameTimeNano != 0) {
            long delta = currentNano - lastFrameTimeNano;
            currentFrameTimeMs = delta / 1_000_000.0;

            sumOfDeltasNanos -= frameTimeBuffer[frameBufferIndex];
            frameTimeBuffer[frameBufferIndex] = delta;
            sumOfDeltasNanos += delta;

            frameBufferIndex = (frameBufferIndex + 1) % frameTimeBuffer.length;
            if (frameBufferSize < frameTimeBuffer.length) {
                frameBufferSize++;
            }
        }
        lastFrameTimeNano = currentNano;
    }

    public void clearAverageFpsData() {
        resetSessionStats();
    }

    public void resetSessionStats() {
        frameBufferIndex = 0;
        frameBufferSize = 0;
        averageFps = 0;
        sumOfDeltasNanos = 0;
        Arrays.fill(frameTimeBuffer, 0);

        graphIndex = 0;
        graphSize = 0;
        Arrays.fill(fpsGraphBuffer, 0);

        minFps = Integer.MAX_VALUE;
        maxFps = 0;
        minPing = Integer.MAX_VALUE;
        maxPing = 0;
        consecutiveZeroGpuSamples = 0;
    }

    public int[] copyGraphValues() {
        int[] values = new int[graphSize];
        for (int i = 0; i < graphSize; i++) {
            int sourceIndex = (graphIndex - graphSize + i + fpsGraphBuffer.length) % fpsGraphBuffer.length;
            values[i] = fpsGraphBuffer[sourceIndex];
        }
        return values;
    }

    public int getCurrentFps() {
        return currentFps;
    }

    public double getCurrentFrameTimeMs() {
        return currentFrameTimeMs;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public double getCurrentCpuUsage() {
        return currentCpuUsage;
    }

    public double getCurrentGpuUsage() {
        return currentGpuUsage;
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

        fpsGraphBuffer[graphIndex] = currentFps;
        graphIndex = (graphIndex + 1) % fpsGraphBuffer.length;
        if (graphSize < fpsGraphBuffer.length) {
            graphSize++;
        }
    }

    private double calculateAverageFps() {
        if (frameBufferSize == 0 || sumOfDeltasNanos == 0) {
            return 0;
        }
        return (frameBufferSize * 1_000_000_000.0) / sumOfDeltasNanos;
    }

    private MemoryData fetchMemoryData() {
        try {
            Runtime runtime = Runtime.getRuntime();
            return new MemoryData(runtime.totalMemory() - runtime.freeMemory(), runtime.maxMemory());
        } catch (Exception e) {
            return new MemoryData(0, 0);
        }
    }

    private double fetchCpuUsage() {
        if (operatingSystemBean == null) {
            return -1;
        }

        double load = operatingSystemBean.getCpuLoad();
        return load >= 0 ? Math.min(100.0, load * 100.0) : -1;
    }

    private double fetchGpuUsage(Minecraft client) {
        double platformGpuUsage = PlatformGpuUsageProvider.getInstance().getCurrentUtilization();
        if (platformGpuUsage >= 0) {
            hasSeenRealGpuSample = true;
            consecutiveZeroGpuSamples = 0;
            return platformGpuUsage;
        }

        try {
            double utilization = invokeDouble(client, "getGpuUtilization", "getGpuUtilizationPercentage");
            if (Double.isNaN(utilization) || Double.isInfinite(utilization) || utilization < 0) {
                consecutiveZeroGpuSamples = 0;
                return -1;
            }

            double clamped = Math.min(100.0, utilization);
            if (clamped > 0) {
                hasSeenRealGpuSample = true;
                consecutiveZeroGpuSamples = 0;
                return clamped;
            }

            consecutiveZeroGpuSamples++;
            return hasSeenRealGpuSample || consecutiveZeroGpuSamples < 8 ? 0 : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private int fetchCurrentPing(Minecraft client) {
        var handler = client.getConnection();
        var player = client.player;
        if (handler == null || player == null) {
            return 0;
        }

        try {
            var entry = handler.getPlayerInfo(player.getUUID());
            return entry != null ? Math.max(0, entry.getLatency()) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int calculateOnePercentLow() {
        if (frameBufferSize < 10) {
            return 0;
        }

        long[] sortedTimes = new long[frameBufferSize];
        System.arraycopy(frameTimeBuffer, 0, sortedTimes, 0, frameBufferSize);
        Arrays.sort(sortedTimes);

        int index = Math.max(0, frameBufferSize - 1 - (frameBufferSize / 100));
        long onePercentFrameNanos = sortedTimes[index];

        if (onePercentFrameNanos == 0) {
            return 0;
        }
        return (int) (1_000_000_000.0 / onePercentFrameNanos);
    }

    private TickData fetchTickData(Minecraft client) {
        var server = client.getSingleplayerServer();
        if (server != null) {
            double mspt = server.getAverageTickTimeNanos() / 1_000_000.0;
            double tps = Math.min(20.0, 1000.0 / Math.max(1.0, mspt));
            return new TickData(mspt, tps);
        }
        return new TickData(0, 20.0);
    }

    private ChunkData fetchChunkData(Minecraft client) {
        if (client.level == null || client.levelRenderer == null) {
            return new ChunkData(0, 0, 0);
        }

        int loaded = 0;
        int visible = 0;
        int completed = 0;

        try {
            loaded = Math.max(0, client.level.getChunkSource().getLoadedChunksCount());
        } catch (Exception ignored) {
        }

        try {
            visible = Math.max(0, client.levelRenderer.countRenderedSections());
        } catch (Exception ignored) {
        }

        try {
            completed = Math.max(0, client.levelRenderer.getVisibleSections().size());
        } catch (Exception ignored) {
            completed = visible;
        }

        return new ChunkData(loaded, visible, completed);
    }

    private LocationData fetchLocationData(Minecraft client) {
        if (client.player == null || client.level == null) {
            return new LocationData("0 64 0", "Unknown");
        }

        BlockPos pos = client.player.blockPosition();
        String coordinates = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        String biome = "Unknown";

        try {
            biome = client.level.getBiome(pos)
                    .unwrapKey()
                    .map(key -> Component.translatable(key.identifier().toLanguageKey("biome")).getString())
                    .orElse("Unknown");
        } catch (Exception ignored) {
        }

        return new LocationData(coordinates, biome);
    }

    private int invokeInt(Object target, String... methodNames) throws ReflectiveOperationException {
        Object value = invokeObject(target, methodNames);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new NoSuchMethodException(String.join(", ", methodNames));
    }

    private double invokeDouble(Object target, String... methodNames) throws ReflectiveOperationException {
        Object value = invokeObject(target, methodNames);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new NoSuchMethodException(String.join(", ", methodNames));
    }

    private Object invokeObject(Object target, String... methodNames) throws ReflectiveOperationException {
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException(String.join(", ", methodNames));
    }

    private Object invokeObjectWithStringArg(Object target, String methodName, String arg)
            throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName, String.class);
        return method.invoke(target, arg);
    }

    private record MemoryData(long used, long max) {
    }

    private record TickData(double mspt, double tps) {
    }

    private record ChunkData(int loaded, int visible, int completed) {
    }

    private record LocationData(String coordinates, String biome) {
    }
}
