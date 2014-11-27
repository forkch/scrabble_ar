package ch.zuehlke.arscrabble.model.scrabble.engine;

/**
 * Created by chsueess on 27.11.14.
 */
public class Stone {
    private Letter letter;

    public Stone(Letter letter) {
        this.letter = letter;
    }

    public Letter getLetter() {
        return letter;
    }
}
