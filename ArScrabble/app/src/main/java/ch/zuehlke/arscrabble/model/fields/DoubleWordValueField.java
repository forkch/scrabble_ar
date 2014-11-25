package ch.zuehlke.arscrabble.model.fields;

/**
 * Created by chsueess on 25.11.14.
 */
public class DoubleWordValueField extends SimpleField {
    @Override
    public int getWordPointsMultiplier() {
        return 2;
    }
}
