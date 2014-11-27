package ch.zuehlke.arscrabble.model.scrabble.engine;

/**
 * Created by chsueess on 27.11.14.
 */
public class Stone {
    private Letter letter;
    private StoneType type;

    public Stone(Letter letter, StoneType type) {
        this.letter = letter;
        this.type = type;
    }

    public Letter getLetter() {
        return letter;
    }

    public StoneType getType() {
        return type;
    }

    public boolean isVirtual(){
        return type == StoneType.VIRTUAL;
    }
}
