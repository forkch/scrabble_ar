package ch.zuehlke.arscrabble.vision;

import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;

import ch.zuehlke.arscrabble.EnhancedTileImage;

import static org.opencv.core.Core.addWeighted;
import static org.opencv.core.Core.circle;
import static org.opencv.core.Core.mean;
import static org.opencv.core.Core.rectangle;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.threshold;

/**
 * Created with love by fork on 27.11.14.
 */
public class ScrabbleTileProcessor {
    private static final String LOGTAG = ScrabbleTileProcessor.class.getSimpleName();
    private static final String LOGTAG_OCR = LOGTAG + "(OCR)";
    private static final int MINIMAL_OCR_CONFIDENCE = 70;
    public static final int MAX_CENTROID_DISTANCE = 10;
    public static final int MAX_CONTOUR_AREA = 500;
    public static final int MIN_CONTOUR_AREA = 50;

    private final TessBaseAPI tessBaseAPI;
    private BoardDetection.BoardDetectionDebugCallback boardDetectionDebugCallback;

    public ScrabbleTileProcessor(TessBaseAPI tessBaseAPI, BoardDetection.BoardDetectionDebugCallback boardDetectionDebugCallback) {
        this.tessBaseAPI = tessBaseAPI;
        this.boardDetectionDebugCallback = boardDetectionDebugCallback;
    }

    public char[][] scanScrabbleboard(Mat image, ScrabbleBoardSegmenter scrabbleBoardSegmenter) {
        long tic = System.currentTimeMillis();
        String allLetters = "";
        char[][] lettersOnBoard = new char[15][15];
        for (int verticalIdx = 0; verticalIdx < 15; verticalIdx++) {
            for (int horizontalIdx = 0; horizontalIdx < 15; horizontalIdx++) {
                final Mat scrabbleTile = scrabbleBoardSegmenter.getScrabbleTile(image, horizontalIdx, verticalIdx);
                lettersOnBoard[verticalIdx][horizontalIdx] = 0;
                EnhancedTileImage processedTile = enhanceTileImage(scrabbleTile);
                if (!processedTile.isScrabbleTileProbable()) {
                    continue;
                }

                final CroppedTileAndBoundingBox croppedTileAndBoundingBox = cropTileImageByContourOfLargestBlob(processedTile.getTileImage());
                if (croppedTileAndBoundingBox.foundGoodCandidates()) {
                    OCRResult ocrResult = performOCR(croppedTileAndBoundingBox.getCroppedTile());
                    if (ocrResult.hasLetterBeenDetected()) {
                        Log.i(LOGTAG_OCR, "tile at (" + horizontalIdx + "," + verticalIdx + "): " + ocrResult);
                        allLetters += ocrResult.getLetter();
                        lettersOnBoard[verticalIdx][horizontalIdx] = ocrResult.getLetter().charAt(0);
                    }
                }
            }
        }
        boardDetectionDebugCallback.setDivTextView(allLetters);
        Log.d(LOGTAG_OCR, "scanned ScrabbleBoard in " + (System.currentTimeMillis() - tic) + "ms");
        return lettersOnBoard;
    }


