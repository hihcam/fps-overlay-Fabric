package net.hicham.fps_overlay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MacOsGpuUsageProvider implements GpuUsageProvider {
    private static final Logger LOGGER = LogManager.getLogger("FpsOverlayMacGpuProvider");
    private static final MacOsGpuUsageProvider INSTANCE = new MacOsGpuUsageProvider();
    private static final long REFRESH_INTERVAL_MS = 1_500L;
    private static final Pattern UTILIZATION_PATTERN =
            Pattern.compile("(?:Device|Renderer) Utilization %\"?\\s*=\\s*(\\d+(?:\\.\\d+)?)");
    private static final List<List<String>> COMMANDS = List.of(
            List.of("ioreg", "-r", "-d", "1", "-w", "0", "-c", "IOAccelerator"),
            List.of("ioreg", "-r", "-d", "1", "-w", "0", "-c", "AGXAccelerator"));

    private volatile long lastRefreshTime;
    private volatile double latestUtilization = -1;
    private volatile boolean commandUnavailable;

    private MacOsGpuUsageProvider() {
    }

    public static MacOsGpuUsageProvider getInstance() {
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
        if (commandUnavailable) {
            return -1;
        }

        for (List<String> command : COMMANDS) {
            double value = runIoreg(command);
            if (value >= 0) {
                return value;
            }
        }

        return -1;
    }

    private double runIoreg(List<String> command) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        try {
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                double max = -1;
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = UTILIZATION_PATTERN.matcher(line);
                    while (matcher.find()) {
                        double value = Double.parseDouble(matcher.group(1));
                        max = Math.max(max, Math.min(100.0, value));
                    }
                }

                process.waitFor();
                return process.exitValue() == 0 ? max : -1;
            } finally {
                process.destroy();
            }
        } catch (IOException e) {
            commandUnavailable = true;
            LOGGER.debug("macOS GPU polling command is unavailable", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (NumberFormatException ignored) {
        }

        return -1;
    }
}
