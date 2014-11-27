package ch.zuehlke.arscrabble.model.scrabble.engine;

/**
 * Created by chsueess on 27.11.14.
 */
public class TurnStep {
    private int x;
    private int y;
    private Stone stone;

    public TurnStep(int x, int y, Stone stone) {
        this.x = x;
        this.y = y;
        this.stone = stone;
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
}
