package com.noxcrew.noxesium.showdium.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import org.joml.Vector3f;

public record ClientboundPingAddPacket(Integer color, String uuid, Vector3f location) implements NoxesiumPacket {
    /**
     * add a keybind to the listener
     */
}
