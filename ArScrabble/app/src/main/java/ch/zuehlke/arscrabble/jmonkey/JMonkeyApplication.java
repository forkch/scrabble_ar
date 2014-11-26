package ch.zuehlke.arscrabble.jmonkey;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Frame;
import com.qualcomm.vuforia.PIXEL_FORMAT;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;

import java.nio.ByteBuffer;

/**
 * Created by ssh on 25.11.2014.
 */
public class JMonkeyApplication extends SimpleApplication implements Vuforia.UpdateCallbackInterface {
    private Material backgroundCameraMaterial;
    private Texture2D backgroundCameraTexture;
    private Spatial backgroundCameraGeometry;
    private Image backgroundCameraImage;
    private ByteBuffer backgroundImageBuffer;
    private boolean hasBackgroundImage;

    @Override
    public void simpleInitApp() {
        // We use custom viewports - so the main viewport does not need to contain the rootNode
        viewPort.detachScene(rootNode);

        initBackground();

        CameraDevice cameraDevice = initCameraDevice();
        VideoMode videoMode = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_OPTIMIZE_SPEED);
        VideoBackgroundConfig config = initVideoBackgroundConfig(videoMode);
        Renderer.getInstance().setVideoBackgroundConfig(config);

        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB888, true);
        Vuforia.registerCallback(this);
    }

    private VideoBackgroundConfig initVideoBackgroundConfig(VideoMode videoMode) {
        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setSynchronous(true);
        config.setPosition(new Vec2I(0, 0));

        int screenWidth = settings.getWidth();
        int screenHeight = settings.getHeight();
        float imageWidth = (float) videoMode.getWidth();
        float imageHeight = (float) videoMode.getHeight();

        int width = screenWidth;
        int height = (int) (videoMode.getHeight() * (screenWidth / imageWidth));
        if (height < screenHeight) {
            width = (int) (screenHeight * (imageWidth / imageHeight));
            height = screenHeight;
        }
        config.setSize(new Vec2I(width, height));

        return config;
    }

    private CameraDevice initCameraDevice() {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        cameraDevice.init(CameraDevice.CAMERA.CAMERA_DEFAULT);
        CameraDevice.getInstance().start();
        return cameraDevice;
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (hasBackgroundImage) {
            backgroundCameraTexture.setImage(backgroundCameraImage);
            backgroundCameraMaterial.setTexture("ColorMap", backgroundCameraTexture);
        }

        // TODO: WTF? Why we need this method? Crash without...
        backgroundCameraGeometry.updateLogicalState(tpf);
        backgroundCameraGeometry.updateGeometricState();
    }

    private void initBackground() {
        int width = settings.getWidth();
        int height = settings.getHeight();

        Quad videoBGQuad = new Quad(1, 1, true);
        backgroundCameraGeometry = new Geometry("quad", videoBGQuad);

        float newWidth = 1.f * width / height;
        // Center the Geometry in the middle of the screen.
        backgroundCameraGeometry.setLocalTranslation(-0.5f * newWidth, -0.5f, 0.f);
        // Scale (stretch) the width of the Geometry to cover the whole screen width.
        backgroundCameraGeometry.setLocalScale(1.f * newWidth, 1.f, 1);

        // Apply a unshaded material which we will use for texturing.
        backgroundCameraMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        backgroundCameraGeometry.setMaterial(backgroundCameraMaterial);

        backgroundCameraTexture = new Texture2D();
        Camera backgroundCamera = createBackgroundCamera(width, height);

        ViewPort videoBackgroundViewPort = renderManager.createMainView("VideoBGView", backgroundCamera);
        videoBackgroundViewPort.attachScene(backgroundCameraGeometry);

        //videoBGVP.setClearFlags(true, false, false);
        //videoBGVP.setBackgroundColor(new ColorRGBA(1,0,0,1));
    }

    private Camera createBackgroundCamera(int width, int height) {
        // Create a custom virtual camera with orthographic projection
        Camera backgroundCamera = new Camera(width, height);
        backgroundCamera.setViewPort(0.0f, 1.0f, 0.f, 1.0f);
        backgroundCamera.setLocation(new Vector3f(0f, 0f, 1.f));
        backgroundCamera.setAxes(new Vector3f(-1f, 0f, 0f), new Vector3f(0f, 1f, 0f), new Vector3f(0f, 0f, -1f));
        backgroundCamera.setParallelProjection(true);
        return backgroundCamera;
    }

    public void initializeImageBuffer(int width, int height) {
        int bufferSizeR = width * height * 24;
        byte[] previewBufferSize = new byte[bufferSizeR];
        backgroundImageBuffer = ByteBuffer.allocateDirect(previewBufferSize.length);
        backgroundCameraImage = new Image(Image.Format.RGB8, width, height, backgroundImageBuffer);
        backgroundImageBuffer.clear();
    }

    @Override
    public void QCAR_onUpdate(State state) {
        com.qualcomm.vuforia.Image image = getRGB888Image(state.getFrame());

        if (image != null) {
            ByteBuffer pixels = image.getPixels();
            byte[] pixelArray = new byte[pixels.remaining()];
            pixels.get(pixelArray, 0, pixelArray.length);

            if(backgroundImageBuffer == null) {
                initializeImageBuffer(image.getWidth(), image.getHeight());
            } else {
                backgroundImageBuffer.clear();
            }
            backgroundImageBuffer.put(pixelArray);
            backgroundCameraImage.setData(backgroundImageBuffer);

            hasBackgroundImage = true;
        }
    }

    private com.qualcomm.vuforia.Image getRGB888Image(Frame frame) {
        com.qualcomm.vuforia.Image image = null;
        for (int tIdx = 0; tIdx < frame.getNumImages(); tIdx++) {
            com.qualcomm.vuforia.Image vuforiaImage = frame.getImage(tIdx);
            if (vuforiaImage.getFormat() == PIXEL_FORMAT.RGB888) {
                image = vuforiaImage;
                break;
            }
        }
        return image;
    }
}
