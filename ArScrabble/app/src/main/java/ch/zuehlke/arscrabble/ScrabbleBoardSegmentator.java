package ch.zuehlke.arscrabble;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created with love by fork on 25.11.14.
 */
public class ScrabbleBoardSegmentator {

    private static Scalar lineColor = new Scalar(255, 0, 0);
    private static int lineThickness = 2;

    public static Mat segment(Mat image) {
        int xOffset = 44;
        int yOffset = 20;

        int cellWidth = 42;
        int cellHeight = 40;

        int currentX = xOffset;
        int currentY = yOffset;
        for(int vertIdx = 0; vertIdx < 16; vertIdx++) {
            Core.line(image, new Point(currentX, currentY), new Point(currentX, image.rows()), lineColor, lineThickness);
            currentX += cellWidth;
        }

        return image;

    }

    private static void houghTransform(Mat image, Mat canny) {
        MatOfPoint2f lines = new MatOfPoint2f();
        Imgproc.HoughLines(canny, lines, 1, Math.PI / 180, 2, 0, 0);
        final Point[] linesArray = lines.toArray();
        for (int i = 0; i < linesArray.length; i++) {
            double rho = linesArray[i].x;
            double theta = linesArray[i].y;
            Point pt1 = new Point();
            Point pt2 = new Point();
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;
            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * (a));
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * (a));
            Core.line(image, pt1, pt2, lineColor, lineThickness);
        }
    }

}
