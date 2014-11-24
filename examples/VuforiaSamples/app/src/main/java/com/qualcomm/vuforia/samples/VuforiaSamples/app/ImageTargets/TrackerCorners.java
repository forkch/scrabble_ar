package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import com.qualcomm.vuforia.Vec2F;

/**
 * Created with love by fork on 24.11.14.
 */
public class TrackerCorners {
    final Vec2F upperLeft;
    final Vec2F upperRight;
    final Vec2F lowerLeft;
    final Vec2F lowerRight;

    public TrackerCorners(Vec2F upperLeft, Vec2F upperRight, Vec2F lowerLeft, Vec2F lowerRight) {
        this.upperLeft = upperLeft;
        this.upperRight = upperRight;
        this.lowerLeft = lowerLeft;
        this.lowerRight = lowerRight;
    }

    public Vec2F getUpperLeft() {
        return upperLeft;
    }

    public Vec2F getUpperRight() {
        return upperRight;
    }

    public Vec2F getLowerLeft() {
        return lowerLeft;
    }

    public Vec2F getLowerRight() {
        return lowerRight;
    }

}
