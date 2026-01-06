package com.noxcrew.noxesium.showdium.pingsystem;

import com.noxcrew.noxesium.showdium.ShowdiumEntrypoint;
import net.minecraft.world.phys.Vec2;

/**
 * Handles rendering of off-screen ping indicators.
 * Shows arrows at screen edges pointing toward pings that are not visible.
 */
public final class OffscreenIndicatorRenderer {

    private static final int EDGE_PADDING = 20;
    private static final int BOTTOM_PADDING = 60;
    private static final float BASE_ARROW_SCALE = 0.25f;
    private static final float ARROW_OFFSET = -5f;

    private static Vec2 screenDimensions;
    private static Vec2 safeAreaTopLeft;
    private static Vec2 safeAreaBottomRight;
    private static Vec2 safeAreaCenter;

    private OffscreenIndicatorRenderer() {
        // Prevent instantiation
    }

    /**
     * Initializes the safe zone boundaries for indicator placement.
     * Call this once per frame before rendering indicators.
     */
    public static void initializeSafeZone() {
        var window = ShowdiumEntrypoint.GAME.getWindow();

        float width = window.getGuiScaledWidth();
        float height = window.getGuiScaledHeight();

        screenDimensions = new Vec2(width, height);
        safeAreaTopLeft = new Vec2(EDGE_PADDING, EDGE_PADDING);
        safeAreaBottomRight = new Vec2(width - EDGE_PADDING, height - BOTTOM_PADDING);

        float centerX = (safeAreaBottomRight.x - safeAreaTopLeft.x) * 0.5f + safeAreaTopLeft.x;
        float centerY = (safeAreaBottomRight.y - safeAreaTopLeft.y) * 0.5f + safeAreaTopLeft.y;
        safeAreaCenter = new Vec2(centerX, centerY);
    }

    /**
     * Renders an off-screen indicator for a ping.
     */
    public static void renderIndicator(PingRenderHelper renderer, ScreenPosition screenPos, PingEntry ping) {
        if (screenPos == null) {
            return;
        }

        // Check if indicator is needed
        boolean isBehindCamera = screenPos.isBehindCamera();
        boolean isOffScreen = isBehindCamera || !screenPos.isWithinBounds(Vec2.ZERO, screenDimensions);

        if (!isOffScreen) {
            return;
        }

        // Calculate direction to ping
        float dirX = screenPos.getX() - safeAreaCenter.x;
        float dirY = screenPos.getY() - safeAreaCenter.y;

        // Flip direction if behind camera
        if (isBehindCamera) {
            dirX = -dirX;
            dirY = -dirY;
        }

        // Calculate angle and edge position
        float angle = (float) Math.atan2(dirY, dirX);
        Vec2 edgePosition = ProjectionHelper.findEdgeIntersection(angle, safeAreaTopLeft, safeAreaBottomRight);

        int indicatorColor = ping.getDisplayColor();

        // Render the arrow indicator
        var matrices = renderer.getMatrixStack();

        matrices.pushMatrix();
        matrices.translate(edgePosition.x, edgePosition.y);

        matrices.pushMatrix();
        ProjectionHelper.applyRotationZ(matrices, angle);
        matrices.scale(BASE_ARROW_SCALE, BASE_ARROW_SCALE);
        matrices.translate(ARROW_OFFSET, 0f);
        // Note: renderDirectionalArrow now applies config scale internally
        renderer.renderDirectionalArrow(indicatorColor);
        matrices.popMatrix();

        matrices.popMatrix();
    }

    /**
     * Gets the current screen dimensions.
     */
    public static Vec2 getScreenDimensions() {
        return screenDimensions;
    }
}
