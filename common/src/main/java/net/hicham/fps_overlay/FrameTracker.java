package net.hicham.fps_overlay;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks per-frame timing data: frame deltas, average FPS, and 1% low FPS.
 * Thread-safe — recordFrame() is called from the render thread while
 * getters may be read from the update tick.
 */
public class FrameTracker {
    private static final int MAX_FRAME_SAMPLES = 1000;

    private volatile double currentFrameTimeMs = 0;
    private volatile long lastFrameTimeNano = 0;

    private final ConcurrentLinkedDeque<Long> frameTimeBuffer = new ConcurrentLinkedDeque<>();
    private final AtomicLong sumOfDeltasNanos = new AtomicLong(0);
    private final AtomicInteger frameBufferSize = new AtomicInteger(0);

    public void recordFrame() {
        long currentNano = System.nanoTime();
        if (lastFrameTimeNano != 0) {
            long delta = currentNano - lastFrameTimeNano;
            currentFrameTimeMs = delta / 1_000_000.0;

            sumOfDeltasNanos.addAndGet(delta);
            frameTimeBuffer.addLast(delta);

            if (frameBufferSize.incrementAndGet() > MAX_FRAME_SAMPLES) {
                Long removed = frameTimeBuffer.pollFirst();
                if (removed != null) {
                    sumOfDeltasNanos.addAndGet(-removed);
                    frameBufferSize.decrementAndGet();
                }
            }
        }
        lastFrameTimeNano = currentNano;
    }

    public double getCurrentFrameTimeMs() {
        return currentFrameTimeMs;
    }

    public double calculateAverageFps() {
        int size = frameBufferSize.get();
        long sum = sumOfDeltasNanos.get();
        if (size == 0 || sum <= 0) {
            return 0;
        }
        return (size * 1_000_000_000.0) / sum;
    }

    public int calculateOnePercentLow() {
        int size = frameBufferSize.get();
        if (size < 10) {
            return 0;
        }

        Long[] samples = frameTimeBuffer.toArray(new Long[0]);
        Arrays.sort(samples);

        int index = Math.max(0, samples.length - 1 - (samples.length / 100));
        long onePercentFrameNanos = samples[index];

        if (onePercentFrameNanos <= 0) {
            return 0;
        }
        return (int) (1_000_000_000.0 / onePercentFrameNanos);
    }

    public void reset() {
        frameBufferSize.set(0);
        sumOfDeltasNanos.set(0);
        frameTimeBuffer.clear();
        lastFrameTimeNano = 0;
    }
}
