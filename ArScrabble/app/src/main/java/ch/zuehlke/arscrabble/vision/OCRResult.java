package ch.zuehlke.arscrabble.vision;

/**
 * Created with love by fork on 26.11.14.
 */
public class OCRResult {

    private final String letter;
    private final boolean letterBeenDetected;
    private final int confidence;

    public static OCRResult createNoLetterFound() {
        return new OCRResult("", false, 0);
    }

    public static OCRResult createForLetterFound(String letter, int confidence) {
        return new OCRResult(letter, true, confidence);
    }

    private OCRResult(String letter, boolean letterBeenDetected, int confidence) {
        this.letter = letter;
        this.letterBeenDetected = letterBeenDetected;
        this.confidence = confidence;
    }

    public String getLetter() {
        return letter;
    }

    public boolean hasLetterBeenDetected() {
        return letterBeenDetected;
    }

    @Override
    public String toString() {
        return letterBeenDetected ? letter + " (" + confidence + ")" : "n/a";
    }

}
