package ch.zuehlke.arscrabble.model.scrabble.solver;

import ch.zuehlke.arscrabble.model.scrabble.engine.Stone;

/**
 * Created by chsueess on 27.11.14.
 */
public class VirtualStone {
    private Stone stone;
    private int x;
    private int y;

    public VirtualStone(Stone stone, int x, int y) {
        this.stone = stone;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Stone getStone() {
        return stone;
    }

    public int hashCode() {
        return (x + "_" + y + "_" + stone.getLetter()).hashCode();
    }

    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj instanceof VirtualStone) {
            VirtualStone virtualStone = (VirtualStone) obj;
            return x == virtualStone.x && y == virtualStone.y && stone.getLetter() == virtualStone.stone.getLetter();
        }

        return false;
    }
}
