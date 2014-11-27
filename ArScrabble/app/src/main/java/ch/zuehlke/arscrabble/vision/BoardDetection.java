package ch.zuehlke.arscrabble.vision;

import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.io.File;

import ch.zuehlke.arscrabble.TrackerCorners;

import static ch.zuehlke.arscrabble.VectorUtils.vecToPoint;
import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.circle;
import static org.opencv.core.Core.rectangle;
import static org.opencv.imgproc.Imgproc.GaussianBlur;

/**
 * Created with love by fork on 27.11.14.
 */
public class BoardDetection {
    private static final String LOGTAG = BoardDetection.class.getSimpleName();

    private final BoardDetectionDebugCallback boardDetectionDebugCallback;
    private TessBaseAPI tessBaseAPI;
    private int frameCounter;

    private int singleSegmentX = 0;
    private int singleSegmentY = 0;

    public BoardDetection(BoardDetectionDebugCallback imageTargetsActivity) {

        this.boardDetectionDebugCallback = imageTargetsActivity;
        initializeTesseract();
    }

    public void detectBoard(byte[] pixelArray, int imageWidth, int imageHeight, TrackerCorners corners) {

        long tic = System.currentTimeMillis();
        Mat imageMat = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC3);
        imageMat.put(0, 0, pixelArray);


        boolean drawCircles = true;
        boolean rectify = true;
        boolean sharpen = false;
        boolean drawBorder = false;
        boolean drawSegmentationLines = rectify && true;
        boolean debugSomeSegments = rectify && true;
        boolean scanScrabbleBoard = rectify && true;

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
        try {
            if (drawCircles) {
                drawCornerCircles(imageMat, red, green, blue, yellow, cyan, center, upperLeft, upperRight, lowerLeft, lowerRight);
            }

            boardDetectionDebugCallback.putMatOnLiveImageView(imageMat);

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

                if (sharpen) {
                    Mat blur = new Mat();
                    GaussianBlur(imageMat, blur, new Size(0, 0), 3);
                    addWeighted(imageMat, 1.5, blur, -0.5, 0, imageMat);
                }

                final ScrabbleBoardSegmenter scrabbleBoardSegmenter = new ScrabbleBoardSegmenter(imageMat);
                final ScrabbleTileProcessor scrabbleTileProcessor = new ScrabbleTileProcessor(tessBaseAPI, boardDetectionDebugCallback);

                if (drawBorder) {
                    drawBorder(imageMat);
                }

                if (debugSomeSegments) {
                    scrabbleTileProcessor.oneTileDebug(imageMat, scrabbleBoardSegmenter, singleSegmentX, singleSegmentY);

                    singleSegmentX++;

                    if (singleSegmentX == 3) {
                        singleSegmentX = 0;
                        singleSegmentY++;
                    }
                    if (singleSegmentY == 2) {
                        singleSegmentY = 0;
                    }
                }

                if (scanScrabbleBoard) {
                    scrabbleTileProcessor.scanScrabbleboard(imageMat, scrabbleBoardSegmenter);
                }

                if (drawSegmentationLines) {
                    imageMat = scrabbleBoardSegmenter.drawSegmentationLines(imageMat);
                }


                Mat toDraw = imageMat;
                boardDetectionDebugCallback.putMatOnProcessedImageView(imageMat);
                logElapsedTime("total computation took: ", tic);
            }

            imageMat.release();
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "something bad happened, discarding frame", e);
        } finally {
            imageMat.release();
        }
    }

    private void logElapsedTime(String msg, long tic) {
        Log.d(LOGTAG, msg + " " + (System.currentTimeMillis() - tic) + " ms");
    }

    private void drawCornerCircles(Mat imageMat, Scalar red, Scalar green, Scalar blue, Scalar yellow, Scalar cyan, Point center, Point upperLeft, Point upperRight, Point lowerLeft, Point lowerRight) {
        circle(imageMat, center, 10, red, 5);
        circle(imageMat, upperLeft, 10, green, 5);
        circle(imageMat, upperRight, 10, blue, 5);
        circle(imageMat, lowerLeft, 10, yellow, 5);
        circle(imageMat, lowerRight, 10, cyan, 5);
    }


    private Mat drawBorder(Mat imageMat) {
        rectangle(imageMat, new Point(0, 0), new Point(imageMat.cols(), imageMat.rows()), new Scalar(255, 0, 0), 5);
        return imageMat;
    }


    private void initializeTesseract() {
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.setDebug(false);
        String path = Environment.getExternalStorageDirectory().getPath() + "/tesseract/";
        final boolean exists = new File(path + "tessdata").exists();
        tessBaseAPI.init(path, "eng", TessBaseAPI.OEM_TESSERACT_ONLY);
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZi");
    }

    public void destroy() {
        tessBaseAPI.end();
    }

    public interface BoardDetectionDebugCallback {
        void putMatOnLiveImageView(Mat imageMat);

        void putMatOnSegment1ImageView(Mat imageMat);

        void putMatOnSegment2ImageView(Mat imageMat);

        void putMatOnProcessedImageView(Mat imageMat);

        void setDivTextView(String s);

        void setOCRTextView(String s);
    }
}