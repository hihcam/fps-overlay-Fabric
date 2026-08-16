package net.hicham.fps_overlay;

import java.util.Arrays;

/**
 * Tracks per-frame timing data: frame deltas, average FPS, and 1% low FPS.
 * Confined to the Minecraft Client main thread.
 */
public class FrameTracker {
    private static final int MAX_FRAME_SAMPLES = 1000;

    private double currentFrameTimeMs = 0;
    private long lastFrameTimeNano = 0;

    private final long[] frameTimeBuffer = new long[MAX_FRAME_SAMPLES];
    private long sumOfDeltasNanos = 0;
    private int head = 0;
    private int size = 0;

    public void recordFrame() {
        recordFrame(System.nanoTime());
    }

    public void recordFrame(long currentNano) {
        if (lastFrameTimeNano != 0) {
            long delta = currentNano - lastFrameTimeNano;
            currentFrameTimeMs = delta / 1_000_000.0;

            if (size >= MAX_FRAME_SAMPLES) {
                // Subtract oldest BEFORE overwriting its slot
                long oldest = frameTimeBuffer[head];
                sumOfDeltasNanos -= oldest;
                sumOfDeltasNanos += delta;
                frameTimeBuffer[head] = delta;
                head = (head + 1) % MAX_FRAME_SAMPLES;
            } else {
                frameTimeBuffer[size] = delta;
                sumOfDeltasNanos += delta;
                size++;
            }
        }
        lastFrameTimeNano = currentNano;
    }

    public double getCurrentFrameTimeMs() {
        return currentFrameTimeMs;
    }

    public double calculateAverageFps() {
        if (size == 0 || sumOfDeltasNanos <= 0) {
            return 0;
        }
        return (size * 1_000_000_000.0) / sumOfDeltasNanos;
    }

    public int calculateOnePercentLow() {
        if (size < 10) {
            return 0;
        }

        long[] samples = new long[size];
        System.arraycopy(frameTimeBuffer, 0, samples, 0, size);
        Arrays.sort(samples);

        int index = Math.max(0, samples.length - 1 - (samples.length / 100));
        long onePercentFrameNanos = samples[index];

        if (onePercentFrameNanos <= 0) {
            return 0;
        }
        return (int) (1_000_000_000.0 / onePercentFrameNanos);
    }

    public void reset() {
        size = 0;
        head = 0;
        sumOfDeltasNanos = 0;
        lastFrameTimeNano = 0;
        Arrays.fill(frameTimeBuffer, 0L);
    }
}
