package vn.phuc.pscore.auth;

import java.util.Objects;

public final class PlayerLocationSnapshot {

    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public PlayerLocationSnapshot(
            String worldName,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
        this.worldName = Objects.requireNonNull(worldName);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
