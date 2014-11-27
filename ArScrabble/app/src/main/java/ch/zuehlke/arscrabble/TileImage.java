package ch.zuehlke.arscrabble;

import org.opencv.core.Mat;

/**
 * Created with love by fork on 27.11.14.
 */
public class TileImage {

    private final Mat tileImage;
    private final boolean scrabbleTileProbable;

    public static TileImage createTilePresent(Mat tileImage) {
        return new TileImage(tileImage, true);
    }

    public static TileImage createNoTilePresent() {
        return new TileImage(new Mat(), false);
    }

    private TileImage(Mat tileImage, boolean scrabbleTileProbable) {
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
