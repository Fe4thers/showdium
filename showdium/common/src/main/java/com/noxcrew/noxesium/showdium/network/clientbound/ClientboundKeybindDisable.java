package com.noxcrew.noxesium.showdium.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

public record ClientboundKeybindDisable(String KeyBindName) implements NoxesiumPacket {
    /**
     * add a keybind to the listener
     */
}
