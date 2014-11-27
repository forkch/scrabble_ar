package ch.zuehlke.arscrabble.vision;

import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ch.zuehlke.arscrabble.ImageTargetsActivity;
import ch.zuehlke.arscrabble.TileImage;
import ch.zuehlke.arscrabble.TrackerCorners;

import static ch.zuehlke.arscrabble.VectorUtils.vecToPoint;

/**
 * Created with love by fork on 27.11.14.
 */
public class BoardDetection {
    private static final String LOGTAG = BoardDetection.class.getSimpleName();
    private static final String LOGTAG_OCR = LOGTAG + "(OCR)";
    public static final int MINIMAL_OCR_CONFIDENCE = 60;

    private final ImageTargetsActivity imageTargetsActivity;
    private final TessBaseAPI tessBaseAPI;
    private int frameCounter;

    private int singleSegmentX = 0;
    private int singleSegmentY = 0;

    public BoardDetection(ImageTargetsActivity imageTargetsActivity, TessBaseAPI tessBaseAPI) {

        this.imageTargetsActivity = imageTargetsActivity;
        this.tessBaseAPI = tessBaseAPI;
    }

    public void detectBoard(byte[] pixelArray, int imageWidth, int imageHeight, TrackerCorners corners) {

        long tic = System.currentTimeMillis();
        Mat imageMat = Mat.zeros(imageHeight, imageWidth, CvType.CV_8UC3);
        imageMat.put(0, 0, pixelArray);


        boolean drawCircles = true;
        boolean rectify = true;
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

        if (drawCircles) {
            drawCornerCircles(imageMat, red, green, blue, yellow, cyan, center, upperLeft, upperRight, lowerLeft, lowerRight);
        }

        imageTargetsActivity.putMatOnLiveImageView(imageMat);

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

            if (debugSomeSegments) {
                Mat scrabbleTile = ScrabbleBoardSegmenter.getScrabbleTile(imageMat, singleSegmentX, singleSegmentY, ScrabbleBoardMetrics.metricsFromImage(imageMat));
                performOCR(scrabbleTile, true);
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
                scanScrabbleboard(imageMat);
            }

            if (drawSegmentationLines) {
                imageMat = ScrabbleBoardSegmenter.drawSegmentationLines(imageMat);
            }


            Mat toDraw = imageMat;
            imageTargetsActivity.putMatOnProcessedImageView(imageMat);
            logElapsedTime("total computation took: ", tic);
        }
        imageMat.release();

    }


    private void scanScrabbleboard(Mat image) {
        long tic = System.currentTimeMillis();
        final ScrabbleBoardMetrics scrabbleBoardMetrics = ScrabbleBoardMetrics.metricsFromImage(image);
        for (int horizontalIdx = 0; horizontalIdx < 16; horizontalIdx++) {
            for (int verticalIdx = 0; verticalIdx < 16; verticalIdx++) {
                final Mat scrabbleTile = ScrabbleBoardSegmenter.getScrabbleTile(image, horizontalIdx, verticalIdx, scrabbleBoardMetrics);
                OCRResult ocrResult = performOCR(scrabbleTile, false);
                if (ocrResult.hasLetterBeenDetected()) {
                    Log.i(LOGTAG_OCR, "tile at (" + horizontalIdx + "," + verticalIdx + "): " + ocrResult);
                }
            }
        }
        Log.d(LOGTAG_OCR, "scanScrabbleBoard in " + (System.currentTimeMillis() - tic) + "ms");

    }

    public OCRResult performOCR(Mat scrabbleTile, boolean debug) {
        TileImage processedTile = enhanceTileImage(scrabbleTile, debug);
        OCRResult ocrResult;
        if (processedTile.isScrabbleTileProbable()) {
            if (debug) {
                imageTargetsActivity.putMatOnSegmentImageView(processedTile.getTileImage());
                imageTargetsActivity.setOCRTextView("");
            }

            ocrResult = callTeseract(processedTile.getTileImage());
        } else {
            ocrResult = OCRResult.createNoLetterFound();
        }
        if (debug) {
            String character;
            if (!ocrResult.hasLetterBeenDetected()) {
                character = "n/a";
            } else {
                character = ocrResult.getLetter();
            }
            imageTargetsActivity.setOCRTextView("(" + singleSegmentX + "," + singleSegmentY + ")=" + character);
        }
        return ocrResult;
    }

    public OCRResult callTeseract(Mat processedTile) {
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

            return OCRResult.createForLetterFound(textResult);
        } catch (Exception x) {
            Log.e(LOGTAG_OCR, "error", x);
            return OCRResult.createNoLetterFound();
        }
    }

    public TileImage enhanceTileImage(Mat scrabbleTile, boolean debug) {
        int pixelsToRemove = 3;
        Mat cropped = scrabbleTile.submat(pixelsToRemove, scrabbleTile.rows() - pixelsToRemove, pixelsToRemove, scrabbleTile.cols() - pixelsToRemove);

        final Scalar meanColor = Core.mean(cropped);

        final Mat grayscale = new Mat();
        Imgproc.cvtColor(cropped, grayscale, Imgproc.COLOR_RGB2GRAY);

        final Scalar meanGrayscale = Core.mean(grayscale);
        imageTargetsActivity.setDivTextView((int) meanColor.val[0] + "," + (int) meanColor.val[1] + "," + (int) meanColor.val[2] + "\n" + (int) meanGrayscale.val[0]);

        if (meanGrayscale.val[0] < 200.f) {
            if (debug) {
                imageTargetsActivity.putMatOnSegmentImageView(grayscale);
            }
            return TileImage.createNoTilePresent();
        }

        final Mat threshold = new Mat();
        Imgproc.threshold(grayscale, threshold, 200, 255, 0);
        return TileImage.createTilePresent(threshold);
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

}
