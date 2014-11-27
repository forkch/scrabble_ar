package ch.zuehlke.arscrabble;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Removes the perspective distortion from an input image.
 */
public class RectifyAlgorithm {
    private static final String TAG = RectifyAlgorithm.class.getSimpleName();

    /**
     * @param inputMat The original image
     * @param corners  The corners that delimit our region of interest
     * @return
     */
    public static Mat rectifyToInputMat(Mat inputMat, Point[] corners) {

        Mat output = new Mat();
        if (!VectorUtils.allCornersVisible(corners, inputMat)) {
            return inputMat;
        }

        Point[] sortedPoints = sortPoints(corners);

        // perform rectification only within boundingbox of corner points
        Rect boundingBox = Imgproc.boundingRect(new MatOfPoint(sortedPoints));
        inputMat = inputMat.submat(boundingBox);

        for (Point sortedPoint : sortedPoints) {
            sortedPoint.x = sortedPoint.x - boundingBox.x;
            sortedPoint.y = sortedPoint.y - boundingBox.y;
        }

        MatOfPoint2f srcPoints = new MatOfPoint2f(sortedPoints);

        float size = Math.min(inputMat.cols(), inputMat.rows());
        MatOfPoint2f destPoints = new MatOfPoint2f(new Point(0, 0), // Top left
                new Point(size, 0), // Top right
                new Point(0, size), // Bottom left
                new Point(size, size)); // Bottom right


        long tic = System.currentTimeMillis();
        Mat transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, destPoints);
        Log.d(TAG, "forming matrix: " + (System.currentTimeMillis() - tic) + " ms");
        Imgproc.warpPerspective(inputMat, output, transformMatrix, inputMat.size());
        Log.d(TAG, "warping: " + (System.currentTimeMillis() - tic) + " ms");

        // Crop region
        Rect rect = new Rect(destPoints.toArray()[0], destPoints.toArray()[3]);

        output = output.submat(rect);

