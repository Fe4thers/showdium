package com.noxcrew.noxesium.showdium.pingsystem;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;

public final class RenderContext {

    private static Matrix4f storedModelView;
    private static Matrix4f storedProjection;
    private static CameraRenderState storedCamera;
    private static float storedTickDelta;

    private RenderContext() {}

    public static void captureMatrices(
            Matrix4f modelView, Matrix4f projection, float tickDelta, CameraRenderState camera) {
        storedModelView = new Matrix4f(modelView);
        storedProjection = projection != null ? new Matrix4f(projection) : null;
        storedCamera = camera;
        storedTickDelta = tickDelta;
    }

    public static void clear() {
        storedModelView = null;
        storedProjection = null;
        storedCamera = null;
        storedTickDelta = 0f;
    }

    public static Matrix4f getModelViewMatrix() {
        return storedModelView;
    }

    public static Matrix4f getProjectionMatrix() {
        return storedProjection;
    }

    public static CameraRenderState getCamera() {
        return storedCamera;
    }

    public static float getTickDelta() {
        return storedTickDelta;
    }

    public static boolean hasValidData() {
        return storedModelView != null && storedCamera != null;
    }
}
