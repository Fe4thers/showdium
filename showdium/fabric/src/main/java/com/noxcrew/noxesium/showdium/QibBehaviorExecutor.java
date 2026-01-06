package com.noxcrew.noxesium.showdium;

import static com.noxcrew.noxesium.api.feature.qib.QibCondition.IS_ON_GROUND;

import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.feature.qib.QibEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

public class QibBehaviorExecutor extends NoxesiumFeature {
    /**
     * Executes the given behavior.
     */
    protected final List<Pair<AtomicInteger, Pair<Player, QibEffect>>> pending = new ArrayList<>();

    private Boolean PlayerOffGroundDoOnce = false;
    private Boolean WasPlayerOnGround = false;
    public static Boolean ResetCooldown = false;

    public QibBehaviorExecutor() {
        ClientTickEvents.END_CLIENT_TICK.register((ignored) -> {
            tickEffects();
        });
    }

    /**
     * Ticks down scheduled effects and runs them.
     */
    private void tickEffects() {
        // Increment all timers
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (!player.onGround() && WasPlayerOnGround) {
                PlayerOffGroundDoOnce = true;
                WasPlayerOnGround = false;
            } else if (player.onGround()) {
                PlayerOffGroundDoOnce = false;
                WasPlayerOnGround = true;
            }
        }

        pending.forEach(pair -> pair.getKey().decrementAndGet());
        var iterator = pending.iterator();
        while (iterator.hasNext()) {
            var pair = iterator.next();
            if (pair.getKey().decrementAndGet() <= 0) {
                var value = pair.getValue();
                iterator.remove();
                executeBehavior(value.getLeft(), value.getRight());
            }
        }
    }

    protected void executeBehavior(Player player, QibEffect effect) {
        switch (effect) {
            case QibEffect.Multiple multiple -> {
                for (var nested : multiple.effects()) {
                    executeBehavior(player, nested);
                }
            }
            case QibEffect.Wait wait -> {
                pending.add(Pair.of(new AtomicInteger(wait.ticks()), Pair.of(player, wait.effect())));
            }
            case QibEffect.Conditional conditional -> {
                boolean result =
                        switch (conditional.condition()) {
                            case IS_GLIDING -> player.isFallFlying();
                            case IS_RIPTIDING -> player.isAutoSpinAttack();
                            case IS_IN_AIR -> !player.onGround();
                            case IS_ON_GROUND -> player.onGround();
                            case IS_IN_WATER -> player.isInWater();
                            case IS_IN_WATER_OR_RAIN -> player.isInWaterOrRain();
                            case IS_IN_VEHICLE -> player.isPassenger();
                        };
                if (conditional.condition() == IS_ON_GROUND && !conditional.value()) {
                    // logic to skip the jump of the ground
                    if (PlayerOffGroundDoOnce) {
                        PlayerOffGroundDoOnce = false;
                        result = true;
                        ResetCooldown = true;
                    }
                }
                // Trigger the effect if it matches
                if (result == conditional.value()) {
                    executeBehavior(player, conditional.effect());
                }
            }
            case QibEffect.Move move -> {
                player.move(MoverType.SELF, new Vec3(move.x(), move.y(), move.z()));
            }
            case QibEffect.SetVelocity setVelocity -> {
                player.setDeltaMovement(setVelocity.x(), setVelocity.y(), setVelocity.z());
            }
            case QibEffect.SetVelocityYawPitch setVelocityYawPitch -> {
                var yawRad = Math.toRadians(
                        setVelocityYawPitch.yaw() + (setVelocityYawPitch.yawRelative() ? player.yRotO : 0));
                var pitchRad = Math.toRadians(
                        setVelocityYawPitch.pitch() + (setVelocityYawPitch.pitchRelative() ? player.xRotO : 0));

                var x = -Math.cos(pitchRad) * Math.sin(yawRad);
                var y = -Math.sin(pitchRad);
                var z = Math.cos(pitchRad) * Math.cos(yawRad);
                player.setDeltaMovement(
                        Math.clamp(
                                x * setVelocityYawPitch.strength(),
                                -setVelocityYawPitch.limit(),
                                setVelocityYawPitch.limit()),
                        Math.clamp(
                                y * setVelocityYawPitch.strength(),
                                -setVelocityYawPitch.limit(),
                                setVelocityYawPitch.limit()),
                        Math.clamp(
                                z * setVelocityYawPitch.strength(),
                                -setVelocityYawPitch.limit(),
                                setVelocityYawPitch.limit()));
            }
            case QibEffect.ModifyVelocity modifyVelocity -> {
                var current = player.getDeltaMovement();
                player.setDeltaMovement(
                        modifyVelocity.xOp().apply(current.x, modifyVelocity.x()),
                        modifyVelocity.yOp().apply(current.y, modifyVelocity.y()),
                        modifyVelocity.zOp().apply(current.z, modifyVelocity.z()));
                player.hurtMarked = true;
            }
            case QibEffect.PlaySound playSound -> {
                player.level()
                        .playLocalSound(
                                player,
                                SoundEvent.createVariableRangeEvent(
                                        Identifier.fromNamespaceAndPath(playSound.namespace(), playSound.path())),
                                SoundSource.PLAYERS,
                                playSound.volume(),
                                playSound.pitch());
            }
            case QibEffect.GivePotionEffect giveEffect -> {
                var type = BuiltInRegistries.MOB_EFFECT
                        .get(Identifier.fromNamespaceAndPath(giveEffect.namespace(), giveEffect.path()))
                        .orElse(null);
                player.noxesium$addClientsidePotionEffect(new MobEffectInstance(
                        type,
                        giveEffect.duration(),
                        giveEffect.amplifier(),
                        giveEffect.ambient(),
                        giveEffect.visible(),
                        giveEffect.showIcon()));
            }
            case QibEffect.RemovePotionEffect removeEffect -> {
                player.noxesium$removeClientsidePotionEffect(BuiltInRegistries.MOB_EFFECT
                        .get(Identifier.fromNamespaceAndPath(removeEffect.namespace(), removeEffect.path()))
                        .orElse(null));
            }
            case QibEffect.RemoveAllPotionEffects ignored -> {
                player.noxesium$clearClientsidePotionEffects();
            }
            case QibEffect.AddVelocity addVelocity -> {
                player.push(addVelocity.x(), addVelocity.y(), addVelocity.z());
            }
            default -> throw new IllegalStateException("Unexpected value: " + effect);
        }
    }
}
