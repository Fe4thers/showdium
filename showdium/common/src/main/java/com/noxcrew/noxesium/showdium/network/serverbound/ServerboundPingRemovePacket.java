package com.noxcrew.noxesium.showdium.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

public record ServerboundPingRemovePacket(String uuid) implements NoxesiumPacket {
    /**
     * add a keybind to the listener
     */
}
