package ch.zuehlke.arscrabble.model.scrabble.engine;

/**
 * Created by chsueess on 25.11.14.
 */
public enum Letter {
    A('A', 1, "a"), B('B', 3, "b"), C('C', 4, "c"), D('D', 1, "d"), E('E', 1, "e"), F('F', 4, "f"), G('G', 2, "g"),
    H('H', 2, "h"), I('I', 1, "i"), J('J', 6, "j"), K('K', 4, "k"), L('L', 2, "l"), M('M', 3, "m"), N('N', 1, "n"),
    O('O', 2, "o"), P('P', 4, "p"), Q('Q', 10, "q"), R('R', 1, "r"), S('S', 1, "s"), T('T', 1, "t"), U('U', 1, "u"),
    V('V', 6, "v"), W('W', 3, "w"), X('X', 8, "x"), Y('Y', 10, "y"), Z('Z', 3, "z"), AE('Ä', 6, "ae"), OE('Ö', 8, "oe"), UE('Ü', 6, "ue"), BLANKO('?', 0, "blank");

    private char value;
    private int points;
    private String textureName;

    private Letter(char value, int points, String textureName){
        this.value = value;
        this.points = points;
        this.textureName = textureName;
    }
    public int getPoints() {
        return points;
    }

    public char getValue() {
        return value;
    }

    public String getTextureName(){return textureName;}

    public static Letter getLetterFor(char charcater) {
        for(Letter letter : values()) {
            if(letter.getValue() == charcater) {
                return letter;
            }
        }
        throw new RuntimeException("Ever saw a scrabble with a letter '' + stringLetter + ''...jerk!");
    }

}
