package ch.zuehlke.arscrabble.model.fields;

import ch.zuehlke.arscrabble.model.Letter;

/**
 * Created by chsueess on 25.11.14.
 */
public class SimpleField {
    private Letter letter;

    public int calculatePoints() {
        return letter.getPoints();
    }

    public int getWordPointsMultiplier() {
        return 1;
    }

    public void paint() {
        System.out.print(letter == null ? "x" : letter);
    }

    public Letter getLetter() {
        return letter;
    }

    public void setLetter(Letter letter) {
        this.letter = letter;
    }

}
