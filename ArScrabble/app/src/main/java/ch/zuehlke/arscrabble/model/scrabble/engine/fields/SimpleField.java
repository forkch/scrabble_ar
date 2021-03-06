package ch.zuehlke.arscrabble.model.scrabble.engine.fields;

import ch.zuehlke.arscrabble.model.scrabble.engine.Letter;
import ch.zuehlke.arscrabble.model.scrabble.engine.Stone;

/**
 * Created by chsueess on 25.11.14.
 */
public class SimpleField {
    private Stone stone;

    public int calculatePoints() {
        return getLetter().getPoints();
    }

    public int getWordPointsMultiplier() {
        return 1;
    }

    public String toString() {
        return getLetter() == null ? "|   " : "| " + getLetter() + " ";
    }

    public Letter getLetter() {
        return (stone == null) ? null : stone.getLetter();
    }

    public void setStone(Stone stone) {
        this.stone = stone;
    }

    public Stone getStone(){
        return stone;
    }

    public boolean hasStone() {
        return stone != null;
    }

}
