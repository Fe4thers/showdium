package com.noxcrew.noxesium.showdium;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * A feature that listens for player ticks to prevent the small "hop" that occurs
 * when a player lands while gliding with an elytra.
 * <p>
 * This behavior is only active when the {@link ShowdiumGameComponent#RemoveElytraHops}
 * game component is enabled by the server.
 */
public final class ElytraListener extends NoxesiumFeature {

    private boolean wasFlyingLastTick = false;

    /**
     * Initializes the ElytraListener and registers its tick event handler.
     */
    public ElytraListener() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    /**
     * The core logic executed at the end of each client tick.
     * <p>
     * It checks if the player was gliding with an elytra on the previous tick and has
     * landed on the ground in the current tick. If this transition is detected, it
     * forcefully stops the flight state to prevent the vanilla landing hop.
     *
     * @param client The Minecraft client instance, provided by the tick event.
     */
    private void onEndTick(final Minecraft client) {
        if (!GameComponents.getInstance().noxesium$hasComponent(ShowdiumGameComponent.RemoveElytraHops)) {
            return;
        }

        final LocalPlayer player = client.player;
        if (player == null) {
            // Player is not in a world, reset state and do nothing.
            this.wasFlyingLastTick = false;
            return;
        }

        final boolean isGlidingThisTick = player.isFallFlying();
        final boolean isOnGroundThisTick = player.onGround();

        // Check for the specific transition from flying last tick to on-ground this tick.
        if (this.wasFlyingLastTick && isOnGroundThisTick) {
            // At the moment of landing, forcefully stop the flight state. This is the key
            // to preventing the hop, as it stops the game from applying landing logic
            // that would otherwise cause a small bounce.
            player.stopFallFlying();
        }

        // Update the state for the next tick's comparison.
        this.wasFlyingLastTick = isGlidingThisTick;
    }
}
