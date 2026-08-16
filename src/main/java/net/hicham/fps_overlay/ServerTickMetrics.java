package net.hicham.fps_overlay;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public final class ServerTickMetrics {
    private static final AtomicLong tier1MsptBits = new AtomicLong(Double.doubleToRawLongBits(0.0));
    private static final AtomicLong tier1TpsBits = new AtomicLong(Double.doubleToRawLongBits(20.0));
    private static final AtomicLong tier1LastReceivedNano = new AtomicLong(0);

    private static final AtomicLong tier2TpsBits = new AtomicLong(Double.doubleToRawLongBits(20.0));
    private static final AtomicLong tier2LastReceivedNano = new AtomicLong(0);
    private static final AtomicLong tier2PrevPacketNano = new AtomicLong(0);

    public static void onDebugSample(long[] samples) {
        if (samples == null || samples.length == 0) return;
        long value = samples[samples.length - 1];
        double mspt = value / 1_000_000.0;
        double tps = Math.min(20.0, 1000.0 / Math.max(1.0, mspt));
        tier1MsptBits.set(Double.doubleToRawLongBits(mspt));
        tier1TpsBits.set(Double.doubleToRawLongBits(tps));
        tier1LastReceivedNano.set(System.nanoTime());
    }

    public static void onSetTime() {
        long now = System.nanoTime();
        long prev = tier2PrevPacketNano.get();
        if (prev == 0) {
            tier2PrevPacketNano.compareAndSet(0, now);
            return;
        }
        long elapsed = now - prev;
        if (elapsed < 200_000_000L) {
            // Ignore packets arriving less than 200ms apart (packet bursts)
            return;
        }
        if (tier2PrevPacketNano.compareAndSet(prev, now)) {
            double elapsedRealSeconds = elapsed / 1_000_000_000.0;
            double estimatedTPS = Math.min(20.0, 20.0 / elapsedRealSeconds);
            tier2TpsBits.set(Double.doubleToRawLongBits(estimatedTPS));
            tier2LastReceivedNano.set(now);
        }
    }

    public static void onJoinServer(Connection connection) {
        tier1MsptBits.set(Double.doubleToRawLongBits(0.0));
        tier1TpsBits.set(Double.doubleToRawLongBits(20.0));
        tier1LastReceivedNano.set(0);
        tier2TpsBits.set(Double.doubleToRawLongBits(20.0));
        tier2LastReceivedNano.set(0);
        tier2PrevPacketNano.set(0);
        if (connection != null) {
            connection.send(new ServerboundDebugSubscriptionRequestPacket(Set.of(net.minecraft.util.debug.DebugSubscriptions.DEDICATED_SERVER_TICK_TIME)));
        }
    }

    public static double getTPS() {
        long last = tier1LastReceivedNano.get();
        if (last == 0) {
            return Double.longBitsToDouble(tier1TpsBits.get());
        }
        long now = System.nanoTime();
        double timeSinceLastPacket = (now - last) / 1_000_000_000.0;
        double baseTps = Double.longBitsToDouble(tier1TpsBits.get());
        if (timeSinceLastPacket > 1.5) {
            // Sk1ght CurseForge Bug Report: Handle server freeze/lag spike gracefully.
            double decayingTps = 20.0 / timeSinceLastPacket;
            double estimatedTps = Math.min(baseTps, decayingTps);
            return estimatedTps < 1.0 ? 0.0 : estimatedTps;
        }
        return baseTps;
    }

    public static double getMSPT() {
        long last = tier1LastReceivedNano.get();
        if (last == 0) {
            return Double.longBitsToDouble(tier1MsptBits.get());
        }
        long now = System.nanoTime();
        double timeSinceLastPacket = (now - last) / 1_000_000_000.0;
        double baseMspt = Double.longBitsToDouble(tier1MsptBits.get());
        if (timeSinceLastPacket > 1.5) {
            // Sk1ght CurseForge Bug Report: Show N/A for MSPT during freeze, consistent with Tier 2.
            return -1.0;
        }
        return baseMspt;
    }

    public static double getTier2TPS() {
        long last = tier2LastReceivedNano.get();
        if (last == 0) {
            return Double.longBitsToDouble(tier2TpsBits.get());
        }
        long now = System.nanoTime();
        double timeSinceLastPacket = (now - last) / 1_000_000_000.0;
        double baseTps = Double.longBitsToDouble(tier2TpsBits.get());
        if (timeSinceLastPacket > 1.5) {
            // Sk1ght CurseForge Bug Report: Handle server freeze/lag spike gracefully.
            double decayingTps = 20.0 / timeSinceLastPacket;
            double estimatedTps = Math.min(baseTps, decayingTps);
            return estimatedTps < 1.0 ? 0.0 : estimatedTps;
        }
        return baseTps;
    }

    public static boolean isTier1Active() {
        long last = tier1LastReceivedNano.get();
        if (last == 0) {
            return false;
        }
        // Sk1ght CurseForge Bug Report: Revert to N/A after 30 seconds of total silence.
        return (System.nanoTime() - last) < 30_000_000_000L;
    }

    public static boolean isTier2Active() {
        long last = tier2LastReceivedNano.get();
        if (last == 0) {
            return false;
        }
        // Sk1ght CurseForge Bug Report: Revert to N/A after 30 seconds of total silence.
        return (System.nanoTime() - last) < 30_000_000_000L;
    }
}
