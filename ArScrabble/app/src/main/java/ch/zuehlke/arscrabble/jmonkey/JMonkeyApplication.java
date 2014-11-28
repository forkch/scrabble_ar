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
import java.util.Map;

import ch.zuehlke.arscrabble.TrackerCorners;
import ch.zuehlke.arscrabble.model.scrabble.engine.Letter;
import ch.zuehlke.arscrabble.model.scrabble.engine.Player;
import ch.zuehlke.arscrabble.model.scrabble.engine.Rack;
import ch.zuehlke.arscrabble.model.scrabble.engine.Scrabble;
import ch.zuehlke.arscrabble.model.scrabble.engine.Stone;
import ch.zuehlke.arscrabble.model.scrabble.engine.Turn;
import ch.zuehlke.arscrabble.model.scrabble.engine.fields.SimpleField;
import ch.zuehlke.arscrabble.model.scrabble.solver.ScrabbleSolver;
import ch.zuehlke.arscrabble.model.scrabble.solver.VirtualStone;
import ch.zuehlke.arscrabble.vision.BoardDetection;
import ch.zuehlke.arscrabble.vision.BoardVisionResult;
import ch.zuehlke.arscrabble.vision.ScrabbleBoardMetrics;
import ch.zuehlke.arscrabble.vuforiautils.SampleMath;

import static ch.zuehlke.arscrabble.VectorUtils.calcCorners;

/**
 * Created by ssh on 25.11.2014.
 */
public class JMonkeyApplication extends SimpleApplication implements BoardDetection.BoardDetectionDebugCallback {
    private Material backgroundCameraMaterial;
    private Texture2D backgroundCameraTexture;
    private Spatial backgroundCameraGeometry;
    private Image backgroundCameraImage;
    private ByteBuffer backgroundImageBuffer;
    private Camera foregroundCamera;
    private DataSet mCurrentDataset;
    private HashMap<VirtualStone, Spatial> virtualStones = new HashMap<VirtualStone, Spatial>();
    private ScrabbleBoardMetrics metrics;
    private ScrabbleSolver scrabbleSolver;
    private Turn currentTurn;

    private boolean isBoardTracked;
    private boolean isBoardVisible;
    private ScrabbleUI ui;
    private Scrabble game;
    private BoardDetection boardDetection;

    private boolean finishRound;

    private void finishRound(State currentState){

        finishRound = false;

        com.qualcomm.vuforia.Image image = getRGB888Image(currentState.getFrame());
        if (image != null && isBoardTracked) {

            Log.d("Vision", "Start ");
            android.graphics.Point windowSize = new android.graphics.Point();
            windowSize.set(1280, 720);

            Log.d("Vision", "Get corners");
            TrackerCorners corners = calcCorners(currentState, currentState.getTrackableResult(0), windowSize.x, windowSize.y, true);

            Log.d("Vision", "Got corners -> image to pixelArray");

            ByteBuffer pixels = image.getPixels();
            byte[] pixelArray = new byte[pixels.remaining()];
            pixels.get(pixelArray, 0, pixelArray.length);


            Log.d("Vision", "Start first detection");
            BoardVisionResult visionResult = boardDetection.detectBoard(pixelArray, image.getWidth(), image.getHeight(), corners, true, 0, 0, true);

            int scanCount = 0;
            while (!visionResult.isScanSuccessful() && scanCount < 10) {
                Log.d("Vision", "Start first detection: " + scanCount);
                visionResult = boardDetection.detectBoard(pixelArray, image.getWidth(), image.getHeight(), corners, true, 0, 0, true);
                scanCount++;
            }

            if (!visionResult.isScanSuccessful()) {
                Log.d("Vision", "Could not find board -> abort");
                return;
            }

            Log.d("Vision", "Got result from vision -> analyze");

            SimpleField[][] fields = game.getBoard().getFields();

            for (int y = 0; y < fields.length; y++) {
                SimpleField[] row = fields[y];
                for (int x = 0; x < row.length; x++) {
                    char visionLetter = visionResult.getLettersOnBoard()[y][x];
                    if(visionLetter == 0) {
                        continue;
                    }
                    Letter onBoardletter = fields[y][x].getLetter();

                    if (onBoardletter == null || onBoardletter.getValue() != visionLetter) {
                        currentTurn.placeStone(x, y, Letter.getLetterFor(visionLetter));
                        Log.d("Vision", "Add '" + visionLetter + "' to X(" + x + ") Y(" + y + ")");
                    }
                }
            }

            game.executeTurn(currentTurn);

            updateActivePlayer();

            if (!hasMissingStones(game.getActivePlayer())) {
                currentTurn = game.newTurn(null);
            }
        }
    }


