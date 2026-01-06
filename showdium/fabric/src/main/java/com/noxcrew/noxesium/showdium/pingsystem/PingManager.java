package com.noxcrew.noxesium.showdium.pingsystem;

import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.showdium.ShowdiumEntrypoint;
import com.noxcrew.noxesium.showdium.config.PingSystemConfig;
import com.noxcrew.noxesium.showdium.network.serverbound.ServerboundPingAddPacket;
import com.noxcrew.noxesium.showdium.network.serverbound.ServerboundPingRemovePacket;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Manages all active pings in the game.
 * Handles creation, removal, and state management of pings.
 * Includes anti-spam protection to prevent abuse.
 */
public final class PingManager {

    private static final Map<UUID, PingEntry> activePings = new ConcurrentHashMap<>();
    private static final double MAX_RAYCAST_DISTANCE = 1000.0;
    private static final double FALLBACK_PING_DISTANCE = 10.0;

    // Anti-spam configuration
    private static final long PING_COOLDOWN_MS = 500L;
    private static final long SOUND_COOLDOWN_MS = 500L;

    // Anti-spam tracking
    private static final Map<UUID, Long> lastPingTime = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> lastSoundTime = new ConcurrentHashMap<>();

    // Input state tracking
    private static boolean previousKeyState = false;
    private static long keyPressStartTime = 0L;

    private PingManager() {}

    /**
     * Gets all active pings, removing expired ones.
     */
    public static Collection<PingEntry> getActivePings() {
        activePings.values().removeIf(PingEntry::hasExpired);
        return activePings.values();
    }

    /**
     * Clears all active pings and resets anti-spam trackers.
     */
    public static void clearAllPings() {
        activePings.clear();
        lastPingTime.clear();
        lastSoundTime.clear();
    }

    /**
     * Creates a ping at the specified position (local player).
     */
    public static void createPing(Vec3 position, int color) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) {
            return;
        }

        // Check if ping system is enabled
        if (!PingSystemConfig.isEnabled()) {
            return;
        }

        UUID playerId = mc.player.getUUID();

        if (!isOffCooldown(playerId)) {
            return;
        }

        removePlayerPings(playerId);

        UUID pingId = UUID.randomUUID();
        PingEntry ping = PingEntry.create(position, null, playerId, 0, 0);
        ping.setCustomColor(color);

        activePings.put(pingId, ping);
        recordPingTime(playerId);

        Vector3f posVector = new Vector3f((float) position.x, (float) position.y, (float) position.z);
        NoxesiumServerboundNetworking.send(new ServerboundPingAddPacket(color, playerId.toString(), posVector));
    }

    /**
     * Creates a ping from another player (received from server).
     */
    public static void createRemotePing(Vec3 position, int color, UUID creatorId) {
        // Check if ping system is enabled
        if (!PingSystemConfig.isEnabled()) {
            return;
        }

        if (!isOffCooldown(creatorId)) {
            return;
        }

        removePlayerPings(creatorId);

        UUID pingId = UUID.randomUUID();
        PingEntry ping = PingEntry.create(position, null, creatorId, 0, 0);
        ping.setCustomColor(color);

        activePings.put(pingId, ping);
        recordPingTime(creatorId);

        // Play sound - ensure it runs on main thread
        playPingSoundIfAllowed(creatorId, position);
    }

    /**
     * Removes all pings created by the specified player.
     */
    public static void removePlayerPings(UUID playerId) {
        activePings
                .values()
                .removeIf(ping ->
                        ping.getCreatorId() != null && ping.getCreatorId().equals(playerId));
    }

    /**
     * Removes pings by the local player and notifies the server.
     */
    public static void removeLocalPlayerPings(UUID playerId) {
        removePlayerPings(playerId);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && playerId.equals(mc.player.getUUID())) {
            NoxesiumServerboundNetworking.send(new ServerboundPingRemovePacket(playerId.toString()));
        }
    }

    /**
     * Creates a ping at the player's crosshair location.
     */
    public static void createCrosshairPing(Minecraft mc, int color) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Check if ping system is enabled
        if (!PingSystemConfig.isEnabled()) {
            return;
        }

        if (!isOffCooldown(mc.player.getUUID())) {
            return;
        }

        Vec3 lookDirection = mc.player.getViewVector(1.0f);
        HitResult hit = RaycastHelper.performRaycast(lookDirection, 0f, MAX_RAYCAST_DISTANCE, true);

        Vec3 pingPosition;

        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            pingPosition = mc.player.position().add(lookDirection.scale(FALLBACK_PING_DISTANCE));
        } else {
            pingPosition = hit.getLocation();
        }

        createPing(pingPosition, color);
    }

    /**
     * Checks if the player's ping cooldown has expired.
     */
    private static boolean isOffCooldown(UUID playerId) {
        Long lastTime = lastPingTime.get(playerId);
        if (lastTime == null) {
            return true;
        }
        return System.currentTimeMillis() - lastTime >= PING_COOLDOWN_MS;
    }

    /**
     * Records the current time as the last ping time for a player.
     */
    private static void recordPingTime(UUID playerId) {
        lastPingTime.put(playerId, System.currentTimeMillis());
    }

    /**
     * Plays the ping sound if the player hasn't played one recently.
     * Ensures the sound is played on the main Minecraft thread.
     */
    private static void playPingSoundIfAllowed(UUID playerId, Vec3 position) {
        // Check volume - if 0 or disabled, don't play
        float volume = PingSystemConfig.getVolume();
        if (volume <= 0.0f || !PingSystemConfig.isEnabled()) {
            return;
        }

        Long lastSound = lastSoundTime.get(playerId);
        long now = System.currentTimeMillis();

        if (lastSound != null && now - lastSound < SOUND_COOLDOWN_MS) {
            return;
        }

        lastSoundTime.put(playerId, now);

        // Ensure sound is played on the main thread
        Minecraft mc = Minecraft.getInstance();
        final float finalVolume = volume;
        final Vec3 finalPosition = position;

        mc.execute(() -> {
            if (ShowdiumEntrypoint.GAME != null && ShowdiumEntrypoint.GAME.getSoundManager() != null) {
                PingSoundInstance soundInstance = PingSoundInstance.createDirectional(
                        PingResources.PING_SOUND, SoundSource.PLAYERS, finalVolume, 1.0f, finalPosition);
                ShowdiumEntrypoint.GAME.getSoundManager().play(soundInstance);
            }
        });
    }

    // Input state accessors

    public static boolean getPreviousKeyState() {
        return previousKeyState;
    }

    public static void setPreviousKeyState(boolean state) {
        previousKeyState = state;
    }

    public static long getKeyPressStartTime() {
        return keyPressStartTime;
    }

    public static void setKeyPressStartTime(long time) {
        keyPressStartTime = time;
    }
}
