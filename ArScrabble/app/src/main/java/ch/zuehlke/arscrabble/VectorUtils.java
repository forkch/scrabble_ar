package ch.zuehlke.arscrabble;

import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Frame;
import com.qualcomm.vuforia.Image;
import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.Matrix34F;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.Vec4F;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created with love by fork on 24.11.14.
 */
public class VectorUtils {
    private static final String LOGTAG = VectorUtils.class.getSimpleName();

    public static String vecToString(Vec2F vec) {
        return "[" + vec.getData()[0] + "," + vec.getData()[1] + "]";
    }

    public static String vecToString(Vec3F vec) {
        return "[" + vec.getData()[0] + "," + vec.getData()[1] + "," + vec.getData()[2] + "]";
    }

    public static String vecToString(Vec4F vec) {
        return "[" + vec.getData()[0] + "," + vec.getData()[1] + "," + vec.getData()[2] + "," + vec.getData()[3] + "]";
    }


    public static Point vecToPoint(Vec2F vec) {
        return new Point(vec.getData()[0], vec.getData()[1]);
    }


    public static TrackerCorners calcCorners(State state, TrackableResult result, View imageView, boolean isLandscape) {

        Trackable trackable = result.getTrackable();

        final Matrix34F pose = result.getPose();
        ImageTarget imageTarget = (ImageTarget) trackable;

        final Vec2F imageTargetSize = imageTarget.getSize();
        float width = imageTargetSize.getData()[0];
        float height = imageTargetSize.getData()[1];
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        float viewWidth = imageView.getHeight();
        float viewHeight = imageView.getWidth();

        if (isLandscape) {
            viewWidth = imageView.getWidth();
            viewHeight = imageView.getHeight();
        }

        VideoMode videoMode = CameraDevice.getInstance().getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        VideoBackgroundConfig config = Renderer.
                getInstance().getVideoBackgroundConfig();

        final CameraCalibration cameraCalibration = CameraDevice.getInstance().getCameraCalibration();
        float scale = viewWidth / videoMode.getWidth();
        if (videoMode.getHeight() * scale < viewHeight) {
            scale = viewHeight / videoMode.getHeight();
        }
        float scaledWidth = videoMode.getWidth() * scale;
        float scaledHeight = videoMode.getHeight() * scale;
        Vec2F margin = new Vec2F((scaledWidth - viewWidth) / 2, (scaledHeight - viewHeight) / 2);

        Vec3F upperLeft = new Vec3F(-halfWidth, halfHeight, 0.f);
        Vec3F upperRight = new Vec3F(halfWidth, halfHeight, 0.f);
        Vec3F lowerLeft = new Vec3F(-halfWidth, -halfHeight, 0.f);
        Vec3F lowerRight = new Vec3F(halfWidth, -halfHeight, 0.f);
        Vec3F center = new Vec3F(0, 0, 0);

        Vec2F upperLeftImageSpace = project(upperLeft, cameraCalibration, pose, margin, scale);
        Vec2F upperRightImageSpace = project(upperRight, cameraCalibration, pose, margin, scale);
        Vec2F lowerLeftImageSpace = project(lowerLeft, cameraCalibration, pose, margin, scale);
        Vec2F lowerRightImageSpace = project(lowerRight, cameraCalibration, pose, margin, scale);
        Vec2F centerImageSpace = project(center, cameraCalibration, pose, margin, scale);


        return new TrackerCorners(centerImageSpace, upperLeftImageSpace, upperRightImageSpace, lowerLeftImageSpace, lowerRightImageSpace);
    }

    private static Vec2F project(Vec3F coord, CameraCalibration cameraCalibration, Matrix34F pose, Vec2F margin, float scale) {

        final Vec2F vec2F = Tool.projectPoint(cameraCalibration, pose, coord);

        final float x = vec2F.getData()[0] * scale - margin.getData()[0];
        final float y = vec2F.getData()[1] * scale - margin.getData()[1];

        return new Vec2F(x, y);
    }

