package ch.zuehlke.arscrabble;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

/**
 * Created with love by fork on 25.11.14.
 */
public class ScrabbleBoardSegmentator {

    private static Scalar horizontalLineColor = new Scalar(255, 0, 0);
    private static Scalar verticalLineColor = new Scalar(0,255, 0);
    private static int lineThickness = 4;

    public static Mat segment(Mat image) {
        int marginLeft = 44;
        int marginRight = 44;
        int marginTop = 20;
        int marginBottom = 80;

        int cellWidth = 42;
        int cellHeight = 42;

        int currentX = marginLeft;
        int currentY = marginTop;
        for(int verticalIdx = 0; verticalIdx < 16; verticalIdx++) {
            Core.line(image, new Point(currentX, currentY), new Point(currentX, image.rows()-marginBottom), verticalLineColor, lineThickness);
            currentX += cellWidth;
        }

        currentX = marginLeft;
        for(int horizontalIdx = 0; horizontalIdx < 16; horizontalIdx++) {
            Core.line(image, new Point(currentX, currentY), new Point(image.cols()-marginRight, currentY), horizontalLineColor, lineThickness);
            currentY += cellHeight;
        }

        return image;

    }

}
