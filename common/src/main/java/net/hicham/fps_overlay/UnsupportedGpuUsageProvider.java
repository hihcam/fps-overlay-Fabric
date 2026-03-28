package net.hicham.fps_overlay;

public final class UnsupportedGpuUsageProvider implements GpuUsageProvider {
    private static final UnsupportedGpuUsageProvider INSTANCE = new UnsupportedGpuUsageProvider();

    private UnsupportedGpuUsageProvider() {
    }

    public static UnsupportedGpuUsageProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public double getCurrentUtilization() {
        return -1;
    }
}
