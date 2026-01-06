package com.noxcrew.noxesium.showdium.pingsystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * A simple positional sound instance for ping sounds.
 * Uses Minecraft's built-in 3D audio positioning.
 */
public class PingSoundInstance extends SimpleSoundInstance {

    /**
     * Creates a new ping sound at the specified position.
     */
    public PingSoundInstance(SoundEvent sound, SoundSource category, float volume, float pitch, Vec3 position) {
        super(sound, category, volume, pitch, RandomSource.create(), position.x, position.y, position.z);
    }

    /**
     * Creates a ping sound with virtual positioning for better directional audio.
     * Maps the actual distance to a closer range for clearer directional cues.
     */
    public static PingSoundInstance createDirectional(
            SoundEvent sound, SoundSource category, float volume, float pitch, Vec3 targetPosition) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            return new PingSoundInstance(sound, category, volume, pitch, targetPosition);
        }

        Vec3 playerPos = mc.player.position();
        Vec3 directionToPing = playerPos.vectorTo(targetPosition);

        // Map distance to a manageable range for audio (max 14 blocks for clear directional audio)
        double actualDistance = directionToPing.length();
        double maxDistance = 64.0;
        double soundRange = 14.0;
        double mappedDistance = Math.min(actualDistance, maxDistance) / maxDistance * soundRange;

        // Calculate virtual sound position
        if (actualDistance > 0.01) {
            Vec3 soundDirection = directionToPing.normalize().scale(mappedDistance);
            Vec3 soundPosition = playerPos.add(soundDirection);
            return new PingSoundInstance(sound, category, volume, pitch, soundPosition);
        }

        return new PingSoundInstance(sound, category, volume, pitch, targetPosition);
    }
}
