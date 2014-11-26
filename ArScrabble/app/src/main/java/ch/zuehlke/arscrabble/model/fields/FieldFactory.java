package ch.zuehlke.arscrabble.model.fields;

/**
 * Created by chsueess on 25.11.14.
 */
public class FieldFactory {

    public static final String SIMPLE_FIELD_IDENTIFIER = "  ";
    public static final String DOUBLE_WORD_VALUE_FIELD_IDENTIFIER = "2W";
    public static final String TRIPLE_WORD_VALUE_FIELD_IDENTIFIER = "3W";
    public static final String DOUBLE_LETTER_VALUE_FIELD_IDENTIFIER = "2L";
    public static final String TRIPLE_LETTER_VALUE_FIELD_IDENTIFIER = "3L";

    public SimpleField create(String fieldIdentifier) {
        if (fieldIdentifier.equals(SIMPLE_FIELD_IDENTIFIER))
            return new SimpleField();
        if (fieldIdentifier.equals(DOUBLE_WORD_VALUE_FIELD_IDENTIFIER))
            return new DoubleWordValueField();
        if (fieldIdentifier.equals(TRIPLE_WORD_VALUE_FIELD_IDENTIFIER))
            return new TripleWordValueField();
        if (fieldIdentifier.equals(DOUBLE_LETTER_VALUE_FIELD_IDENTIFIER))
            return new DoubleLetterValueField();
        if (fieldIdentifier.equals(TRIPLE_LETTER_VALUE_FIELD_IDENTIFIER))
            return new TripleLetterValueField();

       throw new IllegalArgumentException("The field identifier '" + fieldIdentifier + "' does not exist.");
    }
}
