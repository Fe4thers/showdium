package com.noxcrew.noxesium.showdium;

import com.noxcrew.noxesium.api.ClientNoxesiumEntrypoint;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.network.PacketCollection;
import com.noxcrew.noxesium.api.registry.RegistryCollection;
import com.noxcrew.noxesium.showdium.network.ShowdiumPackets;
import com.noxcrew.noxesium.showdium.nms.serialization.ShowdiumPacketSerializers;
import com.noxcrew.noxesium.showdium.pingsystem.PingSystemFeature;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Sets up an entrypoint into Noxesium's APIs.
 */
public class ShowdiumEntrypoint implements ClientNoxesiumEntrypoint {

    @Override
    public String getId() {
        return "showdium";
    }

    @Override
    public String getVersion() {
        return "1.0.2";
    }

    @Override
    @Nullable
    public URL getEncryptionKey() {
        return ShowdiumEntrypoint.class.getClassLoader().getResource("encryption-key.aes");
    }

    public static Minecraft GAME = null;
    public static KeyBindHandler keyBindHandler;
    public static QibBehaviorExecutor qibBehaviorExecutor;
    public static ShowdiumPacketHandling packetHandling;
    public static ElytraListener elytraListener;
    public static PingSystemFeature pingSystem;
    public static KeyMapping.Category Keybindcategory =
            KeyMapping.Category.register(Identifier.parse("category.noxesium"));

    @Override
    public void preInitialize() {
        GAME = Minecraft.getInstance();
        ShowdiumPacketSerializers.register();
        boolean isLoaded = FabricLoader.getInstance().isModLoaded("debugify");
        if (isLoaded) {
            new DebugifyDisables();
        }
    }

    @Override
    public void initialize() {
        qibBehaviorExecutor = new QibBehaviorExecutor();
        keyBindHandler = new KeyBindHandler();
        packetHandling = new ShowdiumPacketHandling();
        elytraListener = new ElytraListener();
        pingSystem = new PingSystemFeature();
    }

    @Override
    public Collection<NoxesiumFeature> getAllFeatures() {
        var features = new ArrayList<NoxesiumFeature>();
        features.add(keyBindHandler);
        features.add(packetHandling);
        features.add(qibBehaviorExecutor);
        features.add(elytraListener);
        features.add(pingSystem);
        return features;
    }

    @Override
    public Collection<PacketCollection> getPacketCollections() {
        return List.of(ShowdiumPackets.INSTANCE);
    }

    @Override
    public Collection<RegistryCollection<?>> getRegistryCollections() {
        return List.of(ShowdiumGameComponent.INSTANCE);
    }
}
