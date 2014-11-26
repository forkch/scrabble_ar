package ch.zuehlke.arscrabble;

/**
 * Created with love by fork on 26.11.14.
 */
public class OCRResult {
    public String getLetter() {
        return letter;
    }

    public int getConfidence() {
        return confidence;
    }

    private final String letter;
    private final int confidence;

    public OCRResult(String letter, int confidence) {

        this.letter = letter;
        this.confidence = confidence;
    }
}
