package ch.zuehlke.arscrabble.model;

/**
 * Created by chsueess on 25.11.14.
 */
public enum Letter {
    A('A', 1), B('B', 3), C('C', 4), D('D', 1), E('E', 1), F('F', 4), G('G', 2),
    H('H', 2), I('I', 1), J('J', 6), K('K', 4), L('L', 2), M('M', 3), N('N', 1),
    O('O', 2), P('P', 4), Q('Q', 10), R('R', 1), S('S', 1), T('T', 1), U('U', 1),
    V('V', 6), W('W', 3), X('X', 8), Y('Y', 10), Z('Z', 3), AE('Ä', 6), OE('Ö', 8), UE('Ü', 6), BLANKO('?', 0);

    private char value;
    private int points;

    private Letter(char value, int points){
        this.value = value;
        this.points = points;
    }
    public int getPoints() {
        return points;
    }

    public char getValue() {
        return value;
    }

    public static Letter getLetterFor(char charcater) {
        for(Letter letter : values()) {
            if(letter.equals(charcater)) {
                return letter;
            }
        }
        throw new RuntimeException("Ever saw a scrabble with a letter '' + stringLetter + ''...jerk!");
    }
}
