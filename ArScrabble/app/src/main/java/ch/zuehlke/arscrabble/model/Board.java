package ch.zuehlke.arscrabble.model;

import ch.zuehlke.arscrabble.model.fields.FieldFactory;
import ch.zuehlke.arscrabble.model.fields.SimpleField;

/**
 * Created by chsueess on 25.11.14.
 */
public class Board {
    SimpleField[][] board = new SimpleField[15][15];

    public static void main(String[] args) {
        String[][] boardTemplate = {
                {"3W", "  ", "  ", "2L", "  ", "  ", "  ", "3W", "  ", "  ", "  ", "2L", "  ", "  ", "3W"},
                {"  ", "2W", "  ", "  ", "  ", "3L", "  ", "  ", "  ", "3L", "  ", "  ", "  ", "2W", "  "},
                {"  ", "  ", "2W", "  ", "  ", "  ", "2L", "  ", "2L", "  ", "  ", "  ", "2W", "  ", "  "},
                {"2L", "  ", "  ", "2W", "  ", "  ", "  ", "2L", "  ", "  ", "  ", "2W", "  ", "  ", "2L"},
                {"  ", "  ", "  ", "  ", "2W", "  ", "  ", "  ", "  ", "  ", "2W", "  ", "  ", "  ", "  "},
                {"  ", "3L", "  ", "  ", "  ", "2W", "  ", "  ", "  ", "2W", "  ", "  ", "  ", "3L", "  "},
                {"  ", "  ", "2L", "  ", "  ", "  ", "2W", "  ", "2W", "  ", "  ", "  ", "2L", "  ", "  "},
                {"3W", "  ", "  ", "2L", "  ", "  ", "  ", "2W", "  ", "  ", "  ", "2L", "  ", "  ", "3W"},
                {"  ", "  ", "2L", "  ", "  ", "  ", "2W", "  ", "2W", "  ", "  ", "  ", "2L", "  ", "  "},
                {"  ", "3L", "  ", "  ", "  ", "2W", "  ", "  ", "  ", "2W", "  ", "  ", "  ", "3L", "  "},
                {"  ", "  ", "  ", "  ", "2W", "  ", "  ", "  ", "  ", "  ", "2W", "  ", "  ", "  ", "  "},
                {"2L", "  ", "  ", "2W", "  ", "  ", "  ", "2L", "  ", "  ", "  ", "2W", "  ", "  ", "2L"},
                {"  ", "  ", "2W", "  ", "  ", "  ", "2L", "  ", "2L", "  ", "  ", "  ", "2W", "  ", "  "},
                {"  ", "2W", "  ", "  ", "  ", "3L", "  ", "  ", "  ", "3L", "  ", "  ", "  ", "2W", "  "},
                {"3W", "  ", "  ", "2L", "  ", "  ", "  ", "3W", "  ", "  ", "  ", "2L", "  ", "  ", "3W"},
        };

        Board board = new Board();
        board.initialize(boardTemplate);
        board.paint();
    }

    public void placeVirtualStone(Stone stone, int x, int y) {
        if(stone.getType() == StoneType.VIRTUAL) {
            throw new RuntimeException("Call Harry Potter for placing physical stones on the board...idiot!");
        }
        board[x][y].setStone(stone);
    }

    public void initialize(String[][] boardTemplate) {
        FieldFactory factory = new FieldFactory();
        for (int i = 0; i < boardTemplate.length; i++) {
            String[] fields = boardTemplate[i];
            for (int j = 0; j < fields.length; j++) {
                board[i][j] = factory.create(fields[j]);
            }
        }
    }

    public void paint() {
        for(SimpleField[] row : board) {
            for(SimpleField field : row) {
                field.paint();
            }
            System.out.println("");
        }
    }
}
