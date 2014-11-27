package ch.zuehlke.arscrabble;

import org.opencv.core.Mat;

/**
 * Created with love by fork on 27.11.14.
 */
public class EnhancedTileImage {

    private final Mat tileImage;
    private final boolean scrabbleTileProbable;

    public static EnhancedTileImage createTilePresent(Mat tileImage) {
        return new EnhancedTileImage(tileImage, true);
    }

    public static EnhancedTileImage createNoTilePresent() {
        return new EnhancedTileImage(new Mat(), false);
    }

    private EnhancedTileImage(Mat tileImage, boolean scrabbleTileProbable) {
        this.tileImage = tileImage;
        this.scrabbleTileProbable = scrabbleTileProbable;
    }

    public Mat getTileImage() {
        return tileImage;
    }

    public boolean isScrabbleTileProbable() {
        return scrabbleTileProbable;
    }

}
