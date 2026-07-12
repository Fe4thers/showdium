package com.noxcrew.noxesium.showdium.pingsystem;

import com.noxcrew.noxesium.showdium.ShowdiumEntrypoint;
import java.util.UUID;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single ping marker in the world.
 * Contains all rendering and state information for a ping.
 */
public class PingEntry extends PingData {

    private static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static final int EXPIRATION_TICKS = 30 * 20; // 30 seconds
    private static final float BASE_PING_SIZE = 1.0f;

    private int creationTime;
    private int ageInTicks;
    private float renderScale;

    private @Nullable PlayerInfo creatorInfo;
    private ScreenPosition screenPosition;
    private double distanceToPlayer;
    private @Nullable Integer customColor;

    private PingEntry(
            Vec3 position,
            @Nullable UUID targetEntityId,
            @Nullable UUID creatorId,
            int sequenceNumber,
            int dimensionId) {
        super(position, targetEntityId, creatorId, sequenceNumber, dimensionId);
    }

    /**
     * Factory method to create a new ping entry.
     */
    public static PingEntry create(
            Vec3 position,
            @Nullable UUID targetEntityId,
            @Nullable UUID creatorId,
            int sequenceNumber,
            int dimensionId) {
        return new PingEntry(position, targetEntityId, creatorId, sequenceNumber, dimensionId);
    }

    /**
     * Updates the ping state for the current frame.
     */
    public void updateState(int currentGameTime) {
        if (creationTime == 0) {
            creationTime = currentGameTime;
        }
        ageInTicks = currentGameTime - creationTime;

        // Update creator info
        var connection = ShowdiumEntrypoint.GAME.getConnection();
        if (connection != null && creatorId != null) {
            creatorInfo = connection.getPlayerInfo(creatorId);
        }

        calculateRenderScale();
    }

    /**
     * Calculates the render scale based on distance.
     */
    private void calculateRenderScale() {
        double distanceFactor = 2.0 / Math.pow(Math.max(1.0, distanceToPlayer), 0.3);
        renderScale = (float) Math.max(1.0, distanceFactor) * 0.5f * BASE_PING_SIZE;
    }

    /**
     * Checks if this ping has expired.
     */
    public boolean hasExpired() {
        return ageInTicks > EXPIRATION_TICKS;
    }

    /**
     * Gets the distance from this ping to the screen center.
     */
    public float getDistanceToScreenCenter() {
        if (screenPosition == null) {
            return 0f;
        }

        var window = ShowdiumEntrypoint.GAME.getWindow();
        Vec2 screenCenter = new Vec2(window.getGuiScaledWidth() * 0.5f, window.getGuiScaledHeight() * 0.5f);

        return screenPosition.distanceTo(screenCenter);
    }

    /**
     * Gets the display color for this ping.
     */
    public int getDisplayColor() {
        if (customColor != null) {
            return customColor;
        }
        return getTeamColor();
    }

    /**
     * Gets the team color of the ping creator.
     */
    public int getTeamColor() {
        if (creatorInfo == null || creatorInfo.getTeam() == null) {
            return DEFAULT_COLOR;
        }

        Integer teamColor = creatorInfo.getTeam().getColor().get().rgb();
        if (teamColor == null) {
            return DEFAULT_COLOR;
        }

        return (255 << 24) | teamColor;
    }

    // Getters and setters

    public @Nullable PlayerInfo getCreatorInfo() {
        return creatorInfo;
    }

    public ScreenPosition getScreenPosition() {
        return screenPosition;
    }

    public void setScreenPosition(ScreenPosition screenPosition) {
        this.screenPosition = screenPosition;
    }

    public double getDistanceToPlayer() {
        return distanceToPlayer;
    }

    public void setDistanceToPlayer(double distance) {
        this.distanceToPlayer = distance;
    }

    public float getRenderScale() {
        return renderScale;
    }

    public @Nullable Integer getCustomColor() {
        return customColor;
    }

    public void setCustomColor(@Nullable Integer color) {
        this.customColor = color;
    }

    public int getAge() {
        return ageInTicks;
    }
}
