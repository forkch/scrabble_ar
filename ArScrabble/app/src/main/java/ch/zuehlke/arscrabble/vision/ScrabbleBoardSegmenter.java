package ch.zuehlke.arscrabble.vision;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

/**
 * Created with love by fork on 25.11.14.
 */
public class ScrabbleBoardSegmenter {

    private static final String LOGTAG = ScrabbleBoardSegmenter.class.getSimpleName();

    private static Scalar boundary = new Scalar(255, 255, 0);
    private static Scalar horizontalLineColor = new Scalar(255, 0, 0);
    private static Scalar verticalLineColor = new Scalar(0, 255, 0);
    private static int lineThickness = 2;

    private ScrabbleBoardMetrics metrics;

    public ScrabbleBoardSegmenter(Mat image) {
        this.metrics = ScrabbleBoardMetrics.metricsFromImage(image);
    }


    public Mat drawSegmentationLines(Mat image) {

        Core.rectangle(image,
                new Point(metrics.getMarginLeft(), metrics.getMarginTop()),
                new Point(image.rows() - metrics.getMarginRight(), image.cols() - metrics.getMarginBottom()),
                boundary, lineThickness);

        for (int verticalIdx = 0; verticalIdx < 16; verticalIdx++) {
            float x = metrics.getX(verticalIdx);
            Core.line(image,
                    new Point(x, metrics.getMarginTop()),
                    new Point(x, image.rows() - metrics.getMarginBottom()),
                    verticalLineColor, lineThickness);
        }

        for (int horizontalIdx = 0; horizontalIdx < 16; horizontalIdx++) {
            float y = metrics.getY(horizontalIdx);
            Core.line(image,
                    new Point(metrics.getMarginLeft(), y),
                    new Point(image.cols() - metrics.getMarginRight(), y),
                    horizontalLineColor, lineThickness);
        }

        return image;

    }

    public Mat getScrabbleTile(Mat image, int horizontalIdx, int verticalIdx) {

        int additionalPixels = 5;
        int x1 = (int) metrics.getX(horizontalIdx) - additionalPixels;
        int y1 = (int) metrics.getY(verticalIdx) - additionalPixels;

        int x2 = (int) metrics.getX(horizontalIdx + 1) + additionalPixels;
        int y2 = (int) metrics.getY(verticalIdx + 1) + additionalPixels;

        final Mat scrabbleTile = new Mat();

        image.submat(y1, y2, x1, x2).copyTo(scrabbleTile);

        //maskPointNumber(scrabbleTile);

        return scrabbleTile;
    }

    public void maskPointNumber(Mat scrabbleTile) {
        Point lowerRight = new Point(scrabbleTile.cols(), scrabbleTile.rows());
        Point upperRight = new Point(lowerRight.x - 11, lowerRight.y - 11);
        Core.rectangle(scrabbleTile, upperRight, lowerRight, new Scalar(255, 255, 255), -1);
    }



}
