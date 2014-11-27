package ch.zuehlke.arscrabble.model.fields;

import ch.zuehlke.arscrabble.model.Letter;
import ch.zuehlke.arscrabble.model.Stone;
import ch.zuehlke.arscrabble.model.StoneType;

/**
 * Created by chsueess on 25.11.14.
 */
public class SimpleField {
    private Stone stone;
    private StoneType stoneType;

    public int calculatePoints() {
        return getLetter().getPoints();
    }

    public int getWordPointsMultiplier() {
        return 1;
    }

    public void paint() {
        System.out.print(getLetter() == null ? "x" : getLetter());
    }

    public Letter getLetter() {
        return (stone == null) ? null : stone.getLetter();
    }

    public void setStone(Stone stone) {
        this.stone = stone;
    }

}
