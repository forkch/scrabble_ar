package ch.zuehlke.arscrabble;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

/**
 * Created with love by fork on 25.11.14.
 */
public class ScrabbleBoardSegmentator {

    private static final String LOGTAG = ScrabbleBoardSegmentator.class.getSimpleName();

    private static Scalar horizontalLineColor = new Scalar(255, 0, 0);
    private static Scalar verticalLineColor = new Scalar(0, 255, 0);
    private static int lineThickness = 4;


    public static Mat drawSegmentationLines(Mat image) {

        final ScrabbleBoardMetrics scrabbleBoardMetrics = ScrabbleBoardMetrics.metricsFromImage(image);
        int currentX = scrabbleBoardMetrics.getMarginLeft();
        int currentY = scrabbleBoardMetrics.getMarginTop();
        for (int verticalIdx = 0; verticalIdx < 16; verticalIdx++) {
            Core.line(image, new Point(currentX, currentY), new Point(currentX, image.rows() - scrabbleBoardMetrics.getMarginBottom()), verticalLineColor, lineThickness);
            currentX += scrabbleBoardMetrics.getCellWidth();
        }

        currentX = scrabbleBoardMetrics.getMarginLeft();
        for (int horizontalIdx = 0; horizontalIdx < 16; horizontalIdx++) {
            Core.line(image, new Point(currentX, currentY), new Point(image.cols() - scrabbleBoardMetrics.getMarginRight(), currentY), horizontalLineColor, lineThickness);
            currentY += scrabbleBoardMetrics.getCellHeight();
        }

        return image;

    }


    public static Mat getScrabbleTile(Mat image, int horizontalIdx, int verticalIdx, ScrabbleBoardMetrics scrabbleBoardMetrics) {

        int rowStart = (verticalIdx * scrabbleBoardMetrics.getCellHeight()) + scrabbleBoardMetrics.getMarginTop();
        int colStart = (horizontalIdx * scrabbleBoardMetrics.getCellWidth()) + scrabbleBoardMetrics.getMarginLeft();

        final Mat scrabbleTile = new Mat();

        image.submat(rowStart, rowStart + scrabbleBoardMetrics.getCellHeight(), colStart, colStart + scrabbleBoardMetrics.getCellWidth()).copyTo(scrabbleTile);

        Point lowerRight = new Point(scrabbleTile.cols(), scrabbleTile.rows());
        Point upperRight = new Point(lowerRight.x - 11, lowerRight.y - 11);
        Core.rectangle(scrabbleTile, upperRight, lowerRight, new Scalar(255, 255, 255), -1);

        return scrabbleTile;
    }

}
