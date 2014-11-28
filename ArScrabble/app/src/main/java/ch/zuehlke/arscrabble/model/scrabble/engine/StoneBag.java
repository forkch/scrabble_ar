package ch.zuehlke.arscrabble.model.scrabble.engine;

/**
 * Created by chsueess on 27.11.14.
 */
public class StoneBag {
    // TODO Limit stones to the real amount of each!
    public Stone pop(Letter letter) {
        if(!hasStones()) {
            throw new ScrabbleException("The bag is empty... start crying!");
        }
        return new Stone(letter);
    }

    public boolean hasStones() {
        return true;
    }


}