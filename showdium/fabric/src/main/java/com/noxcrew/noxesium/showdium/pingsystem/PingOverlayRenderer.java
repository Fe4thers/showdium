package com.noxcrew.noxesium.showdium.pingsystem;

import com.noxcrew.noxesium.showdium.config.PingSystemConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public final class PingOverlayRenderer {

    private static final float TEXT_SCALE_FACTOR = 1.0f;
    private static final float TEXT_Y_OFFSET = 2.0f;

    private PingOverlayRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        if (!PingSystemConfig.isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) {
            return;
        }

        if (!RenderContext.hasValidData()) {
            return;
        }

        var pings = PingManager.getActivePings();

        if (pings.isEmpty()) {
            return;
        }

        PingRenderHelper renderer = new PingRenderHelper(graphics);
        OffscreenIndicatorRenderer.initializeSafeZone();

        Matrix4f modelView = RenderContext.getModelViewMatrix();
        Matrix4f projection = RenderContext.getProjectionMatrix();

        if (projection == null) {
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        for (PingEntry ping : pings) {
            ScreenPosition screenPos = ProjectionHelper.projectToScreen(ping.getPosition(), modelView, projection);

            ping.setScreenPosition(screenPos);
            ping.setDistanceToPlayer(mc.player.position().distanceTo(ping.getPosition()));
            ping.updateState((int) mc.level.getGameTime());

            if (screenPos == null) {
                continue;
            }

            if (!screenPos.isVisibleOnScreen(screenWidth, screenHeight)) {
                OffscreenIndicatorRenderer.renderIndicator(renderer, screenPos, ping);
            } else {
                renderOnscreenPing(renderer, ping, screenPos);
            }
        }
    }

    private static void renderOnscreenPing(PingRenderHelper renderer, PingEntry ping, ScreenPosition screenPos) {
        var matrices = renderer.getMatrixStack();

        matrices.pushMatrix();
        matrices.translate(screenPos.getX(), screenPos.getY());

        float pingScale = ping.getRenderScale();

        matrices.pushMatrix();
        matrices.scale(pingScale, pingScale);
        renderer.renderFullPingMarker(ping.getDisplayColor(), ping.getCreatorInfo());
        matrices.popMatrix();

        matrices.pushMatrix();
        float textScale = pingScale * TEXT_SCALE_FACTOR;
        matrices.scale(textScale, textScale);

        int distanceRounded = (int) Math.round(ping.getDistanceToPlayer());
        renderer.renderTextLabel(Component.literal(distanceRounded + "m"), TEXT_Y_OFFSET, 0xFFFFFFFF);

        matrices.popMatrix();
        matrices.popMatrix();
    }
}
