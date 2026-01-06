package com.noxcrew.noxesium.showdium.pingsystem;

import com.noxcrew.noxesium.showdium.ShowdiumEntrypoint;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Utility class for mathematical operations related to the ping system.
 * Handles world-to-screen projection and geometric calculations.
 */
public final class ProjectionHelper {

    private ProjectionHelper() {
        // Prevent instantiation
    }

    /**
     * Projects a 3D world position to 2D screen coordinates.
     */
    public static ScreenPosition projectToScreen(Vec3 worldPosition, Matrix4f modelView, Matrix4f projection) {
        var window = ShowdiumEntrypoint.GAME.getWindow();
        var camera = ShowdiumEntrypoint.GAME.gameRenderer.getMainCamera();

        // Transform world position relative to camera
        Vec3 cameraPos = camera.position();
        Vec3 relativePos = worldPosition.subtract(cameraPos.x, cameraPos.y, cameraPos.z);

        Vector4f clipSpacePos = new Vector4f((float) relativePos.x, (float) relativePos.y, (float) relativePos.z, 1.0f);

        // Apply transformations
        clipSpacePos.mul(modelView);
        clipSpacePos.mul(projection);

        float depth = clipSpacePos.w;

        // Perspective division
        if (depth != 0) {
            clipSpacePos.div(depth);
        }

        // Convert to screen coordinates
        float screenX = window.getGuiScaledWidth() * (0.5f + clipSpacePos.x * 0.5f);
        float screenY = window.getGuiScaledHeight() * (0.5f - clipSpacePos.y * 0.5f);

        return new ScreenPosition(screenX, screenY, depth);
    }

    /**
     * Applies a Z-axis rotation to a matrix.
     */
    public static void applyRotationZ(Matrix3x2f matrix, float angleRadians) {
        Matrix3x2f rotationMatrix = new Matrix3x2f().rotateLocal(angleRadians);
        matrix.mul(rotationMatrix);
    }

    /**
     * Calculates where a line from center at given angle intersects a rectangle.
     * Used for positioning off-screen indicators at screen edges.
     */
    public static Vec2 findEdgeIntersection(float angle, Vec2 topLeft, Vec2 bottomRight) {
        float width = bottomRight.x - topLeft.x;
        float height = bottomRight.y - topLeft.y;

        float dirX = (float) Math.cos(angle);
        float dirY = (float) Math.sin(angle);

        // Normalize direction by rectangle dimensions
        float normalizedX = dirX / width;
        float normalizedY = dirY / height;

        float centerX = width * 0.5f;
        float centerY = height * 0.5f;

        // Determine which edge the line intersects
        if (Math.abs(normalizedX) < Math.abs(normalizedY)) {
            // Intersects top or bottom edge
            if (normalizedY < 0) {
                // Top edge
                float t = -centerY / dirY;
                float x = centerX + t * dirX;
                return new Vec2(x + topLeft.x, topLeft.y);
            } else {
                // Bottom edge
                float t = centerY / dirY;
                float x = centerX + t * dirX;
                return new Vec2(x + topLeft.x, bottomRight.y);
            }
        } else {
            // Intersects left or right edge
            if (normalizedX < 0) {
                // Left edge
                float t = -centerX / dirX;
                float y = centerY + t * dirY;
                return new Vec2(topLeft.x, y + topLeft.y);
            } else {
                // Right edge
                float t = centerX / dirX;
                float y = centerY + t * dirY;
                return new Vec2(bottomRight.x, y + topLeft.y);
            }
        }
    }
}
