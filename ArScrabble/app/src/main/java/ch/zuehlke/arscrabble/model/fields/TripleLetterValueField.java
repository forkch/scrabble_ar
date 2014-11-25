package ch.zuehlke.arscrabble.model.fields;

/**
 * Created by chsueess on 25.11.14.
 */
public class TripleLetterValueField extends SimpleField {

    public int calculatePoints() {
        return 3 * getLetter().getPoints();
    }

}
