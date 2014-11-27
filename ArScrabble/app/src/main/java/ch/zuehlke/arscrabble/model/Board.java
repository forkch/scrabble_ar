package ch.zuehlke.arscrabble.model;

import ch.zuehlke.arscrabble.model.fields.FieldFactory;
import ch.zuehlke.arscrabble.model.fields.SimpleField;

/**
 * Created by chsueess on 25.11.14.
 */
public class Board {

    private static final int BOARD_SIZE = 15;
    private static final String[][] BOARD_TEMPLATE = {
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

    SimpleField[][] board;

    public Board() {
        initialize(BOARD_TEMPLATE);
        //paint();
    }

    public void placeVirtualStone(Stone stone, int x, int y) {
        if(stone.getType() == StoneType.VIRTUAL) {
            throw new RuntimeException("Call Harry Potter for placing physical stones on the board...idiot!");
        }
        board[x][y].setStone(stone);
    }

    public void initialize(String[][] boardTemplate) {
        board = new SimpleField[BOARD_SIZE][BOARD_SIZE];
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