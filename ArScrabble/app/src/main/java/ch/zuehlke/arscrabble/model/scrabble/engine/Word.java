package ch.zuehlke.arscrabble.model.scrabble.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chsueess on 25.11.14.
 */
public class Word {
    private int x;
    private int y;
    private Direction direction;
    private List<Letter> letters = new ArrayList<Letter>();

    public Word(int x, int y, Direction direction, Letter... letters) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.letters.addAll(Arrays.asList(letters));
    }
    public ch.zuehlke.arscrabble.model.scrabble.engine.Direction getDirection() {
        return direction;
    }
    public int getX() {
        return x;
    }

    public List<Letter> getLetters() {
        return letters;
    }


    public int getY() {
        return y;
    }


    public int size() {
        return this.letters.size();
    }
}