    public void oneTileDebug(Mat imageMat, ScrabbleBoardSegmenter scrabbleBoardSegmenter, int x, int y) {
        boardDetectionDebugCallback.setOCRTextView("");
        boardDetectionDebugCallback.setDivTextView("");

        boardDetectionDebugCallback.putMatOnSegment1ImageView(null);
        boardDetectionDebugCallback.putMatOnSegment2ImageView(null);

        Mat scrabbleTile = scrabbleBoardSegmenter.getScrabbleTile(imageMat, x, y);
        final EnhancedTileImage tileImage = enhanceTileImage(scrabbleTile);
        if (tileImage.isScrabbleTileProbable()) {
            final Mat thresholdedTileImage = tileImage.getTileImage();

            final CroppedTileAndBoundingBox croppedTileAndBoundingBox = cropTileImageByContourOfLargestBlob(thresholdedTileImage);
            final Rect bestBoundingBoxCandidate = croppedTileAndBoundingBox.getBoundingBoxInInputImage();

            if (croppedTileAndBoundingBox.foundGoodCandidates()) {
                for (Rect r : croppedTileAndBoundingBox.getBoundingBoxes()) {
                    rectangle(scrabbleTile, r.tl(), r.br(), new Scalar(255, 0, 0));
                    circle(scrabbleTile, new Point(r.x + r.width / 2, r.y + r.height / 2), 1, new Scalar(255, 0, 0));
                }
                rectangle(scrabbleTile, bestBoundingBoxCandidate.tl(), bestBoundingBoxCandidate.br(), new Scalar(0, 255, 0));
                circle(scrabbleTile, new Point(scrabbleTile.cols() / 2, scrabbleTile.rows() / 2), 1, new Scalar(0, 0, 255));

                boardDetectionDebugCallback.putMatOnSegment2ImageView(croppedTileAndBoundingBox.getCroppedTile());

                performOCRDebug(croppedTileAndBoundingBox.getCroppedTile(), x, y);
            }
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
        findContours(dilatedInput, contours, mHierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) {
            return new CroppedTileAndBoundingBox(inputImage, new Rect(0, 0, inputImage.cols(), inputImage.rows()), new MatOfPoint(), new ArrayList<Rect>());
        }
        Point centerOfImage = new Point(inputImage.cols() / 2, inputImage.rows() / 2);
        List<Rect> boundingRects = new ArrayList<Rect>();
        // Find max contour area
        double maxArea = 0;
        MatOfPoint bestContour = null;
        double currentMinDistance = MAX_CENTROID_DISTANCE;
        for (MatOfPoint contour : contours) {

            double area = contourArea(contour);
            maxArea = area;

            final Rect boundingBox = boundingRect(contour);
            //Log.i(LOGTAG, "" + boundingBox.area());
            if (boundingBox.area() > MAX_CONTOUR_AREA || boundingBox.area() < MIN_CONTOUR_AREA) {
                continue;
            }
            boundingRects.add(boundingBox);

            Point centerOfBoundingBox = new Point((boundingBox.x + boundingBox.width / 2), (boundingBox.y + boundingBox.height / 2));

            double dist_x_pow = Math.pow(Math.abs(centerOfBoundingBox.x - centerOfImage.x), 2);
            double dist_y_pow = Math.pow(Math.abs(centerOfBoundingBox.y - centerOfImage.y), 2);
            double distance = Math.sqrt(dist_x_pow + dist_y_pow);

            if (distance < currentMinDistance) {
                currentMinDistance = distance;
                bestContour = contour;
            }
        }

        if (bestContour == null) {
            return new CroppedTileAndBoundingBox(inputImage, new Rect(0, 0, inputImage.cols(), inputImage.rows()), new MatOfPoint(), new ArrayList<Rect>());
        }

        final Rect boundingBoxInInputImage = boundingRect(bestContour);

        return new CroppedTileAndBoundingBox(inputImage.submat(boundingBoxInInputImage), boundingBoxInInputImage, bestContour, boundingRects);
    }

    private EnhancedTileImage enhanceTileImage(Mat scrabbleTile) {

        final Mat grayscale = new Mat();
        cvtColor(scrabbleTile, grayscale, COLOR_RGB2GRAY);

        Mat blur = new Mat();
        Mat sharpened = new Mat();
        GaussianBlur(grayscale, blur, new Size(0, 0), 3);
        addWeighted(grayscale, 1.5, blur, -0.5, 0, sharpened);

        final Scalar meanGrayscale = mean(sharpened);

//        if (meanGrayscale.val[0] < 180) {
//            return EnhancedTileImage.createNoTilePresent();
//        }

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

            if (textResult.length() == 0 || confidence < MINIMAL_OCR_CONFIDENCE) {
                return OCRResult.createNoLetterFound();
            }

            return OCRResult.createForLetterFound(textResult, confidence);
        } catch (Exception x) {
            Log.e(LOGTAG_OCR, "error", x);
            return OCRResult.createNoLetterFound();
        }
    }


}
