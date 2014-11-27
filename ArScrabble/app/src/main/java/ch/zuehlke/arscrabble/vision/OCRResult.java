package ch.zuehlke.arscrabble.vision;

/**
 * Created with love by fork on 26.11.14.
 */
public class OCRResult {

    private final String letter;
    private final boolean letterBeenDetected;

    public static OCRResult createNoLetterFound() {
        return new OCRResult("", false);
    }

    public static OCRResult createForLetterFound(String letter) {
        return new OCRResult(letter, true);
    }

    private OCRResult(String letter, boolean letterBeenDetected) {
        this.letter = letter;
        this.letterBeenDetected = letterBeenDetected;
    }

    public String getLetter() {
        return letter;
    }

    public boolean hasLetterBeenDetected() {
        return letterBeenDetected;
    }

    @Override
    public String toString() {
        return letterBeenDetected ? letter : "n/a";
    }

}
