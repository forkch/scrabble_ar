package ch.zuehlke.arscrabble.vision;

/**
 * Created with love by fork on 27.11.14.
 */
public class BoardVisionResult {

    private final boolean scanSuccessful;
    private final char[][] lettersOnBoard;


    public BoardVisionResult(boolean scanSuccessful, char[][] lettersOnBoard) {
        this.scanSuccessful = scanSuccessful;
        this.lettersOnBoard = lettersOnBoard;
    }

    public boolean isScanSuccessful() {
        return scanSuccessful;
    }

    public char[][] getLettersOnBoard() {
        return lettersOnBoard;
    }
}
