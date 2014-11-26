package ch.zuehlke.arscrabble;

import android.util.Log;

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
    private static int marginLeft = 44;
    private static int marginRight = 44;
    private static int marginTop = 20;
    private static int marginBottom = 80;

    private static int cellWidth = 42;
    private static int cellHeight = 42;

    public static Mat drawSegmentationLines(Mat image) {


        int currentX = marginLeft;
        int currentY = marginTop;
        for (int verticalIdx = 0; verticalIdx < 16; verticalIdx++) {
            Core.line(image, new Point(currentX, currentY), new Point(currentX, image.rows() - marginBottom), verticalLineColor, lineThickness);
            currentX += cellWidth;
        }

        currentX = marginLeft;
        for (int horizontalIdx = 0; horizontalIdx < 16; horizontalIdx++) {
            Core.line(image, new Point(currentX, currentY), new Point(image.cols() - marginRight, currentY), horizontalLineColor, lineThickness);
            currentY += cellHeight;
        }

        return image;

    }

    public static void segmentImage(Mat image) {

        for (int horizontalIdx = 0; horizontalIdx < 16; horizontalIdx++) {
            for (int verticalIdx = 0; verticalIdx < 16; verticalIdx++) {
                getScrabbleTile(image, horizontalIdx, verticalIdx);
            }
        }
    }

    public static Mat getScrabbleTile(Mat image, int horizontalIdx, int verticalIdx) {
        int rowStart = (verticalIdx*cellHeight)+marginTop;
        int colStart = (horizontalIdx * cellWidth)+marginLeft;
        final Mat scrabbleTile = image.submat(rowStart, rowStart + cellHeight, colStart, colStart + cellWidth);
        return scrabbleTile;
    }

}