    public void roundFinished() {
        finishRound = true;
    }

    public void startGame(HashMap<String, String> players, String[] wordList) {
        game = new Scrabble();

        for (Map.Entry<String, String> playerInfo : players.entrySet()) {
            List<Stone> stones = new ArrayList<Stone>();
            char[] stoneParts = playerInfo.getValue().toCharArray();
            for (char stone : stoneParts) {
                stones.add(game.getStoneBag().pop(Letter.valueOf((stone + "").toUpperCase())));
            }

            Player player = new Player(playerInfo.getKey(), new Rack(stones));
            game.addPlayer(player);
        }

        game.start();


        scrabbleSolver = new ScrabbleSolver(game, wordList);

        updateActivePlayer();

        currentTurn = game.newTurn(null);
    }

    public void setNewStones(String newStones) {
        char[] stoneParts = newStones.toCharArray();
        Letter[] letters = new Letter[stoneParts.length];
        for (int i = 0; i < stoneParts.length; i++) {
            letters[i] = Letter.valueOf(stoneParts[i] + "");
        }
        currentTurn = game.newTurn(letters);
        updateActivePlayer();
    }

    private void updateActivePlayer() {
        Player activePlayer = game.getActivePlayer();
        ui.UpdatePlayer(activePlayer.getName());
        List<Stone> stones = activePlayer.getRack().getStones();
        String remainingStones = "";
        for (Stone stone : stones) {
            remainingStones += stone.getLetter().getValue();
        }
        ui.UpdatePlayerStones(remainingStones);

        ui.setPlayerNeedStones(hasMissingStones(activePlayer));
    }

    private boolean hasMissingStones(Player activePlayer) {
        return activePlayer.getRack().getNumberOfMissingStones() > 0;
    }

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

