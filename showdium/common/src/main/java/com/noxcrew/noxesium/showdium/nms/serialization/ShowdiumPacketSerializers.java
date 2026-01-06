package com.noxcrew.noxesium.showdium.nms.serialization;

import static com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry.registerSerializer;

import com.noxcrew.noxesium.showdium.network.clientbound.*;
import com.noxcrew.noxesium.showdium.network.serverbound.ServerboundKeybindTriggeredPacket;
import com.noxcrew.noxesium.showdium.network.serverbound.ServerboundPingAddPacket;
import com.noxcrew.noxesium.showdium.network.serverbound.ServerboundPingRemovePacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;

/**
 * Defines all common Showdium serializers.
 */
public class ShowdiumPacketSerializers {
    /**
     * Registers all serializers.
     */
    public static void register() {
        registerSerializer(
                ClientboundKeybindAddPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundKeybindAddPacket::KeyBindName,
                        ByteBufCodecs.BOOL,
                        ClientboundKeybindAddPacket::Client,
                        ByteBufCodecs.BOOL,
                        ClientboundKeybindAddPacket::singlePressTrigger,
                        ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
                        ClientboundKeybindAddPacket::delay,
                        ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
                        ClientboundKeybindAddPacket::clientQibImplementation,
                        ClientboundKeybindAddPacket::new));

        registerSerializer(
                ClientboundKeybindRemovePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundKeybindRemovePacket::KeyBindName,
                        ClientboundKeybindRemovePacket::new));

        registerSerializer(
                ServerboundKeybindTriggeredPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ServerboundKeybindTriggeredPacket::KeybindTriggered,
                        ByteBufCodecs.BOOL,
                        ServerboundKeybindTriggeredPacket::pressedIn,
                        ServerboundKeybindTriggeredPacket::new));

        registerSerializer(
                ClientboundPingAddPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.INT,
                        ClientboundPingAddPacket::color,
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundPingAddPacket::uuid,
                        ByteBufCodecs.VECTOR3F.map(
                                vector3fc -> new Vector3f(vector3fc.x(), vector3fc.y(), vector3fc.z()),
                                vector3f -> vector3f
                        ),
                        ClientboundPingAddPacket::location,
                        ClientboundPingAddPacket::new));

        registerSerializer(
                ClientboundPingRemovePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundPingRemovePacket::uuid,
                        ClientboundPingRemovePacket::new));

        registerSerializer(
                ServerboundPingAddPacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.INT,
                        ServerboundPingAddPacket::color,
                        ByteBufCodecs.STRING_UTF8,
                        ServerboundPingAddPacket::uuid,
                        ByteBufCodecs.VECTOR3F.map(
                                vector3fc -> new Vector3f(vector3fc.x(), vector3fc.y(), vector3fc.z()),
                                vector3f -> vector3f
                        ),
                        ServerboundPingAddPacket::location,
                        ServerboundPingAddPacket::new));

        registerSerializer(
                ServerboundPingRemovePacket.class,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ServerboundPingRemovePacket::uuid,
                        ServerboundPingRemovePacket::new));

        registerSerializer(
                ClientboundKeybindDisable.class,
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8,
                        ClientboundKeybindDisable::KeyBindName,
                        ClientboundKeybindDisable::new));
    }
}
