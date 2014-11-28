package ch.zuehlke.arscrabble.vision;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;

import java.util.List;

/**
 * Created with love by fork on 27.11.14.
 */
public class CroppedTileAndBoundingBox {
    private final Mat croppedTile;
    private final Rect boundingBoxInInputImage;
    private final MatOfPoint maxContour;
    private final List<Rect> boundingBoxes;

    public CroppedTileAndBoundingBox(Mat croppedTile, Rect boundingBoxInInputImage, MatOfPoint maxContour, List<Rect> boundingBoxes) {
        this.croppedTile = croppedTile;
        this.boundingBoxInInputImage = boundingBoxInInputImage;
        this.maxContour = maxContour;
        this.boundingBoxes = boundingBoxes;
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

    public List<Rect> getBoundingBoxes() {
        return boundingBoxes;
    }

    public boolean foundGoodCandidates() {
        return !boundingBoxes.isEmpty();
    }
}