        boardDetection = new BoardDetection(this);
    }

    private void initTrackers() {
        doInitTrackers();
        doLoadTrackersData();
        doStartTrackers();
    }

    private void initDeviceCamera() {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        cameraDevice.init(CameraDevice.CAMERA.CAMERA_DEFAULT);
        CameraDevice.getInstance().start();

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

    private Spatial createStone(Letter letter) {
        String letterModel = "Models/Stone/stone_" + letter.getTextureName() + ".obj";
        Spatial stone = assetManager.loadModel(letterModel);
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

    @Override
    public void simpleUpdate(float tpf) {
        // First get the current state from Vuforia
        // -> We get tracking informations and the current image/frame
        State currentState = Renderer.getInstance().begin();

        if(finishRound){
            Log.d("Vision", "Finish round now");
            finishRound(currentState);
        }

        updateBackgroundVideoImage(currentState, tpf);
        updateJMonkeyCameraByVuforiaState(currentState);

        if (isBoardTracked) {
            showBoard();
        } else {
            hideBoard();
        }
    }

    private void hideBoard() {
        for (Spatial stone : virtualStones.values()) {
            rootNode.detachChild(stone);
        }

        isBoardVisible = false;
    }

    private void showBoard() {
        if (!isBoardVisible) {
            for (Spatial stone : virtualStones.values()) {
                rootNode.attachChild(stone);
            }

            isBoardVisible = true;
        }

        if (scrabbleSolver != null) {

            List<VirtualStone> allVirtualStones = scrabbleSolver.getWord(game.getActivePlayer());

            removeNotExistingStones(allVirtualStones);
            addNewStones(allVirtualStones);
        }
    }

    private void addNewStones(List<VirtualStone> allVirtualStones) {
        List<VirtualStone> stonesToAdd = new ArrayList<VirtualStone>();
        for (VirtualStone newStone : allVirtualStones) {

            if (!virtualStones.containsKey(newStone)) {
                stonesToAdd.add(newStone);
            }
        }

        for (VirtualStone stoneToAdd : stonesToAdd) {
            Spatial stoneSpatial = createStone(stoneToAdd.getStone().getLetter());
            moveToField(stoneSpatial, stoneToAdd.getX(), stoneToAdd.getY());
            rootNode.attachChild(stoneSpatial);
            virtualStones.put(stoneToAdd, stoneSpatial);
        }
    }

    private void removeNotExistingStones(List<VirtualStone> allVirtualStones) {
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
    }

    private void moveToField(Spatial stone, int x, int y) {

        // TODO: Where to add this? -> Solver is 0 based we are 1 based
        x++;
        y++;

        // Initial stone position -> x = 8 / y = 8
        x = x - 8;
        y = (y - 8) * -1;

        float lineWidth = 5.8f;

        int totalWidth = metrics.getCellWidth() * 15 + metrics.getMarginLeft() + metrics.getMarginRight();
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
        Camera backgroundCamera = createBackgroundCamera(width, height);

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
        backgroundImageBuffer = ByteBuffer.allocateDirect(bufferSizeR);
        backgroundCameraImage = new Image(Image.Format.RGB8, width, height, backgroundImageBuffer);
        backgroundImageBuffer.clear();
    }

    public void updateBackgroundVideoImage(State state, float tpf) {
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

            Mat imageMat = Mat.zeros(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
            metrics = ScrabbleBoardMetrics.metricsFromImageFrom3D(imageMat);

            backgroundCameraTexture.setImage(backgroundCameraImage);
            backgroundCameraMaterial.setTexture("ColorMap", backgroundCameraTexture);
        }

        // TODO: WTF? Why we need this method? Crash without...
        backgroundCameraGeometry.updateLogicalState(tpf);
        backgroundCameraGeometry.updateGeometricState();
    }

    private com.qualcomm.vuforia.Image getRGB888Image(Frame frame) {
        com.qualcomm.vuforia.Image image = null;
        int num = frame.getNumImages();
        for (int tIdx = 0; tIdx < frame.getNumImages(); tIdx++) {
            com.qualcomm.vuforia.Image vuforiaImage = frame.getImage(tIdx);
            if (vuforiaImage.getFormat() == PIXEL_FORMAT.RGB888) {
                image = vuforiaImage;
                break;
            }
        }
        return image;
    }

    private void updateJMonkeyCameraByVuforiaState(State currentState) {
        isBoardTracked = currentState.getNumTrackableResults() > 0;

        if (isBoardTracked) {

            TrackableResult result = currentState.getTrackableResult(0);

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
            float viewportDistort;

            float screenWidth = settings.getWidth();
            float screenHeight = settings.getHeight();
            if (viewportWidth != screenWidth) {
                viewportDistort = viewportWidth / screenWidth;
                fovDegrees = fovDegrees * viewportDistort;
                aspectRatio = aspectRatio / viewportDistort;
            }

            if (viewportHeight != screenHeight) {
                viewportDistort = viewportHeight / screenHeight;
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

    public void setUI(ScrabbleUI ui) {
        this.ui = ui;
    }

    @Override
    public void putMatOnLiveImageView(Mat imageMat) {

    }

    @Override
    public void putMatOnSegment1ImageView(Mat imageMat) {

    }

    @Override
    public void putMatOnSegment2ImageView(Mat imageMat) {

    }

    @Override
    public void putMatOnProcessedImageView(Mat imageMat) {

    }

    @Override
    public void setDivTextView(String s) {

    }

    @Override
    public void setOCRTextView(String s) {

    }
}