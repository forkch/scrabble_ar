package ch.zuehlke.arscrabble.model.scrabble.engine;

/**
 * Created by chsueess on 27.11.14.
 */
public class StoneBag {
    // TODO Limit stones to the real amount of each!
    public Stone pop(Letter letter) {
        return new Stone(letter);
    }
}