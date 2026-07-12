package com.noxcrew.noxesium.showdium.registry;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.api.NoxesiumReferences;
import com.noxcrew.noxesium.api.component.NoxesiumComponentType;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumCodecs;
import com.noxcrew.noxesium.api.nms.codec.NoxesiumStreamCodecs;
import com.noxcrew.noxesium.api.nms.serialization.ComponentSerializerRegistry;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import com.noxcrew.noxesium.api.util.Unit;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stores blocks that should not be interactable on the client
 */
public class ShowdiumGameComponent {
    public static final RegistryCollection<NoxesiumComponentType<?>> INSTANCE =
            new RegistryCollection<>(NoxesiumRegistries.GAME_COMPONENTS);

    /**
     * Makes a set of blocks non Interactable.
     */
    @SuppressWarnings("unchecked")
    public static NoxesiumComponentType<String> NoBlockInteractions =
            register("no_block_interaction", Codec.string(0, 256), String.class);

    /**
     * Makes it so all blocks do the same knockback as slime while being pushed by a piston
     * send a "" to activate for all blocks, put a string with "11,12,13" to remove the slime effect for y lvls
     */
    @SuppressWarnings("unchecked")
    public static NoxesiumComponentType<String> SlimeBlockBlocks =
            register("more_slime_blocks", Codec.string(0, 256), String.class);

    /**
     * disables subtitles
     */
    public static NoxesiumComponentType<com.noxcrew.noxesium.api.util.Unit> DisableSubtitles =
            register("disable_subtitles", NoxesiumCodecs.UNIT, com.noxcrew.noxesium.api.util.Unit.class);
    /**
     * disable hitting other players
     */
    public static NoxesiumComponentType<com.noxcrew.noxesium.api.util.Unit> NoPlayerAttacking =
            register("no_player_attacking", NoxesiumCodecs.UNIT, com.noxcrew.noxesium.api.util.Unit.class);
    /**
     * forces the player in a perspective
     * 0 = first person
     * 1 = third person back
     * 2 = third person front
     */
    public static NoxesiumComponentType<Integer> ForcePerspective =
            register("force_player_perspective", Codec.INT, Integer.class);

    /**
     * adds collision to structure voids
     */
    public static NoxesiumComponentType<Unit> StructureVoidsWithCollision =
            register("structure_voids_with_collision", NoxesiumCodecs.UNIT, Unit.class);

    /**
     * removes the ability of higher ping players to elytra-hop
     */
    public static NoxesiumComponentType<Unit> RemoveElytraHops =
            register("remove_elytra_hops", NoxesiumCodecs.UNIT, Unit.class);
    /**
     * adds the showdium loading screen to resourcepack loading
     */
    public static NoxesiumComponentType<Unit> ShowdiumLoadingScreen =
            register("showdium_loading_screen", NoxesiumCodecs.UNIT, Unit.class);

    /**
     * adds the ping system
     */
    public static NoxesiumComponentType<Unit> PingSystem = register("ping_system", NoxesiumCodecs.UNIT, Unit.class);

    /**
     * adds the team color for the ping system
     */
    public static NoxesiumComponentType<Integer> TeamColor = register("team_color", Codec.INT, Integer.class);

    /**
     * Registers a new component type to the registry.
     */
    @SuppressWarnings("unchecked")
    private static <T> NoxesiumComponentType<T> register(String key, Codec<T> codec, Class<T> clazz) {
        var type = NoxesiumRegistries.<T>register(INSTANCE, NoxesiumReferences.NAMESPACE, key, clazz);
        if (clazz == String.class) {
            StreamCodec<RegistryFriendlyByteBuf, String> stringStreamCodec =
                    StreamCodec.of(RegistryFriendlyByteBuf::writeUtf, RegistryFriendlyByteBuf::readUtf);

            ComponentSerializerRegistry.registerSerializers(
                    NoxesiumRegistries.GAME_COMPONENTS,
                    (NoxesiumComponentType<String>) type,
                    (Codec<String>) codec,
                    stringStreamCodec,
                    null);
        } else if (clazz == Unit.class) {
            ComponentSerializerRegistry.registerSerializers(
                    NoxesiumRegistries.GAME_COMPONENTS,
                    (NoxesiumComponentType<com.noxcrew.noxesium.api.util.Unit>) type,
                    (Codec<com.noxcrew.noxesium.api.util.Unit>) codec,
                    NoxesiumStreamCodecs.UNIT,
                    null);
        } else if (clazz == Integer.class) {
            StreamCodec<RegistryFriendlyByteBuf, Integer> integerStreamCodec =
                    StreamCodec.of(RegistryFriendlyByteBuf::writeInt, RegistryFriendlyByteBuf::readInt);

            ComponentSerializerRegistry.registerSerializers(
                    NoxesiumRegistries.GAME_COMPONENTS,
                    (NoxesiumComponentType<Integer>) type,
                    (Codec<Integer>) codec,
                    integerStreamCodec,
                    null);
        } else {
            ComponentSerializerRegistry.registerSerializers(
                    NoxesiumRegistries.GAME_COMPONENTS, type, codec, null, null);
        }

        return type;
    }
}
