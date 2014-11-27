package ch.zuehlke.arscrabble.vision;

import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ch.zuehlke.arscrabble.EnhancedTileImage;
import ch.zuehlke.arscrabble.TrackerCorners;

import static ch.zuehlke.arscrabble.VectorUtils.vecToPoint;
import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.circle;
import static org.opencv.core.Core.mean;
import static org.opencv.core.Core.rectangle;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.threshold;

/**
 * Created with love by fork on 27.11.14.
 */
public class BoardDetection {
    private static final String LOGTAG = BoardDetection.class.getSimpleName();
    private static final String LOGTAG_OCR = LOGTAG + "(OCR)";
    private static final int MINIMAL_OCR_CONFIDENCE = 70;

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

                if (drawBorder) {
                    drawBorder(imageMat);
                }

                if (debugSomeSegments) {
                    oneTileDebug(imageMat, scrabbleBoardSegmenter);
                }

                if (scanScrabbleBoard) {
                    scanScrabbleboard(imageMat, scrabbleBoardSegmenter);
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

    private void scanScrabbleboard(Mat image, ScrabbleBoardSegmenter scrabbleBoardSegmenter) {
        long tic = System.currentTimeMillis();
        String allLetters = "";
        for (int verticalIdx = 0; verticalIdx < 16; verticalIdx++) {
            for (int horizontalIdx = 0; horizontalIdx < 16; horizontalIdx++) {
                final Mat scrabbleTile = scrabbleBoardSegmenter.getScrabbleTile(image, horizontalIdx, verticalIdx);

                EnhancedTileImage processedTile = enhanceTileImage(scrabbleTile, false);
                if (!processedTile.isScrabbleTileProbable()) {
                    continue;
                }

                final CroppedTileAndBoundingBox croppedTileAndBoundingBox = cropTileImageByContourOfLargestBlob(processedTile.getTileImage());

                OCRResult ocrResult = performOCR(croppedTileAndBoundingBox.getCroppedTile(), false);
                if (ocrResult.hasLetterBeenDetected()) {
                    Log.i(LOGTAG_OCR, "tile at (" + horizontalIdx + "," + verticalIdx + "): " + ocrResult);
                    allLetters += ocrResult.getLetter();
                }
            }
        }
        boardDetectionDebugCallback.setDivTextView(allLetters);
        Log.d(LOGTAG_OCR, "scanScrabbleBoard in " + (System.currentTimeMillis() - tic) + "ms");

    }


    private void oneTileDebug(Mat imageMat, ScrabbleBoardSegmenter scrabbleBoardSegmenter) {
        boardDetectionDebugCallback.setOCRTextView("");
        boardDetectionDebugCallback.setDivTextView("");
        Mat scrabbleTile = scrabbleBoardSegmenter.getScrabbleTile(imageMat, singleSegmentX, singleSegmentY);
        final EnhancedTileImage tileImage = enhanceTileImage(scrabbleTile, false);
        if (tileImage.isScrabbleTileProbable()) {
            final Mat thresholdedTileImage = tileImage.getTileImage();

            final CroppedTileAndBoundingBox croppedTileAndBoundingBox = cropTileImageByContourOfLargestBlob(thresholdedTileImage);
            final Rect boundingBoxInInputImage = croppedTileAndBoundingBox.getBoundingBoxInInputImage();

            drawContours(scrabbleTile, Arrays.asList(croppedTileAndBoundingBox.getMaxContour()), -1, new Scalar(0, 255, 0));
            rectangle(scrabbleTile, boundingBoxInInputImage.tl(), boundingBoxInInputImage.br(), new Scalar(255, 0, 0));

            boardDetectionDebugCallback.putMatOnSegment2ImageView(croppedTileAndBoundingBox.getCroppedTile());

            performOCR(croppedTileAndBoundingBox.getCroppedTile(), true);
        } else {
            boardDetectionDebugCallback.putMatOnSegment2ImageView(imageMat);
        }
        boardDetectionDebugCallback.putMatOnSegment1ImageView(scrabbleTile);

        singleSegmentX++;

        if (singleSegmentX == 3) {
            singleSegmentX = 0;
            singleSegmentY++;
        }
        if (singleSegmentY == 2) {
            singleSegmentY = 0;
        }
    }


    private CroppedTileAndBoundingBox cropTileImageByContourOfLargestBlob(Mat inputImage) {

        Mat dilatedInput = new Mat();

        dilate(inputImage, dilatedInput, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat mHierarchy = new Mat();
        findContours(dilatedInput, contours, mHierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            return new CroppedTileAndBoundingBox(inputImage, new Rect(0, 0, inputImage.cols(), inputImage.rows()), new MatOfPoint());
        }
        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        MatOfPoint maxContour = null;
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = contourArea(wrapper);
            if (area > maxArea) {
                maxArea = area;
                maxContour = wrapper;
            }
        }

        if (maxContour == null) {
            return new CroppedTileAndBoundingBox(inputImage, new Rect(0, 0, inputImage.cols(), inputImage.rows()), new MatOfPoint());
        }


        final Rect boundingBoxInInputImage = boundingRect(maxContour);

        return new CroppedTileAndBoundingBox(inputImage.submat(boundingBoxInInputImage), boundingBoxInInputImage, maxContour);
    }

    private EnhancedTileImage enhanceTileImage(Mat scrabbleTile, boolean debug) {

        final Mat grayscale = new Mat();
        cvtColor(scrabbleTile, grayscale, COLOR_RGB2GRAY);

        Mat blur = new Mat();
        Mat sharpened = new Mat();
        GaussianBlur(grayscale, blur, new Size(0, 0), 3);
        addWeighted(grayscale, 1.5, blur, -0.5, 0, sharpened);

        final Scalar meanGrayscale = mean(sharpened);

        if (meanGrayscale.val[0] < 200.f) {
            return EnhancedTileImage.createNoTilePresent();
        }

        final Mat threshold = new Mat();
        threshold(sharpened, threshold, 180, 255, THRESH_BINARY_INV);
        return EnhancedTileImage.createTilePresent(threshold);
    }

    private OCRResult performOCR(Mat processedTile, boolean debug) {
        OCRResult ocrResult;

        ocrResult = callTeseract(processedTile);
        if (debug) {
            boardDetectionDebugCallback.setOCRTextView("(" + singleSegmentX + "," + singleSegmentY + ")=" + ocrResult.toString());
        }
        return ocrResult;
    }

    private OCRResult callTeseract(Mat processedTile) {
        try {
            byte[] imageData = new byte[(processedTile.cols() * processedTile.rows() *
                    processedTile.channels())];

            processedTile.get(0, 0, imageData);

            tessBaseAPI.setImage(imageData, processedTile.cols(), processedTile.rows(), processedTile.channels(), processedTile.cols() * processedTile.channels());

            String textResult = tessBaseAPI.getUTF8Text();
            final int confidence = tessBaseAPI.meanConfidence();
            tessBaseAPI.clear();

            if (confidence < MINIMAL_OCR_CONFIDENCE) {
                return OCRResult.createNoLetterFound();
            }

            return OCRResult.createForLetterFound(textResult, confidence);
        } catch (Exception x) {
            Log.e(LOGTAG_OCR, "error", x);
            return OCRResult.createNoLetterFound();
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

    private void destroy() {
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