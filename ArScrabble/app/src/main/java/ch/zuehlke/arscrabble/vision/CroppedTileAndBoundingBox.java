package ch.zuehlke.arscrabble.vision;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;

/**
 * Created with love by fork on 27.11.14.
 */
public class CroppedTileAndBoundingBox {
    private final Mat croppedTile;
    private final Rect boundingBoxInInputImage;
    private final MatOfPoint maxContour;

    public CroppedTileAndBoundingBox(Mat croppedTile, Rect boundingBoxInInputImage, MatOfPoint maxContour) {
        this.croppedTile = croppedTile;
        this.boundingBoxInInputImage = boundingBoxInInputImage;
        this.maxContour = maxContour;
    }

    public Mat getCroppedTile() {
        return croppedTile;
    }

    public Rect getBoundingBoxInInputImage() {
        return boundingBoxInInputImage;
    }

    public MatOfPoint getMaxContour() {
        return maxContour;
    }
}
