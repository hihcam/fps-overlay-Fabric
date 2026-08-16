package net.hicham.fps_overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * Static utility methods for fetching individual performance metrics
 * from the Minecraft client. Each method is self-contained and handles
 * its own error cases, returning a typed record.
 */
public final class MetricProvider {

    private MetricProvider() {
    }

    // ── Memory ──────────────────────────────────────────────────

    public record MemoryData(long used, long max) {
    }

    public static MemoryData fetchMemory() {
        Runtime runtime = Runtime.getRuntime();
        return new MemoryData(runtime.totalMemory() - runtime.freeMemory(), runtime.maxMemory());
    }

    // ── Ping ────────────────────────────────────────────────────

    private static net.minecraft.client.multiplayer.ClientPacketListener lastHandler = null;
    private static long stuckStartTime = 0L;
    private static boolean useFallback = false;
    private static long lastRequestTime = 0L;
    private static int lastSampledPing = 0;

    private static void resetFallbackState() {
        stuckStartTime = 0L;
        useFallback = false;
        lastRequestTime = 0L;
        lastSampledPing = 0;
    }

    public static int fetchPing(Minecraft client) {
        var handler = client.getConnection();
        var player = client.player;
        if (handler == null || player == null) {
            lastHandler = null;
            resetFallbackState();
            return 0;
        }

        // Reset fallback state on server change / reconnect
        if (handler != lastHandler) {
            lastHandler = handler;
            resetFallbackState();
        }

        try {
            var entry = handler.getPlayerInfo(player.getUUID());
            int latency = entry != null ? Math.max(0, entry.getLatency()) : 0;

            // Fallback detection: some servers (e.g. Hypixel SkyBlock, BedWars) leave
            // PlayerInfo#getLatency() stuck at 1ms. If we see latency==1 on a remote
            // server for more than 5 seconds, switch to active RTT measurement via
            // ServerboundPingRequestPacket / debug overlay ping logger.
            if (!client.isLocalServer() && latency == 1) {
                if (stuckStartTime == 0L) {
                    stuckStartTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - stuckStartTime > 5000L) {
                    useFallback = true;
                }
            } else {
                stuckStartTime = 0L;
                useFallback = false;
            }

            if (useFallback) {
                long now = System.currentTimeMillis();
                if (now - lastRequestTime > 2000L) {
                    lastRequestTime = now;
                    handler.send(new net.minecraft.network.protocol.ping.ServerboundPingRequestPacket(net.minecraft.util.Util.getMillis()));
                }

                var debugOverlay = client.getDebugOverlay();
                if (debugOverlay != null) {
                    var pingLogger = debugOverlay.getPingLogger();
                    if (pingLogger != null && pingLogger.size() > 0) {
                        lastSampledPing = (int) pingLogger.get(pingLogger.size() - 1);
                    }
                }
                return lastSampledPing > 0 ? lastSampledPing : latency;
            }

            return latency;
        } catch (Exception e) {
            return 0;
        }
    }


    // ── Tick (MSPT / TPS) ───────────────────────────────────────

    public record TickData(double mspt, double tps) {
    }

    public static TickData fetchTickData(Minecraft client) {
        var server = client.getSingleplayerServer();
        if (server != null) {
            double mspt = server.getAverageTickTimeNanos() / 1_000_000.0;
            double tps = Math.min(20.0, 1000.0 / Math.max(1.0, mspt));
            return new TickData(mspt, tps);
        }
        return new TickData(-1, -1);
    }

    // ── Chunks ──────────────────────────────────────────────────

    public record ChunkData(int loaded, int visible, int completed) {
    }

    public static ChunkData fetchChunks(Minecraft client) {
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
            visible = Math.max(0, client.levelExtractor.countRenderedSections());
        } catch (Exception ignored) {
        }

        try {
            completed = Math.max(0, client.levelRenderer.visibleSections().size());
        } catch (Exception ignored) {
            completed = visible;
        }

        return new ChunkData(loaded, visible, completed);
    }

    // ── Location (Coordinates + Biome) ──────────────────────────

    public record LocationData(String coordinates, String biome) {
    }

    public static LocationData fetchLocation(Minecraft client) {
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
}
