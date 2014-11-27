package ch.zuehlke.arscrabble.model.scrabble.engine.fields;

/**
 * Created by chsueess on 25.11.14.
 */
public class TripleWordValueField extends SimpleField {
    @Override
    public int getWordPointsMultiplier() {
        return 3;
    }
}
