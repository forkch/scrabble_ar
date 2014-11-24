package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.Vec4F;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;

/**
 * Created with love by fork on 24.11.14.
 */
public class VectorUtils {

    public static  String vecToString(Vec2F vec) {
        return "[" + vec.getData()[0] + "," + vec.getData()[1] + "]";
    }

    public static  String vecToString(Vec3F vec) {
        return "[" + vec.getData()[0] + "," + vec.getData()[1] + "," + vec.getData()[2] + "]";
    }

    public static  String vecToString(Vec4F vec) {
        return "[" + vec.getData()[0] + "," + vec.getData()[1] + "," + vec.getData()[2] + "," + vec.getData()[3] + "]";
    }

    public static Vec2F cameraPointToScreenPoint(Vec2F cameraPoint) {

        VideoMode videoMode = CameraDevice.getInstance().getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        VideoBackgroundConfig config = Renderer.
                getInstance().getVideoBackgroundConfig();
        int xOffset = 0;//((int) screenWidth - config.getSize().getData()[0]) / 2.0f + config.getPosition().getData()[0];
        int yOffset = 0;//((int) screenHeight - config.getSize().getData()[1]) / 2.0f - config.getPosition().getData()[1];

        boolean isActivityInPortraitMode = false;
        if (isActivityInPortraitMode) {
            // camera image is rotated 90 degrees
            float rotatedX = videoMode.getHeight() - cameraPoint.getData()[1];
            float rotatedY = cameraPoint.getData()[0];
            return new Vec2F(rotatedX * config.getSize().getData()[0] / (float) videoMode.getHeight() + xOffset,
                    rotatedY * config.getSize().getData()[1] / (float) videoMode.getWidth() + yOffset);
        } else {
            return new Vec2F(cameraPoint.getData()[0] * config.getSize().getData()[0] / (float) videoMode.getWidth() + xOffset,
                    cameraPoint.getData()[1] * config.getSize().getData()[1] / (float) videoMode.getHeight() + yOffset);
        }

    }
}
