package com.noxcrew.noxesium.showdium.pingsystem;

import com.noxcrew.noxesium.showdium.ShowdiumEntrypoint;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Handles raycasting operations for the ping system.
 * Used to determine where the player is looking.
 */
public final class RaycastHelper {

    private static final double DEFAULT_MAX_DISTANCE = 1000.0;

    private RaycastHelper() {
        // Prevent instantiation
    }

    /**
     * Performs a raycast in the specified direction from the camera.
     */
    public static HitResult performRaycast(
            Vec3 direction, float tickDelta, double maxDistance, boolean includeTranslucent) {
        var cameraEntity = ShowdiumEntrypoint.GAME.getCameraEntity();

        if (cameraEntity == null || cameraEntity.level() == null) {
            return null;
        }

        Vec3 startPosition = cameraEntity.getEyePosition(tickDelta);
        Vec3 endPosition = startPosition.add(direction.scale(maxDistance));

        // Create bounding box for entity detection
        AABB searchBox = cameraEntity
                .getBoundingBox()
                .expandTowards(cameraEntity.getViewVector(1.0f).scale(maxDistance))
                .inflate(1.0, 1.0, 1.0);

        // Perform block raycast
        ClipContext.Block blockMode = includeTranslucent ? ClipContext.Block.OUTLINE : ClipContext.Block.VISUAL;
        ClipContext.Fluid fluidMode = includeTranslucent ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;

        ClipContext clipContext = new ClipContext(startPosition, endPosition, blockMode, fluidMode, cameraEntity);
        HitResult blockHit = cameraEntity.level().clip(clipContext);

        // Perform entity raycast
        EntityHitResult entityHit =
                findEntityHit(cameraEntity, startPosition, endPosition, searchBox, entity -> !entity.isSpectator());

        // Return closest hit
        if (entityHit == null) {
            return blockHit;
        }

        double blockDistance = startPosition.distanceToSqr(blockHit.getLocation());
        double entityDistance = startPosition.distanceToSqr(entityHit.getLocation());

        return blockDistance < entityDistance ? blockHit : entityHit;
    }

    /**
     * Performs a raycast with default settings.
     */
    public static HitResult performRaycast(Vec3 direction) {
        return performRaycast(direction, 0f, DEFAULT_MAX_DISTANCE, true);
    }

    /**
     * Finds the closest entity hit within the search area.
     */
    private static EntityHitResult findEntityHit(
            Entity sourceEntity, Vec3 start, Vec3 end, AABB searchBox, Predicate<Entity> filter) {
        double closestDistance = start.distanceToSqr(end);
        EntityHitResult closestHit = null;

        for (Entity entity : sourceEntity.level().getEntities(sourceEntity, searchBox, filter)) {
            AABB entityBox =
                    entity.getBoundingBox().inflate(entity.getPickRadius()).inflate(0.25);

            var intersection = entityBox.clip(start, end);

            if (intersection.isEmpty()) {
                continue;
            }

            EntityHitResult hit = new EntityHitResult(entity, intersection.get());
            double distance = start.distanceToSqr(hit.getLocation());

            if (distance < closestDistance) {
                closestDistance = distance;
                closestHit = hit;
            }
        }

        return closestHit;
    }
}
