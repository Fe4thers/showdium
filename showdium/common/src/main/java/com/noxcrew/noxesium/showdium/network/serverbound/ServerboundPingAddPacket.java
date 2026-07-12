package com.noxcrew.noxesium.showdium.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import org.joml.Vector3f;

public record ServerboundPingAddPacket(Integer color, String uuid, Vector3f location) implements NoxesiumPacket {
    /**
     * add a keybind to the listener
     */
}
