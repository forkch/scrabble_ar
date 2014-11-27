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
}