        return output;
    }

    /**
     * Reconstructs the points that may have been rotated to the points in the original image.
     *
     * @param source   Points that have been rotated
     * @param original Size of input image
     * @param angle    Angle of rotation [degree]
     * @return Points in original image (rotated by the inverse angle)
     */
    private static MatOfPoint2f reconstuctPointInOriginalImage(Point[] source, Size original, int angle) {
        // Calculate size of rotated image
        Size rotatedImage = original;
        if (angle != 0 && angle != 180 && angle != -180) {
            rotatedImage = new Size(original.height, original.width);
        }

        Point pivotRotatedPagCorners = new Point(rotatedImage.width / 2, rotatedImage.height / 2);
        Point pivotOriginalPageCorners = new Point(original.width / 2, original.height / 2);

        double transX = pivotOriginalPageCorners.x - pivotRotatedPagCorners.x;
        double transY = pivotOriginalPageCorners.y - pivotRotatedPagCorners.y;

        Point[] originalCorners = new Point[source.length];
        for (int i = 0; i < originalCorners.length; i++) {
            originalCorners[i] = rotatePoint(source[i], pivotRotatedPagCorners, -1 * angle);
            originalCorners[i] = translatePoint(originalCorners[i], transX, transY);
        }

        return new MatOfPoint2f(originalCorners);
    }

    /**
     * Sorts the corners so that 1. top left 2. top right 3. bottom left 4. bottom right.
     *
     * @param corners Corners
     * @return Sorted array of corners
     */
    private static Point[] sortPoints(Point[] corners) {
        // Calculate Bounding box. Calculate distance to the corners.
        // Choose point with min dist to corner.
        Rect boundingBox = Imgproc.boundingRect(new MatOfPoint(corners));
        Point topLeft = new Point(boundingBox.x, boundingBox.y);
        Point topRight = new Point(boundingBox.x + boundingBox.width, boundingBox.y);
        Point bottomLeft = new Point(boundingBox.x, boundingBox.y + boundingBox.height);
        Point bottomRight = new Point(boundingBox.x + boundingBox.width, boundingBox.y + boundingBox.height);

        Point[] bb = {topLeft, topRight, bottomLeft, bottomRight};
        Point[] result = new Point[corners.length];
        for (int i = 0; i < corners.length; i++) {
            Point min = null;
            Point p = bb[i];
            double minDist = boundingBox.width;
            for (Point c : corners) {
                double dist = distanceBetween(c, p);
                if (minDist > dist) {
                    minDist = dist;
                    min = c;
                }

            }
            result[i] = min;
        }
        return result;
    }

    /**
     * Calculates the euclidean distance between two points
     *
     * @param p1 Point 1
     * @param p2 Point 2
     * @return Euclidean distance between the points.
     */
    private static double distanceBetween(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     * Calculates the destination point matrix for a given width height ratio of the original document.
     *
     * @param src              The source point matrix.
     * @param widthHeightRatio The width height ratio of the original image
     * @return The destination point matrix.
     */
    private static MatOfPoint2f calculateDestMatrix(MatOfPoint2f src, double widthHeightRatio) {
        Point[] points = src.toArray();

        Rect boundingBox = Imgproc.boundingRect(new MatOfPoint(points));
        boundingBox.height = boundingBox.height - 1;
        boundingBox.width = boundingBox.width - 1;
        int width = boundingBox.width, height;

        if (widthHeightRatio > 0.01) {
            height = (int) Math.floor(width * widthHeightRatio); // Portrait

            return new MatOfPoint2f(new Point(boundingBox.x, boundingBox.y), // Top left
                    new Point(boundingBox.x + width, boundingBox.y), // Top right
                    new Point(boundingBox.x, boundingBox.y + height), // Bottom left
                    new Point(boundingBox.x + width, boundingBox.y + height)); // Bottom right
        } else {
            // if width height ratio is almost 0 take bounding box
            return new MatOfPoint2f(new Point(boundingBox.x, boundingBox.y), // Top left
                    new Point(boundingBox.x + width, boundingBox.y), // Top right
                    new Point(boundingBox.x, boundingBox.y + boundingBox.height), // Bottom left
                    new Point(boundingBox.x + width, boundingBox.y + boundingBox.height)); // Bottom right
        }
    }

    /**
     * Calculates the size of the output matrix.
     *
     * @param dest  Destination points.
     * @param input Size of the input image.
     * @return Size of the output image.
     */
    private static Size calculateOutputSize(MatOfPoint2f dest, Size input) {
        Size output = input.clone();

        Rect boundingBox = Imgproc.boundingRect(new MatOfPoint(dest.toArray()));
        boundingBox.height = boundingBox.height - 1;
        boundingBox.width = boundingBox.width - 1;

        int rightMost, bottomMost;

        if (boundingBox.width < boundingBox.height) {
            rightMost = boundingBox.x + boundingBox.width;
            bottomMost = boundingBox.y + boundingBox.height;

        } else {
            rightMost = boundingBox.x + boundingBox.height;
            bottomMost = boundingBox.y + boundingBox.width;
        }

        if (input.height < bottomMost) {
            output.height = bottomMost;
        }
        if (input.width < rightMost) {
            output.width = rightMost;
        }

        return output;
    }

    private static Point rotatePoint(Point point, Point center, int angle) {
        double angleRad = angle * Math.PI / 180;
        double xRotated = center.x + (point.x - center.x) * Math.cos(angleRad) - (point.y - center.y) * Math.sin(angleRad);
        double yRotated = center.y + (point.x - center.x) * Math.sin(angleRad) + (point.y - center.y) * Math.cos(angleRad);

        return new Point(xRotated, yRotated);
    }

    private static Point translatePoint(Point point, double x, double y) {
        return new Point(point.x + x, point.y + y);
    }

    /**
     * Method used, that is described here
     * http://stackoverflow.com/questions/1194352/proportions-of-a-perspective-deformed-rectangle
     *
     * @param corners Corners that may have been adjusted by user
     * @param input   Input size of the image
     * @return Width/height ratio of the original image
     */
    private static double reconstructWithHeightRatio(Point[] corners, Size input) {
        // Calculate image center, principal point
        double u0 = input.width / 2;
        double v0 = input.height / 2;

        // Translate points so the principal point is at (0,0)
        double m1x = corners[0].x - u0;
        double m1y = corners[0].y - v0;
        double m2x = corners[1].x - u0;
        double m2y = corners[1].y - v0;
        double m3x = corners[2].x - u0;
        double m3y = corners[2].y - v0;
        double m4x = corners[3].x - u0;
        double m4y = corners[3].y - v0;

        double k2 = ((m1y - m4y) * m3x - (m1x - m4x) * m3y + m1x * m4y - m1y * m4x)
                / ((m2y - m4y) * m3x - (m2x - m4x) * m3y + m2x * m4y - m2y * m4x);
        double k3 = ((m1y - m4y) * m2x - (m1x - m4x) * m2y + m1x * m4y - m1y * m4x)
                / ((m3y - m4y) * m2x - (m3x - m4x) * m2y + m3x * m4y - m3y * m4x);

        // f_squared is the focal length of the camera, squared
        // if k2==1 OR k3==1 then this equation is not solvable
        // if the focal length is known, then this equation is not needed
        // in that case assign f_squared= sqr(focal_length)
        double fSquared = -((k3 * m3y - m1y) * (k2 * m2y - m1y) + (k3 * m3x - m1x) * (k2 * m2x - m1x))
                / ((k3 - 1) * (k2 - 1));

        // The width/height ratio of the original rectangle
        double whRatio = Math.sqrt(
                (Math.pow(k2 - 1, 2) + Math.pow(k2 * m2y - m1y, 2) / fSquared + Math.pow(k2 * m2x - m1x, 2) / fSquared)
                        / (Math.pow(k3 - 1, 2) + Math.pow(k3 * m3y - m1y, 2) / fSquared + Math.pow(k3 * m3x - m1x, 2) / fSquared)
        );

        double epsilon = 0.000001;
        // if k2==1 AND k3==1, then the focal length equation is not solvable
        // but the focal length is not needed to calculate the ratio.
        // I am still trying to figure out under which circumstances k2 and k3 become 1
        // but it seems to be when the rectangle is not distorted by perspective,
        // i.e. viewed straight on. Then the equation is obvious:
        if (Math.abs(k2 - 1) < epsilon && Math.abs(k3 - 1) < epsilon) {
            whRatio = Math.sqrt(
                    (Math.pow(m2y - m1y, 2) + Math.pow(m2x - m1x, 2))
                            / (Math.pow(m3y - m1y, 2) + Math.pow(m3x - m1x, 2)));
        }
        whRatio = 1 / whRatio;

        // Use input width height ratio if whRatio is infinity.
        if (Double.isInfinite(whRatio)) {
            whRatio = input.height / input.width;
        }
        return whRatio;
    }
}
