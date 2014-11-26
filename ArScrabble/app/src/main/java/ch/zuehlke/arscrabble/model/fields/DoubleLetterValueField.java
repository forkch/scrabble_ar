package ch.zuehlke.arscrabble.model.fields;

/**
 * Created by chsueess on 25.11.14.
 */
public class DoubleLetterValueField extends SimpleField {

    public int calculatePoints() {
        return 2 * getLetter().getPoints();
    }

}
