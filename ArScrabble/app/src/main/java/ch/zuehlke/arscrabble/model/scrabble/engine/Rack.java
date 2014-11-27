package ch.zuehlke.arscrabble.model.scrabble.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chsueess on 27.11.14.
 */
public class Rack {
    private static final int MAX_NUMBER_OF_STONES = 7;

    List<Stone> stones = new ArrayList<Stone>();

    public Rack(List<Stone> initialStones) {
        if(initialStones == null || initialStones.size() != MAX_NUMBER_OF_STONES) {
            throw new RuntimeException("Every rack has '" + MAX_NUMBER_OF_STONES + "' not '" + initialStones.size() + "'in the beginning...stupid!");
        }
        addStones(initialStones);
    }

    public void addStones(List<Stone> newStones) {
        if(newStones.size() > getNumberOfMissingStones())
            throw new RuntimeException("The rack has already '" + stones.size() + "' stones, another '" + newStones.size() + "' are too much...dude!");
        stones.addAll(newStones);
    }

    public int getNumberOfMissingStones() {
        return MAX_NUMBER_OF_STONES - stones.size();
    }

    public List<Stone> getStones() {
        return stones;
    }
}
