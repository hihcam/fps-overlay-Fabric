package net.hicham.fps_overlay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

public final class LinuxGpuUsageProvider implements GpuUsageProvider {
    private static final Logger LOGGER = LogManager.getLogger("FpsOverlayLinuxGpuProvider");
    private static final LinuxGpuUsageProvider INSTANCE = new LinuxGpuUsageProvider();
    private static final long REFRESH_INTERVAL_MS = 1_000L;
    private static final int MAX_CARDS = 8;
    private static final List<String> SYSFS_TEMPLATES = List.of(
            "/sys/class/drm/card%d/device/gpu_busy_percent",
            "/sys/class/drm/card%d/device/mem_busy_percent",
            "/sys/class/drm/card%d/gt_busy_percent",
            "/sys/class/drm/card%d/device/gt_busy_percent");

    private volatile long lastRefreshTime;
    private volatile double latestUtilization = -1;
    private volatile boolean sysfsUnavailable;
    private volatile boolean nvidiaSmiUnavailable;

    private LinuxGpuUsageProvider() {
    }

    public static LinuxGpuUsageProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public double getCurrentUtilization() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime < REFRESH_INTERVAL_MS) {
            return latestUtilization;
        }

        synchronized (this) {
            currentTime = System.currentTimeMillis();
            if (currentTime - lastRefreshTime < REFRESH_INTERVAL_MS) {
                return latestUtilization;
            }

            latestUtilization = refreshSample();
            lastRefreshTime = currentTime;
            return latestUtilization;
        }
    }

    private double refreshSample() {
        double sysfsValue = readSysfsUtilization();
        if (sysfsValue >= 0) {
            return sysfsValue;
        }

        return readNvidiaSmiUtilization();
    }

    private double readSysfsUtilization() {
        if (sysfsUnavailable) {
            return -1;
        }

        double max = -1;
        boolean foundFile = false;
        for (int cardIndex = 0; cardIndex < MAX_CARDS; cardIndex++) {
            for (String template : SYSFS_TEMPLATES) {
                Path path = Path.of(template.formatted(cardIndex));
                if (!Files.isRegularFile(path)) {
                    continue;
                }

                foundFile = true;
                double value = readPercentage(path);
                if (value >= 0) {
                    max = Math.max(max, value);
                }
            }
        }

        if (!foundFile) {
            sysfsUnavailable = true;
        }
        return max;
    }

    private double readNvidiaSmiUtilization() {
        if (nvidiaSmiUnavailable) {
            return -1;
        }

        ProcessBuilder builder = new ProcessBuilder(
                "nvidia-smi",
                "--query-gpu=utilization.gpu",
                "--format=csv,noheader,nounits");
        builder.redirectErrorStream(true);

        try {
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                double max = -1;
                String line;
                while ((line = reader.readLine()) != null) {
                    double value = parseNumber(line);
                    if (value >= 0) {
                        max = Math.max(max, value);
                    }
                }

                process.waitFor();
                if (process.exitValue() != 0 && max < 0) {
                    nvidiaSmiUnavailable = true;
                }
                return max;
            } finally {
                process.destroy();
            }
        } catch (IOException e) {
            nvidiaSmiUnavailable = true;
            LOGGER.debug("nvidia-smi is unavailable for GPU polling", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return -1;
    }

    private static double readPercentage(Path path) {
        try {
            return clamp(parseNumber(Files.readString(path, StandardCharsets.UTF_8)));
        } catch (IOException e) {
            return -1;
        }
    }

    private static double parseNumber(String text) {
        if (text == null || text.isBlank()) {
            return -1;
        }

        String normalized = text.trim().replace("%", "");
        try {
            return clamp(Double.parseDouble(normalized));
        } catch (NumberFormatException ignored) {
        }

        try {
            Number number = NumberFormat.getNumberInstance(Locale.getDefault()).parse(normalized);
            return number != null ? clamp(number.doubleValue()) : -1;
        } catch (ParseException ignored) {
            return -1;
        }
    }

    private static double clamp(double value) {
        return Double.isFinite(value) && value >= 0 ? Math.min(100.0, value) : -1;
    }
}
