package ch.zuehlke.arscrabble.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chsueess on 25.11.14.
 */
public class Word {
    List<Letter> letters = new ArrayList<Letter>();

    public void addLetter(Letter letter) {
        letters.add(letter);
    }
}
