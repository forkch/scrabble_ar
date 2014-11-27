package ch.zuehlke.arscrabble.model.scrabble.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chsueess on 25.11.14.
 */
public class Word {
    List<Stone> stones = new ArrayList<Stone>();

    public Word(String wordString, StoneType stoneType) {
        for(char letter : wordString.toCharArray()) {
            addStone(new Stone(Letter.getLetterFor(letter), stoneType));
        }
    }

    public void addStone(Stone stone) {
        stones.add(stone);
    }


}
