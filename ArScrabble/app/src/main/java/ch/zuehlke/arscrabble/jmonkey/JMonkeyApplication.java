package ch.zuehlke.arscrabble.jmonkey;

import android.util.Log;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.Frame;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.Matrix34F;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.PIXEL_FORMAT;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.zuehlke.arscrabble.model.scrabble.engine.Board;
import ch.zuehlke.arscrabble.model.scrabble.engine.Letter;
import ch.zuehlke.arscrabble.model.scrabble.engine.Player;
import ch.zuehlke.arscrabble.model.scrabble.engine.Rack;
import ch.zuehlke.arscrabble.model.scrabble.engine.Scrabble;
import ch.zuehlke.arscrabble.model.scrabble.engine.Stone;
import ch.zuehlke.arscrabble.model.scrabble.engine.StoneBag;
import ch.zuehlke.arscrabble.model.scrabble.engine.StoneType;
import ch.zuehlke.arscrabble.model.scrabble.solver.ScrabbleSolver;
import ch.zuehlke.arscrabble.model.scrabble.solver.VirtualStone;
import ch.zuehlke.arscrabble.vision.ScrabbleBoardMetrics;
import ch.zuehlke.arscrabble.vuforiautils.SampleMath;

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
    private Camera backgroundCamera;
    private Camera foregroundCamera;
    private DataSet mCurrentDataset;
    private HashMap<VirtualStone, Spatial> virtualStones = new HashMap<VirtualStone, Spatial>();
    private ScrabbleBoardMetrics metrics;
    private Board board = new Board();
    private Scrabble game;
    private Player stefan;

    @Override
    public void simpleInitApp() {
        // We use custom viewports - so the main viewport does not need to contain the rootNode
        viewPort.detachScene(rootNode);

        OpenCVLoader.initDebug(false);

        initTrackers();
        initDeviceCamera();

        initBackground();
        initForegroundScene();

        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB888, true);
        Vuforia.registerCallback(this);

        /* GAME */
        game = new Scrabble();

        stefan = new Player("Stefan", new Rack(getStefansStones(game.getStoneBag())));
        Player benjamin = new Player("Benjamin", new Rack(getBenjaminsStones(game.getStoneBag())));

        game.addPlayer(stefan);
        game.addPlayer(benjamin);

        game.start();
         /* GAME */

        board.placeVirtualStone(new Stone(Letter.H, StoneType.VIRTUAL), 1, 1);
        board.placeVirtualStone(new Stone(Letter.H, StoneType.VIRTUAL), 1, 2);
        board.placeVirtualStone(new Stone(Letter.H, StoneType.VIRTUAL), 1, 3);
        board.placeVirtualStone(new Stone(Letter.H, StoneType.VIRTUAL), 1, 4);
        board.placeVirtualStone(new Stone(Letter.H, StoneType.VIRTUAL), 1, 5);
        board.placeVirtualStone(new Stone(Letter.H, StoneType.VIRTUAL), 1, 6);
        board.placeVirtualStone(new Stone(Letter.H, StoneType.VIRTUAL), 1, 7);
        board.placeVirtualStone(new Stone(Letter.H, StoneType.VIRTUAL), 1, 8);

        board.placeVirtualStone(new Stone(Letter.H, StoneType.VIRTUAL), 8, 8);
    }

    private static List<Stone> getStefansStones(StoneBag stoneBag) {
        List<Stone> stefansStones = new ArrayList<Stone>();
        stefansStones.add(stoneBag.pop(Letter.A));
        stefansStones.add(stoneBag.pop(Letter.D));
        stefansStones.add(stoneBag.pop(Letter.F));
        stefansStones.add(stoneBag.pop(Letter.B));
        stefansStones.add(stoneBag.pop(Letter.O));
        stefansStones.add(stoneBag.pop(Letter.A));
        stefansStones.add(stoneBag.pop(Letter.K));
        return stefansStones;
    }

    private static List<Stone> getBenjaminsStones(StoneBag stoneBag) {
        List<Stone> benjaminsStones = new ArrayList<Stone>();
        benjaminsStones.add(stoneBag.pop(Letter.G));
        benjaminsStones.add(stoneBag.pop(Letter.H));
        benjaminsStones.add(stoneBag.pop(Letter.S));
        benjaminsStones.add(stoneBag.pop(Letter.T));
        benjaminsStones.add(stoneBag.pop(Letter.M));
        benjaminsStones.add(stoneBag.pop(Letter.O));
        benjaminsStones.add(stoneBag.pop(Letter.K));
        return benjaminsStones;
    }

    private void initTrackers() {
        doInitTrackers();
        doLoadTrackersData();
        doStartTrackers();
    }

    private void initDeviceCamera() {
        CameraDevice cameraDevice = initCameraDevice();
        VideoMode videoMode = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_OPTIMIZE_SPEED);
        VideoBackgroundConfig config = initVideoBackgroundConfig(videoMode);
        Renderer.getInstance().setVideoBackgroundConfig(config);
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

    private Spatial createStone() {
        Spatial stone = assetManager.loadModel("Models/Stone/stone_u.obj");
        stone.rotate((float) (Math.PI / 2), 0, (float) Math.PI);
        stone.scale(0.27f);
        return stone;
    }

    private void initForegroundScene() {
        // Add light from every side
        addLight(ColorRGBA.White, -1, 0, 0);
        addLight(ColorRGBA.White, 0, -1, 0);
        addLight(ColorRGBA.White, 0, 0, -1);
        addLight(ColorRGBA.White, 1, 0, 0);
        addLight(ColorRGBA.White, 0, 1, 0);
        addLight(ColorRGBA.White, 0, 0, 1);

        foregroundCamera = new Camera(settings.getWidth(), settings.getHeight());
        ViewPort foregroundViewPort = renderManager.createMainView("ForegroundView", foregroundCamera);
        foregroundViewPort.attachScene(rootNode);
        foregroundViewPort.setClearFlags(false, true, false);
    }

    private void addLight(ColorRGBA color, int x, int y, int z) {
        DirectionalLight sun1 = new DirectionalLight();
        sun1.setColor(color);
        sun1.setDirection(new Vector3f(x, y, z).normalizeLocal());
        rootNode.addLight(sun1);
    }

    private CameraDevice initCameraDevice() {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        cameraDevice.init(CameraDevice.CAMERA.CAMERA_DEFAULT);
        CameraDevice.getInstance().start();
        return cameraDevice;
    }

    @Override
    public void simpleUpdate(float tpf) {
        updateTracking();
        updateBoard();
        updateBackgroundVideo(tpf);
    }

    private void updateBoard() {

        ScrabbleSolver solver = new ScrabbleSolver(game);
        List<VirtualStone> allVirtualStones = solver.getWord(stefan);

        // Which stones have to be removed?
        List<VirtualStone> stonesToRemove = new ArrayList<VirtualStone>();

        for (VirtualStone existingStone : virtualStones.keySet()) {
            boolean found = false;
            for (VirtualStone virtualStone : allVirtualStones) {
                if (existingStone.equals(virtualStone)) {
                    found = true;
                }
            }

            if (!found) {
                stonesToRemove.add(existingStone);
            }
        }

        for (VirtualStone stoneToRemove : stonesToRemove) {
            rootNode.detachChild(virtualStones.get(stoneToRemove));
            virtualStones.remove(stoneToRemove);
        }

        // Which stones are new and have to be added?
        List<VirtualStone> stonesToAdd = new ArrayList<VirtualStone>();
        for (VirtualStone newStone : allVirtualStones) {

            if (!virtualStones.containsKey(newStone)) {
                stonesToAdd.add(newStone);
            }
        }

        for (VirtualStone stoneToAdd : stonesToAdd) {
            Spatial stoneSpatial = createStone();
            moveToField(stoneSpatial, stoneToAdd.getX(), stoneToAdd.getY());
            rootNode.attachChild(stoneSpatial);
            virtualStones.put(stoneToAdd, stoneSpatial);
        }
    }

    private void updateBackgroundVideo(float tpf) {
        if (hasBackgroundImage) {
            backgroundCameraTexture.setImage(backgroundCameraImage);
            backgroundCameraMaterial.setTexture("ColorMap", backgroundCameraTexture);
        }

        // TODO: WTF? Why we need this method? Crash without...
        backgroundCameraGeometry.updateLogicalState(tpf);
        backgroundCameraGeometry.updateGeometricState();
    }

    private void moveToField(Spatial stone, int x, int y) {

        // Initial stone position -> x = 8 / y = 8
        x = x - 8;
        y = (y - 8) * -1;

        int lineWidth = 6;

        int totalWidth = metrics.getCellWidth() * 15 + metrics.getMarginLeft() + metrics.getMarginRight() + 16 * lineWidth;
        float multiplicator = 100.0f / totalWidth;

        // Move stone to center (H8)
        float moveVerticalCenter = 0.5f * metrics.getCellHeight();

        // Move stone to position(XY)
        float moveHorizontal = (x * metrics.getCellWidth() + lineWidth * x) * multiplicator;
        float moveVertical = ((y * metrics.getCellHeight() + lineWidth * y) + moveVerticalCenter) * multiplicator;

        stone.move(moveHorizontal, moveVertical, 0);
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
        backgroundCamera = createBackgroundCamera(width, height);

        ViewPort videoBackgroundViewPort = renderManager.createMainView("VideoBGView", backgroundCamera);
        videoBackgroundViewPort.attachScene(backgroundCameraGeometry);
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

            if (backgroundImageBuffer == null) {
                initializeImageBuffer(image.getWidth(), image.getHeight());
            } else {
                backgroundImageBuffer.clear();
            }

            backgroundImageBuffer.put(pixelArray);
            backgroundCameraImage.setData(backgroundImageBuffer);

            hasBackgroundImage = true;

            Mat imageMat = Mat.zeros(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
            metrics = new ScrabbleBoardMetrics(imageMat);
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

    private void updateTracking() {
        State currentState = Renderer.getInstance().begin();
        int numberOfTrackableResults = currentState.getNumTrackableResults();

        for (int tIdx = 0; tIdx < numberOfTrackableResults; tIdx++) {
            TrackableResult result = currentState.getTrackableResult(tIdx);

            final Matrix34F pose = result.getPose();
            Matrix44F modelViewMatrixVuforia = Tool.convertPose2GLMatrix(pose);

            Matrix44F inverseMv = SampleMath.Matrix44FInverse(modelViewMatrixVuforia);
            Matrix44F invTranspMV = SampleMath.Matrix44FTranspose(inverseMv);

            //get position
            float cam_x = invTranspMV.getData()[12];
            float cam_y = invTranspMV.getData()[13];
            float cam_z = invTranspMV.getData()[14];

            //get rotation
            float cam_right_x = invTranspMV.getData()[0];
            float cam_right_y = invTranspMV.getData()[1];
            float cam_right_z = invTranspMV.getData()[2];
            float cam_up_x = invTranspMV.getData()[4];
            float cam_up_y = invTranspMV.getData()[5];
            float cam_up_z = invTranspMV.getData()[6];
            float cam_dir_x = invTranspMV.getData()[8];
            float cam_dir_y = invTranspMV.getData()[9];
            float cam_dir_z = invTranspMV.getData()[10];

            setCameraPoseNative(cam_x, cam_y, cam_z);
            setCameraOrientation(cam_right_x, cam_right_y, cam_right_z, cam_up_x, cam_up_y, cam_up_z, cam_dir_x, cam_dir_y, cam_dir_z);

            float nearPlane = 1.0f;
            float farPlane = 1000.0f;
            CameraCalibration cameraCalibration = CameraDevice.getInstance().getCameraCalibration();

            VideoBackgroundConfig config = Renderer.getInstance().getVideoBackgroundConfig();

            float viewportWidth = config.getSize().getData()[0];
            float viewportHeight = config.getSize().getData()[1];

            Vec2F size = cameraCalibration.getSize();
            Vec2F focalLength = cameraCalibration.getFocalLength();
            float fovRadians = (float) (2 * Math.atan(0.5f * (size.getData()[1] / focalLength.getData()[1])));
            float fovDegrees = (float) (fovRadians * 180.0f / Math.PI);
            float aspectRatio = (size.getData()[0] / size.getData()[1]);

            //adjust for screen vs camera size distorsion
            float viewportDistort = 1.0f;

            float screenWidth = settings.getWidth();
            float screenHeight = settings.getHeight();
            if (viewportWidth != screenWidth) {
                viewportDistort = viewportWidth / (float) screenWidth;
                fovDegrees = fovDegrees * viewportDistort;
                aspectRatio = aspectRatio / viewportDistort;
            }

            if (viewportHeight != screenHeight) {
                viewportDistort = viewportHeight / (float) screenHeight;
                fovDegrees = fovDegrees / viewportDistort;
                aspectRatio = aspectRatio * viewportDistort;
            }

            setCameraPerspective(fovDegrees, aspectRatio);
        }
    }

    private void setCameraPerspective(float fovDegrees, float aspectRatio) {
        foregroundCamera.setFrustumPerspective(fovDegrees, aspectRatio, 1.0f, 1000.f);
    }

    private void setCameraOrientation(float cam_right_x, float cam_right_y, float cam_right_z, float cam_up_x, float cam_up_y, float cam_up_z, float cam_dir_x, float cam_dir_y, float cam_dir_z) {
        Vector3f left = new Vector3f(-cam_right_x, -cam_right_y, -cam_right_z);
        Vector3f up = new Vector3f(-cam_up_x, -cam_up_y, -cam_up_z);
        Vector3f direction = new Vector3f(cam_dir_x, cam_dir_y, cam_dir_z);

        foregroundCamera.setAxes(left, up, direction);
    }

    private void setCameraPoseNative(float cam_x, float cam_y, float cam_z) {
        foregroundCamera.setLocation(new Vector3f(cam_x, cam_y, cam_z));
    }

    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) tManager.getTracker(ImageTracker.getClassType());
        if (imageTracker == null) {
            return false;
        }

        if (mCurrentDataset == null) {
            mCurrentDataset = imageTracker.createDataSet();
        }

        if (mCurrentDataset == null) {
            return false;
        }

        if (!mCurrentDataset.load("board.xml", STORAGE_TYPE.STORAGE_APPRESOURCE)) {
            return false;
        }

        if (!imageTracker.activateDataSet(mCurrentDataset)) {
            return false;
        }

        return true;
    }

    public void doInitTrackers() {
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker = tManager.initTracker(ImageTracker.getClassType());
        if (tracker == null) {
            Log.e("JMonkeyApplication", "Tracker not initialized. Tracker already initialized or the camera is already started");
        } else {
            Log.i("JMonkeyApplication", "Tracker successfully initialized");
        }
    }

    public void doStartTrackers() {
        Tracker imageTracker = TrackerManager.getInstance().getTracker(ImageTracker.getClassType());
        if (imageTracker != null) {
            imageTracker.start();
        }
    }
}