    public static Vec2F cameraPointToScreenPoint(Vec2F cameraPoint, int screenWidth, int screenHeight) {

        VideoMode videoMode = CameraDevice.getInstance().getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        VideoBackgroundConfig config = Renderer.
                getInstance().getVideoBackgroundConfig();
        int xOffset = (int) ((screenWidth - config.getSize().getData()[0]) / 2.0f + config.getPosition().getData()[0]);
        int yOffset = (int) ((screenHeight - config.getSize().getData()[1]) / 2.0f - config.getPosition().getData()[1]);

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

    private TrackerCorners getCornerCoordinatesInScreenSpace(State state, TrackableResult result, View imageView) {

        Trackable trackable = result.getTrackable();

        final Matrix34F pose = result.getPose();
        Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(pose);
        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

        float targetCenter_X = modelViewMatrix[12];
        float targetCenter_Y = modelViewMatrix[13];
        float targetCenter_Z = modelViewMatrix[14];

        ImageTarget imageTarget = (ImageTarget) trackable;

        final Frame frame = state.getFrame();
        final Image image = frame.getImage(0);

        final int imageWidth = image.getWidth();
        final int imageHeight = image.getHeight();
        Log.d(LOGTAG, "Image Size w=" + imageWidth + " h=" + imageHeight);

        final Vec2F imageTargetSize = imageTarget.getSize();
        float width = imageTargetSize.getData()[0];
        float height = imageTargetSize.getData()[1];
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;

        Log.d(LOGTAG, "Target Size: w=" + width + ", h=" + height);

        final Vec3F targetVec = new Vec3F(targetCenter_X, targetCenter_Y, targetCenter_Z);
        Log.d(LOGTAG, "Target Center:  " + vecToString(targetVec));

        Vec3F upperLeft = new Vec3F(-halfWidth, halfHeight, 0.f);
        Vec3F upperRight = new Vec3F(halfWidth, halfHeight, 0.f);
        Vec3F lowerLeft = new Vec3F(-halfWidth, -halfHeight, 0.f);
        Vec3F lowerRight = new Vec3F(halfWidth, -halfHeight, 0.f);
        Vec3F center = new Vec3F(0, 0, 0);

        Log.d(LOGTAG, "3D:  " + vecToString(upperLeft) + " " + vecToString(upperRight) + " " + vecToString(lowerLeft) + " " + vecToString(lowerRight));


        final CameraCalibration cameraCalibration = CameraDevice.getInstance().getCameraCalibration();
        Log.d(LOGTAG, "calibration: size=" + vecToString(cameraCalibration.getSize()) + " distortionParams=" + vecToString(cameraCalibration.getDistortionParameters()) + " focalLength=" + vecToString(cameraCalibration.getFocalLength()) + " principalPoint=" + vecToString(cameraCalibration.getPrincipalPoint()));
        final Vec2F upperLeftScreenSpace = Tool.projectPoint(cameraCalibration, pose, upperLeft);
        final Vec2F upperRightScreenSpace = Tool.projectPoint(cameraCalibration, pose, upperRight);
        final Vec2F lowerLeftScreenSpace = Tool.projectPoint(cameraCalibration, pose, lowerLeft);
        final Vec2F lowerRightScreenSpace = Tool.projectPoint(cameraCalibration, pose, lowerRight);
        final Vec2F centerScreenSpace = Tool.projectPoint(cameraCalibration, pose, center);

        //Log.d(LOGTAG, "2D: " + vecToString(upperLeftScreenSpace) + " " + vecToString(upperRightScreenSpace) + " " + vecToString(lowerLeftScreenSpace) + " " + vecToString(lowerRightScreenSpace));
        final Vec2F targetInScreenSpace = Tool.projectPoint(cameraCalibration, pose, targetVec);
        //Log.d(LOGTAG, "Target screen space: " + vecToString(targetInScreenSpace));


        final int viewWidth = imageView.getWidth();
        final int viewHeight = imageView.getHeight();

        final Vec2F upperLeftImageSpace = cameraPointToScreenPoint(upperLeftScreenSpace, viewWidth, viewHeight);
        final Vec2F upperRightImageSpace = cameraPointToScreenPoint(upperRightScreenSpace, viewWidth, viewHeight);
        final Vec2F lowerLeftImageSpace = cameraPointToScreenPoint(lowerLeftScreenSpace, viewWidth, viewHeight);
        final Vec2F lowerRightImageSpace = cameraPointToScreenPoint(lowerRightScreenSpace, viewWidth, viewHeight);
        final Vec2F centerImageSpace = cameraPointToScreenPoint(centerScreenSpace, viewWidth, viewHeight);


        VideoBackgroundConfig config = Renderer.
                getInstance().getVideoBackgroundConfig();
        //Log.d(LOGTAG, "VBC: " + config.getSize().getData()[0] + "x" + config.getSize().getData()[1]);
        Log.i(LOGTAG, "2D (image): " + vecToString(upperLeftImageSpace) + " " + vecToString(upperRightImageSpace) + " " + vecToString(lowerLeftImageSpace) + " " + vecToString(lowerRightImageSpace));
        return new TrackerCorners(centerImageSpace, upperLeftImageSpace, upperRightImageSpace, lowerLeftImageSpace, lowerRightImageSpace);
    }
}
