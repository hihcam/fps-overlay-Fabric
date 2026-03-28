package net.hicham.fps_overlay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class WindowsGpuUsagePoller {
    private static final Logger LOGGER = LogManager.getLogger("FpsOverlayGpuPoller");
    private static final String WINDOWS_NAME = "windows";
    private static final String COUNTER_PATH = "\\GPU Engine(*)\\Utilization Percentage";
    private static final WindowsGpuUsagePoller INSTANCE = new WindowsGpuUsagePoller();

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean supported = new AtomicBoolean(isWindows());
    private final AtomicReference<Double> latestUtilization = new AtomicReference<>(Double.NaN);

    private volatile Process process;

    private WindowsGpuUsagePoller() {
    }

    public static WindowsGpuUsagePoller getInstance() {
        return INSTANCE;
    }

    public double getCurrentUtilization() {
        ensureStarted();
        Double current = latestUtilization.get();
        return current != null && Double.isFinite(current) ? current : -1;
    }

    private void ensureStarted() {
        if (!supported.get() || !started.compareAndSet(false, true)) {
            return;
        }

        Thread worker = new Thread(this::runPollingLoop, "fps-overlay-gpu-poller");
        worker.setDaemon(true);
        worker.start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stopProcess, "fps-overlay-gpu-poller-shutdown"));
    }

    private void runPollingLoop() {
        ProcessBuilder builder = new ProcessBuilder("typeperf", COUNTER_PATH, "-si", "1");
        builder.redirectErrorStream(true);

        try {
            process = builder.start();
        } catch (IOException e) {
            supported.set(false);
            LOGGER.debug("Windows GPU counters are unavailable", e);
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            streamSamples(reader);
        } catch (IOException e) {
            supported.set(false);
            LOGGER.debug("Failed to read Windows GPU counters", e);
        } finally {
            stopProcess();
        }
    }

    private void streamSamples(BufferedReader reader) throws IOException {
        String line;
        boolean sawHeader = false;

        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }

            if (!sawHeader) {
                if (line.startsWith("\"(PDH-CSV")) {
                    sawHeader = true;
                } else if (looksLikeCounterFailure(line)) {
                    supported.set(false);
                    return;
                }
                continue;
            }

            if (looksLikeCounterFailure(line)) {
                supported.set(false);
                return;
            }

            if (!line.startsWith("\"")) {
                continue;
            }

            double sample = parseSample(line);
            if (sample >= 0) {
                latestUtilization.set(sample);
            }
        }
    }

    private static boolean looksLikeCounterFailure(String line) {
        String normalized = line.toLowerCase(Locale.ROOT);
        return normalized.contains("error:")
                || normalized.contains("exiting, please wait")
                || normalized.contains("no valid counters")
                || normalized.contains("unable to add these counters");
    }

    private static double parseSample(String line) {
        List<String> columns = splitCsv(line);
        if (columns.size() <= 1) {
            return -1;
        }

        double max = 0;
        boolean foundValue = false;
        for (int i = 1; i < columns.size(); i++) {
            double value = parseNumber(columns.get(i));
            if (value < 0) {
                continue;
            }

            max = Math.max(max, value);
            foundValue = true;
        }

        return foundValue ? Math.min(100.0, max) : -1;
    }

    private static List<String> splitCsv(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (ch == ',' && !inQuotes) {
                columns.add(current.toString().trim());
                current.setLength(0);
                continue;
            }

            current.append(ch);
        }

        columns.add(current.toString().trim());
        return columns;
    }

    private static double parseNumber(String text) {
        if (text == null || text.isBlank()) {
            return -1;
        }

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ignored) {
        }

        try {
            NumberFormat format = NumberFormat.getNumberInstance();
            Number number = format.parse(text);
            return number != null ? number.doubleValue() : -1;
        } catch (ParseException ignored) {
            return -1;
        }
    }

    private void stopProcess() {
        Process activeProcess = process;
        if (activeProcess != null) {
            activeProcess.destroy();
            process = null;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains(WINDOWS_NAME);
    }
}
