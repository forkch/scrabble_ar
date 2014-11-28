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
            throw new ScrabbleException("Every rack has '" + MAX_NUMBER_OF_STONES + "' not '" + initialStones.size() + "'in the beginning...stupid!");
        }
        addStones(initialStones);
    }

    private void addStones(List<Stone> newStones) {
        for(Stone stone : newStones) {
            addStone(stone);
        }
    }

    public void addStone(Stone stone) {
        if(stones.size() + 1 > MAX_NUMBER_OF_STONES)
            throw new ScrabbleException("The rack has already '" + stones.size() + "' stones, another one is too much...dude!");
        stones.add(stone);
    }

    public Stone pop(Letter letter) {
        for (Stone stone : stones) {
            if(stone.getLetter().equals(letter)) {
                if(stones.remove(stone)) {
                    return stone;
                };
            }
        }
        return null;
    }

    public int getNumberOfMissingStones() {
        return MAX_NUMBER_OF_STONES - stones.size();
    }

    public List<Stone> getStones() {
        return stones;
    }

    public String toString() {
        String stringRack = "";
       for(Stone stone : stones) {
            stringRack += stone.getLetter() + " ";
        }
        return stringRack;
    }


    public boolean isFull() {
        return (stones.size() == MAX_NUMBER_OF_STONES);
    }

    public int size() {
        return stones.size();
    }
}
