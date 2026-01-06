package com.noxcrew.noxesium.showdium

import com.noxcrew.noxesium.api.NoxesiumApi
import com.noxcrew.noxesium.api.feature.qib.QibCondition
import com.noxcrew.noxesium.api.feature.qib.QibDefinition
import com.noxcrew.noxesium.api.feature.qib.QibEffect
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries
import com.noxcrew.noxesium.api.util.Unit
import com.noxcrew.noxesium.core.registry.CommonEntityComponentTypes
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.component.noxesiumPlayer
import com.noxcrew.noxesium.paper.component.setNoxesiumComponent
import com.noxcrew.noxesium.paper.feature.ListeningNoxesiumFeature
import com.noxcrew.noxesium.showdium.network.clientbound.ClientboundKeybindAddPacket
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import java.util.Optional
import java.util.UUID

public class TestListener() : ListeningNoxesiumFeature() {
    @EventHandler
    public fun playerjoinevent(event: PlayerJoinEvent) {
        Bukkit.getScheduler().runTaskLater(
            NoxesiumPaper.plugin,
            Runnable {
                val gameComponents = event.player.noxesiumPlayer?.gameComponents
                val blocks =
                    "${
                        Material.OAK_TRAPDOOR.name.lowercase()}," +
                        Material.SPRUCE_TRAPDOOR.name.lowercase()
                gameComponents?.`noxesium$setComponent`(ShowdiumGameComponent.NoBlockInteractions, blocks)
                gameComponents?.`noxesium$setComponent`(ShowdiumGameComponent.SlimeBlockBlocks, "101")
                gameComponents?.`noxesium$setComponent`(ShowdiumGameComponent.NoPlayerAttacking, Unit.INSTANCE)
                gameComponents?.`noxesium$setComponent`(ShowdiumGameComponent.DisableSubtitles, Unit.INSTANCE)
                gameComponents?.`noxesium$setComponent`(ShowdiumGameComponent.ForcePerspective, 1)
                gameComponents?.`noxesium$setComponent`(ShowdiumGameComponent.StructureVoidsWithCollision, Unit.INSTANCE)
                gameComponents?.`noxesium$setComponent`(ShowdiumGameComponent.RemoveElytraHops, Unit.INSTANCE)
                gameComponents?.`noxesium$setComponent`(ShowdiumGameComponent.ShowdiumLoadingScreen, Unit.INSTANCE)

                val velocityYawPitch =
                    QibEffect.SetVelocityYawPitch(
                        0.0,
                        true, // yawRelative: true if you want it relative to the player's current yaw
                        -7.5,
                        false, // pitchRelative: true if you want it relative to the player's current pitch
                        2.3,
                        10.0
                    )
                val Wait = QibEffect.Wait(1, velocityYawPitch)

                // val soundParts = soundName.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                // val namespace = soundParts[0]
                // val path = soundParts[1]
                val sound = QibEffect.PlaySound("showdium", "launchpad", 1.0f, 1.0f)
                // val sound = QibEffect.PlaySound(namespace, path, 1.0f, 1.0f)
                val qibsbounce: java.util.ArrayList<QibEffect> = ArrayList()
                qibsbounce.add(sound)
                qibsbounce.add(Wait)
                val BouceLogic = QibEffect.Multiple(qibsbounce)
                val qibDefinitionBounce = QibDefinition(null, null, null, BouceLogic, null, false)

                NoxesiumRegistries.QIB_EFFECTS.register(Key.key("bounce_pad"), qibDefinitionBounce)

                val velocity =
                    QibEffect.SetVelocityYawPitch(
                        0.0,
                        true,
                        -75.5,
                        false,
                        1.15,
                        10.0
                    )
                val condition = QibCondition.IS_ON_GROUND
                val conditional = QibEffect.Conditional(condition, false, velocity)
                val qibJump: ArrayList<QibEffect> = ArrayList()
                qibJump.add(sound)
                qibJump.add(conditional)
                val JumpoLogic = QibEffect.Multiple(qibJump)
                val qibDefinitionJump = QibDefinition(null, null, null, JumpoLogic, null, false)

                NoxesiumRegistries.QIB_EFFECTS.register(Key.key("jump_pad"), qibDefinitionJump)

                val entity = Bukkit.getEntity(UUID.fromString("db4c8b0b-a7cf-4e8f-b78e-109b82a28e17"))
                entity?.setNoxesiumComponent(CommonEntityComponentTypes.QIB_BEHAVIOR, Key.key("bounce_pad"))
                val playerMods = event.player.noxesiumPlayer?.mods
                if (playerMods != null) {
                    NoxesiumApi.getLogger().info(playerMods.toString())
                } else {
                    NoxesiumApi.getLogger().info("No mods found for ${event.player.name}")
                }
                val op = Optional.of("jump_pad")
                val noxplayer = event.player.noxesiumPlayer
                if (noxplayer == null) {
                    Bukkit.getLogger().info { ("NoxPlayer is null!") }
                    return@Runnable
                }
                Bukkit.getLogger().info { ("sending packets") }
                noxplayer
                    .sendPacket(
                        ClientboundKeybindAddPacket(
                            "key.noxesium.customkeybind0", true, true, Optional.of(500),
                            Optional
                                .of("bounce_pad")
                        )
                    )
                noxplayer
                    .sendPacket(
                        ClientboundKeybindAddPacket(
                            "key.noxesium.customkeybind1", false, true, Optional.of(500),
                            Optional
                                .of("")
                        )
                    )
                noxplayer.sendPacket(
                    ClientboundKeybindAddPacket(
                        "key.jump",
                        true,
                        true,
                        Optional.of(500),
                        op
                    )
                )
                noxplayer.sendPacket(
                    ClientboundKeybindAddPacket(
                        "key.forward",
                        false,
                        true,
                        Optional.of(0),
                        Optional
                            .of("")
                    )
                )
            },
            100
        )
    }

    @EventHandler
    public fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            val clickedBlock: Block? = event.clickedBlock
            if (clickedBlock != null) {
                val blockType = clickedBlock.type

                // Prevent trapdoor interaction
                if (blockType.name.endsWith("_TRAPDOOR") && blockType != Material.IRON_TRAPDOOR) {
                    event.setCancelled(true)
                }
            }
        }
    }
}
