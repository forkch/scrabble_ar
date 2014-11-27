package ch.zuehlke.arscrabble.model.scrabble.engine.fields;

/**
 * Created by chsueess on 25.11.14.
 */
public class TripleLetterValueField extends SimpleField {

    public int calculatePoints() {
        return 3 * getLetter().getPoints();
    }

}
