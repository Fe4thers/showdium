package com.noxcrew.noxesium.showdium.pingsystem;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Contains all resource identifiers for the ping system.
 */
public final class PingResources {

    private static final String NAMESPACE = "showdium";

    private PingResources() {
        // Prevent instantiation
    }

    // Sound resources
    public static final Identifier PING_SOUND_ID = Identifier.fromNamespaceAndPath(NAMESPACE, "ping");

    public static final SoundEvent PING_SOUND = SoundEvent.createVariableRangeEvent(PING_SOUND_ID);

    // Texture resources
    public static final Identifier PING_ICON_TEXTURE = Identifier.fromNamespaceAndPath(NAMESPACE, "textures/ping.png");

    public static final Identifier ARROW_ICON_TEXTURE =
            Identifier.fromNamespaceAndPath(NAMESPACE, "textures/arrow.png");
}
