/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package ch.zuehlke.arscrabble;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.Frame;
import com.qualcomm.vuforia.Image;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.PIXEL_FORMAT;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Vector;

import ch.zuehlke.arscrabble.vuforiautils.Texture;

import static ch.zuehlke.arscrabble.VectorUtils.calcCorners;
import static ch.zuehlke.arscrabble.VectorUtils.vecToPoint;


public class ImageTargetsActivity extends Activity implements ApplicationControl {
    private static final String LOGTAG = "ImageTargets";

    ApplicationSession vuforiaAppSession;

    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private int mStartDatasetsIndex = 0;
    private int mDatasetsNumber = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();

    // Our OpenGL view:
    private ApplicationGLView mGlView;

    // Our renderer:
    private ImageTargetRenderer mRenderer;

    private GestureDetector mGestureDetector;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private boolean mSwitchDatasetAsap = false;
    private boolean mFlash = false;
    private boolean mContAutofocus = false;
    private boolean mExtendedTracking = false;

    private View mFlashOptionView;

    //private RelativeLayout mUILayout;

    boolean mIsDroidDevice = false;

    private int frameCounter;
    private Bitmap processedBitmap;
    private Bitmap liveBitmap;
    private Bitmap segmentBitmap;
    private ImageView liveImageView;
    private ImageView processedImageView;
    private ImageView segmentImageView;


    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageproc);
        liveImageView = (ImageView) findViewById(R.id.live);
        processedImageView = (ImageView) findViewById(R.id.processed);
        segmentImageView = (ImageView) findViewById(R.id.segment);

        vuforiaAppSession = new ApplicationSession(this);

        OpenCVLoader.initDebug();

        mDatasetStrings.add("board.xml");

        vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mGestureDetector = new GestureDetector(this, new GestureListener());

        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith("droid");
    }

    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable() {
                public void run() {
                    boolean result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

                    if (!result) {
                        Log.e("SingleTapUp", "Unable to trigger focus");
                    }
                }
            }, 1000L);

            return true;
        }
    }


    // We want to load specific textures from the APK, which we will later use
    // for rendering.

    private void loadTextures() {
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotRed.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("ImageTargets/Buildings.jpeg",
                getAssets()));
    }


    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume() {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        try {
            vuforiaAppSession.resumeAR();
        } catch (ApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null) {
            Log.d(LOGTAG, "GLView goes active");
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }


    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }


    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause() {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        // Turn off the flash
        if (mFlashOptionView != null && mFlash) {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ((Switch) mFlashOptionView).setChecked(false);
            } else {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }

        try {
            vuforiaAppSession.pauseAR();
        } catch (ApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }
    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try {
            vuforiaAppSession.stopAR();
        } catch (ApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }


    // Initializes AR application components.
    private void initApplicationAR() {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new ApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new ImageTargetRenderer(this, vuforiaAppSession);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);

    }

    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) tManager
                .getTracker(ImageTracker.getClassType());
        if (imageTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = imageTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (!mCurrentDataset.load(
                mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!imageTracker.activateDataSet(mCurrentDataset))
            return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if (isExtendedTrackingActive()) {
                trackable.startExtendedTracking();
            }

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                    + (String) trackable.getUserData());
        }

        return true;
    }


    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) tManager
                .getTracker(ImageTracker.getClassType());
        if (imageTracker == null)
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive()) {
            if (imageTracker.getActiveDataSet().equals(mCurrentDataset)
                    && !imageTracker.deactivateDataSet(mCurrentDataset)) {
                result = false;
            } else if (!imageTracker.destroyDataSet(mCurrentDataset)) {
                result = false;
            }

            mCurrentDataset = null;
        }

        return result;
    }


    @Override
    public void onInitARDone(ApplicationException exception) {

        if (exception == null) {
            initApplicationAR();

            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            // addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//            imageView = new ImageView(this);
//
//            addContentView(imageView, new LayoutParams(LayoutParams.WRAP_CONTENT,
//                    LayoutParams.WRAP_CONTENT));

            // Sets the UILayout to be drawn in front of the camera
            //mUILayout.bringToFront();

            // Sets the layout background to transparent
            //mUILayout.setBackgroundColor(Color.TRANSPARENT);

            try {
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (ApplicationException e) {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (result)
                mContAutofocus = true;
            else
                Log.e(LOGTAG, "Unable to enable continuous autofocus");

/*            mSampleAppMenu = new SampleAppMenu(this, this, "Image Targets",
                    mGlView, mUILayout, null);
            setSampleAppMenuSettings();*/

        } else {
            Log.e(LOGTAG, exception.getString());
            finish();
        }
    }


    @Override
    public void onQCARUpdate(State state) {
        Image imageFromFrame = null;

        long tic = System.currentTimeMillis();
        Frame frame = state.getFrame();
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {

            final boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            android.graphics.Point windowSize = new android.graphics.Point();
            final View view = findViewById(android.R.id.content);

            windowSize.set(1280, 720);

            final TrackerCorners corners = calcCorners(state, state.getTrackableResult(tIdx), windowSize.x, windowSize.y, isLandscape);
            for (int imageIdx = 0; imageIdx < frame.getNumImages(); imageIdx++) {
                Image image = frame.getImage(imageIdx);
                if (image.getFormat() == PIXEL_FORMAT.RGB888) {
                    imageFromFrame = image;
                    break;
                }
            }


            boolean drawCircles = true;
            boolean rectify = true;
            boolean drawBorder = false;
            boolean drawSegmentationLines = rectify && true;
            boolean segment = rectify && true;

            if (imageFromFrame != null) {
                ByteBuffer pixels = imageFromFrame.getPixels();
                byte[] pixelArray = new byte[pixels.remaining()];
                pixels.get(pixelArray, 0, pixelArray.length);
                int imageWidth = imageFromFrame.getWidth();
                int imageHeight = imageFromFrame.getHeight();
                int stride = imageFromFrame.getStride();


                Mat imageMat = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC3);
                imageMat.put(0, 0, pixelArray);

                final Scalar red = new Scalar(255, 0, 0);
                final Scalar green = new Scalar(0, 255, 0);
                final Scalar blue = new Scalar(0, 0, 255);
                final Scalar yellow = new Scalar(255, 255, 0);
                final Scalar cyan = new Scalar(0, 255, 255);

                final Point center = vecToPoint(corners.getCenter());
                final Point upperLeft = vecToPoint(corners.getUpperLeft());
                final Point upperRight = vecToPoint(corners.getUpperRight());
                final Point lowerLeft = vecToPoint(corners.getLowerLeft());
                final Point lowerRight = vecToPoint(corners.getLowerRight());

                if (drawCircles) {
                    drawCornerCircles(imageMat, red, green, blue, yellow, cyan, center, upperLeft, upperRight, lowerLeft, lowerRight);
                }

                putMatOnImageView(imageMat, liveBitmap, liveImageView);

                boolean computationFrame;
                if (frameCounter < 5) {
                    frameCounter++;
                    computationFrame = false;
                } else {
                    frameCounter = 0;
                    computationFrame = true;
                }

                if (computationFrame) {
                    Log.i(LOGTAG, "RECALCULATING BOARD");


                    if (rectify) {
                        imageMat = RectifyAlgorithm.rectifyToInputMat(imageMat, new Point[]{upperLeft, upperRight, lowerLeft, lowerRight});

                        logElapsedTime("rectification: ", tic);
                    }
                    if (drawBorder) {
                        Mat rectified = drawBorder(imageMat);
                    }

                    if (segment) {
                        ScrabbleBoardSegmentator.segmentImage(imageMat);
                        final Mat scrabbleTile = ScrabbleBoardSegmentator.getScrabbleTile(imageMat, 3, 3, ScrabbleBoardMetrics.metricsFromImage(imageMat));
                        putMatOnImageView(scrabbleTile, segmentBitmap, segmentImageView);
                    }

                    if (drawSegmentationLines) {
                        imageMat = ScrabbleBoardSegmentator.drawSegmentationLines(imageMat);
                    }



                    Mat toDraw = imageMat;
                    putMatOnImageView(imageMat, processedBitmap, processedImageView);
                    logElapsedTime("total computation took: ", tic);
                }
                imageMat.release();

            }
        }


        if (mSwitchDatasetAsap) {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ImageTracker it = (ImageTracker) tm.getTracker(ImageTracker
                    .getClassType());
            if (it == null || mCurrentDataset == null
                    || it.getActiveDataSet() == null) {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }

    public void putMatOnImageView(Mat image, Bitmap bitmap, ImageView imageView) {
        // convert to bitmap:
        if (bitmap != null) {
            bitmap.recycle();
        }

        bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bitmap);
        imageView.setImageBitmap(bitmap);


    }

    private void logElapsedTime(String msg, long tic) {
        Log.d(LOGTAG, msg + " " + (System.currentTimeMillis() - tic) + " ms");
    }

    private void drawCornerCircles(Mat imageMat, Scalar red, Scalar green, Scalar blue, Scalar yellow, Scalar cyan, Point center, Point upperLeft, Point upperRight, Point lowerLeft, Point lowerRight) {
        Core.circle(imageMat, center, 10, red, 5);
        Core.circle(imageMat, upperLeft, 10, green, 5);
        Core.circle(imageMat, upperRight, 10, blue, 5);
        Core.circle(imageMat, lowerLeft, 10, yellow, 5);
        Core.circle(imageMat, lowerRight, 10, cyan, 5);
    }


    private Mat drawBorder(Mat imageMat) {
        Core.rectangle(imageMat, new Point(0, 0), new Point(imageMat.cols(), imageMat.rows()), new Scalar(255, 0, 0), 5);
        return imageMat;
    }


    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ImageTracker.getClassType());
        if (tracker == null) {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }


    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker imageTracker = TrackerManager.getInstance().getTracker(
                ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.start();

        return result;
    }


    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker imageTracker = TrackerManager.getInstance().getTracker(
                ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.stop();

        return result;
    }


    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ImageTracker.getClassType());

        return result;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Process the Gestures
        //if (mSampleAppMenu != null && mSampleAppMenu.processEvent(event))
        //    return true;

        return mGestureDetector.onTouchEvent(event);
    }


    boolean isExtendedTrackingActive() {
        return mExtendedTracking;
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
