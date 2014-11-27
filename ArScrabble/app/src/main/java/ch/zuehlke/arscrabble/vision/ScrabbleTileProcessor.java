package ch.zuehlke.arscrabble.vision;

import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ch.zuehlke.arscrabble.EnhancedTileImage;

import static org.opencv.core.Core.addWeighted;
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
public class ScrabbleTileProcessor {
    private static final String LOGTAG = ScrabbleTileProcessor.class.getSimpleName();
    private static final String LOGTAG_OCR = LOGTAG + "(OCR)";
    private static final int MINIMAL_OCR_CONFIDENCE = 70;

    private final TessBaseAPI tessBaseAPI;
    private BoardDetection.BoardDetectionDebugCallback boardDetectionDebugCallback;

    public ScrabbleTileProcessor(TessBaseAPI tessBaseAPI, BoardDetection.BoardDetectionDebugCallback boardDetectionDebugCallback) {
        this.tessBaseAPI = tessBaseAPI;
        this.boardDetectionDebugCallback = boardDetectionDebugCallback;
    }

    public void scanScrabbleboard(Mat image, ScrabbleBoardSegmenter scrabbleBoardSegmenter) {
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

                OCRResult ocrResult = performOCR(croppedTileAndBoundingBox.getCroppedTile());
                if (ocrResult.hasLetterBeenDetected()) {
                    Log.i(LOGTAG_OCR, "tile at (" + horizontalIdx + "," + verticalIdx + "): " + ocrResult);
                    allLetters += ocrResult.getLetter();
                }
            }
        }
        boardDetectionDebugCallback.setDivTextView(allLetters);
        Log.d(LOGTAG_OCR, "scanScrabbleBoard in " + (System.currentTimeMillis() - tic) + "ms");

    }


    public void oneTileDebug(Mat imageMat, ScrabbleBoardSegmenter scrabbleBoardSegmenter, int x, int y) {
        boardDetectionDebugCallback.setOCRTextView("");
        boardDetectionDebugCallback.setDivTextView("");
        Mat scrabbleTile = scrabbleBoardSegmenter.getScrabbleTile(imageMat, x, y);
        final EnhancedTileImage tileImage = enhanceTileImage(scrabbleTile, false);
        if (tileImage.isScrabbleTileProbable()) {
            final Mat thresholdedTileImage = tileImage.getTileImage();

            final CroppedTileAndBoundingBox croppedTileAndBoundingBox = cropTileImageByContourOfLargestBlob(thresholdedTileImage);
            final Rect boundingBoxInInputImage = croppedTileAndBoundingBox.getBoundingBoxInInputImage();

            drawContours(scrabbleTile, Arrays.asList(croppedTileAndBoundingBox.getMaxContour()), -1, new Scalar(0, 255, 0));
            rectangle(scrabbleTile, boundingBoxInInputImage.tl(), boundingBoxInInputImage.br(), new Scalar(255, 0, 0));

            boardDetectionDebugCallback.putMatOnSegment2ImageView(croppedTileAndBoundingBox.getCroppedTile());

            performOCRDebug(croppedTileAndBoundingBox.getCroppedTile(), x, y);
        } else {
            boardDetectionDebugCallback.putMatOnSegment2ImageView(imageMat);
        }
        boardDetectionDebugCallback.putMatOnSegment1ImageView(scrabbleTile);

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

    private OCRResult performOCR(Mat processedTile) {
        OCRResult ocrResult;

        ocrResult = callTeseract(processedTile);
        return ocrResult;
    }

    private OCRResult performOCRDebug(Mat processedTile, int x, int y) {
        OCRResult ocrResult;

        ocrResult = callTeseract(processedTile);
        boardDetectionDebugCallback.setOCRTextView("(" + x + "," + y + ")=" + ocrResult.toString());
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


}