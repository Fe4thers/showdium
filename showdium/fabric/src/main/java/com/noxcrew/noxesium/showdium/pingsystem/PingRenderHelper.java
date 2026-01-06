package com.noxcrew.noxesium.showdium.pingsystem;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.noxcrew.noxesium.showdium.ShowdiumEntrypoint;
import com.noxcrew.noxesium.showdium.config.PingSystemConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

/**
 * Helper class for rendering ping-related UI elements.
 */
public class PingRenderHelper {

    private static final int LABEL_BACKGROUND_COLOR = ARGB.color(64, 0, 0, 0);
    private static final int ARROW_ICON_SIZE = 75;
    private static final int PING_ICON_SIZE = 16;
    private static final float PING_ICON_SCALE = 3.5f;
    private static final float HEAD_ICON_SCALE = 2.0f;

    private final GuiGraphics graphics;
    private final Matrix3x2fStack matrixStack;

    public PingRenderHelper(GuiGraphics graphics) {
        this.graphics = graphics;
        this.matrixStack = graphics.pose();
    }

    public Matrix3x2fStack getMatrixStack() {
        return matrixStack;
    }

    /**
     * Renders a text label with background.
     */
    public void renderTextLabel(Component text, float yOffset, int textColor) {
        var font = ShowdiumEntrypoint.GAME.font;

        float textWidth = font.width(text);
        float textHeight = font.lineHeight;

        float offsetX = -textWidth * 0.5f;
        float offsetY = textHeight * yOffset;

        matrixStack.pushMatrix();
        matrixStack.translate(offsetX, offsetY);

        // Background
        graphics.fill(-2, -2, (int) textWidth + 1, (int) textHeight, LABEL_BACKGROUND_COLOR);

        // Text
        graphics.drawString(font, text, 0, 0, textColor, false);

        matrixStack.popMatrix();
    }

    /**
     * Renders a player head texture.
     */
    public void renderPlayerHead(PlayerInfo player) {
        if (player == null) {
            return;
        }

        Identifier skinTexture = player.getSkin().body().texturePath();

        GlStateManager._enableBlend();

        // Base head layer (8x8 from texture)
        graphics.blit(RenderPipelines.GUI_TEXTURED, skinTexture, -4, -4, 8, 8, 8, 8, 64, 64);

        // Hat overlay layer
        graphics.blit(RenderPipelines.GUI_TEXTURED, skinTexture, -4, -4, 40, 8, 8, 8, 64, 64);

        GlStateManager._disableBlend();
    }

    /**
     * Renders a texture at the current position.
     */
    public void renderTexture(Identifier texture, int size, int color) {
        int offset = -size / 2;

        GlStateManager._enableBlend();
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, offset, offset, 0, 0, size, size, size, size, color);
        GlStateManager._disableBlend();
    }

    /**
     * Renders the directional arrow icon with config scale applied.
     */
    public void renderDirectionalArrow(int color) {
        float configScale = PingSystemConfig.getPingScale();
        matrixStack.pushMatrix();
        matrixStack.scale(configScale, configScale);
        renderTexture(PingResources.ARROW_ICON_TEXTURE, ARROW_ICON_SIZE, color);
        matrixStack.popMatrix();
    }

    /**
     *
     * Renders the default ping icon (simple square).
     */
    public void renderPingIcon(int color) {
        matrixStack.pushMatrix();
        matrixStack.translate(-3f, -3f);
        graphics.fill(0, 0, 6, 6, color);
        matrixStack.popMatrix();
    }

    /**
     * Renders a complete ping marker with player head.
     * Applies the config scale setting.
     */
    public void renderFullPingMarker(int pingColor, PlayerInfo creator) {
        float configScale = PingSystemConfig.getPingScale();

        // Main ping icon
        matrixStack.pushMatrix();
        float totalPingScale = PING_ICON_SCALE * configScale;
        matrixStack.scale(totalPingScale, totalPingScale);
        renderPingIcon(pingColor);
        matrixStack.popMatrix();

        // Player head overlay
        if (creator != null) {
            matrixStack.pushMatrix();
            float totalHeadScale = HEAD_ICON_SCALE * configScale;
            matrixStack.scale(totalHeadScale, totalHeadScale);
            renderPlayerHead(creator);
            matrixStack.popMatrix();
        }
    }
}
