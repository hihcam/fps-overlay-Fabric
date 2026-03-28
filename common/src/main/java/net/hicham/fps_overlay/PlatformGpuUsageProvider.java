package net.hicham.fps_overlay;

import java.util.Locale;

public final class PlatformGpuUsageProvider implements GpuUsageProvider {
    private static final PlatformGpuUsageProvider INSTANCE = new PlatformGpuUsageProvider();

    private final GpuUsageProvider provider = createProvider();

    private PlatformGpuUsageProvider() {
    }

    public static PlatformGpuUsageProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public double getCurrentUtilization() {
        return provider.getCurrentUtilization();
    }

    private static GpuUsageProvider createProvider() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            return WindowsGpuUsageProvider.getInstance();
        }
        if (osName.contains("linux")) {
            return LinuxGpuUsageProvider.getInstance();
        }
        if (osName.contains("mac") || osName.contains("darwin")) {
            return MacOsGpuUsageProvider.getInstance();
        }
        return UnsupportedGpuUsageProvider.getInstance();
    }
}
