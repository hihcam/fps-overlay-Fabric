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
        try {
            Runtime runtime = Runtime.getRuntime();
            return new MemoryData(runtime.totalMemory() - runtime.freeMemory(), runtime.maxMemory());
        } catch (Exception e) {
            return new MemoryData(0, 0);
        }
    }

    // ── Ping ────────────────────────────────────────────────────

    public static int fetchPing(Minecraft client) {
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
