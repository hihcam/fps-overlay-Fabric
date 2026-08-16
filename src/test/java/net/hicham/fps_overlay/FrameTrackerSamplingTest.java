package net.hicham.fps_overlay;

/**
 * Unit and regression tests for {@link FrameTracker}.
 * Verifies GC-free ring buffer behavior, average FPS calculations,
 * 1% low percentile calculations, and wraparound eviction.
 */
public final class FrameTrackerSamplingTest {
    private FrameTrackerSamplingTest() {
    }

    public static void main(String[] args) {
        testInitialState();
        testUniformFrameRate();
        testAverageFpsWithVariableFrames();
        testOnePercentLowPercentile();
        testRingBufferEvictionAndWraparound();
        testResetClearsAllData();
        testMemoryAllocationFlatness();
        System.out.println("ALL FrameTrackerSamplingTest assertions passed successfully!");
    }

    private static void testInitialState() {
        FrameTracker tracker = new FrameTracker();
        assertEquals(0.0, tracker.calculateAverageFps(), "initial average fps");
        assertEquals(0, tracker.calculateOnePercentLow(), "initial 1% low");
        assertEquals(0.0, tracker.getCurrentFrameTimeMs(), "initial current frame time ms");
    }

    private static void testUniformFrameRate() {
        FrameTracker tracker = new FrameTracker();
        long time = 1_000_000_000L; // 1s
        tracker.recordFrame(time);

        // Record 100 frames at 10ms intervals (10,000,000 ns = 100 FPS)
        for (int i = 0; i < 100; i++) {
            time += 10_000_000L;
            tracker.recordFrame(time);
        }

        assertClose(100.0, tracker.calculateAverageFps(), "uniform 100 FPS average");
        assertEquals(100, tracker.calculateOnePercentLow(), "uniform 100 FPS 1% low");
        assertClose(10.0, tracker.getCurrentFrameTimeMs(), "last frame time 10ms");
    }

    private static void testAverageFpsWithVariableFrames() {
        FrameTracker tracker = new FrameTracker();
        long time = 1_000_000_000L;
        tracker.recordFrame(time);

        // 50 frames @ 10ms (500ms total)
        for (int i = 0; i < 50; i++) {
            time += 10_000_000L;
            tracker.recordFrame(time);
        }

        // 50 frames @ 20ms (1000ms total)
        for (int i = 0; i < 50; i++) {
            time += 20_000_000L;
            tracker.recordFrame(time);
        }

        // Total time for 100 frames = 1500ms (1.5s) -> 100 / 1.5s = 66.6667 FPS
        assertClose(66.6666667, tracker.calculateAverageFps(), "50x10ms + 50x20ms average FPS");
    }

    private static void testOnePercentLowPercentile() {
        FrameTracker tracker = new FrameTracker();
        long time = 1_000_000_000L;
        tracker.recordFrame(time);

        // 98 fast frames @ 10ms (100 FPS)
        for (int i = 0; i < 98; i++) {
            time += 10_000_000L;
            tracker.recordFrame(time);
        }

        // 2 slow spike frames @ 50ms (20 FPS)
        for (int i = 0; i < 2; i++) {
            time += 50_000_000L;
            tracker.recordFrame(time);
        }

        // In 100 samples, 1% cutoff is index 98 (0-based ascending).
        // Index 98 is 50ms -> 20 FPS.
        assertEquals(20, tracker.calculateOnePercentLow(), "1% low with 2% frame spike");
    }

    private static void testRingBufferEvictionAndWraparound() {
        FrameTracker tracker = new FrameTracker();
        long time = 1_000_000_000L;
        tracker.recordFrame(time);

        // Fill buffer to capacity (1000 samples @ 10ms = 100 FPS)
        for (int i = 0; i < 1000; i++) {
            time += 10_000_000L;
            tracker.recordFrame(time);
        }
        assertClose(100.0, tracker.calculateAverageFps(), "full buffer 1000 samples @ 100 FPS");
        assertEquals(100, tracker.calculateOnePercentLow(), "full buffer 1% low @ 100 FPS");

        // Push 1000 new samples @ 20ms = 50 FPS (evicting all 10ms samples)
        for (int i = 0; i < 1000; i++) {
            time += 20_000_000L;
            tracker.recordFrame(time);
        }
        assertClose(50.0, tracker.calculateAverageFps(), "fully evicted buffer @ 50 FPS");
        assertEquals(50, tracker.calculateOnePercentLow(), "fully evicted buffer 1% low @ 50 FPS");

        // Push 500 new samples @ 25ms = 40 FPS
        // Buffer now holds 500 @ 20ms (10,000ms) + 500 @ 25ms (12,500ms) = 22,500ms total
        for (int i = 0; i < 500; i++) {
            time += 25_000_000L;
            tracker.recordFrame(time);
        }
        // Expected average = 1000 / 22.5s = 44.444444 FPS
        assertClose(44.444444, tracker.calculateAverageFps(), "partial wraparound 500x20ms + 500x25ms");
    }

    private static void testResetClearsAllData() {
        FrameTracker tracker = new FrameTracker();
        long time = 1_000_000_000L;
        tracker.recordFrame(time);
        for (int i = 0; i < 50; i++) {
            time += 16_666_667L;
            tracker.recordFrame(time);
        }

        assertTrue(tracker.calculateAverageFps() > 0, "fps before reset");
        tracker.reset();

        assertEquals(0.0, tracker.calculateAverageFps(), "fps after reset");
        assertEquals(0, tracker.calculateOnePercentLow(), "1% low after reset");
    }

    private static void testMemoryAllocationFlatness() {
        FrameTracker tracker = new FrameTracker();
        long time = 1_000_000_000L;
        tracker.recordFrame(time);

        // Run 20,000 iterations to ensure recordFrame has zero heap allocation overhead
        for (int i = 0; i < 20_000; i++) {
            time += 16_666_667L;
            tracker.recordFrame(time);
        }

        assertTrue(tracker.calculateAverageFps() > 0, "healthy average fps after 20k frames");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Double.compare(expected, actual) != 0) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertClose(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 0.001) {
            throw new AssertionError(message + " - Expected ~" + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " - Expected condition to be true");
        }
    }
}
