package ch.zuehlke.arscrabble.model.scrabble.engine;

import ch.zuehlke.arscrabble.model.scrabble.engine.fields.FieldFactory;
import ch.zuehlke.arscrabble.model.scrabble.engine.fields.SimpleField;

/**
 * Created by chsueess on 25.11.14.
 */
public class Board {

    public static final int BOARD_SIZE = 15;
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

    private SimpleField[][] board;

    public Board() {
        initialize(BOARD_TEMPLATE);
        //paint();
    }

    public SimpleField[][] getFields(){
        return board;
    }

    public void placeStone(Stone stone, int x, int y) {
        validateCoordinates(x, y);
        if(board[y][x].getStone() != null) {
            throw new ScrabbleException("That is Scrabble not Tetris, stop stacking stones ('" + stone.getLetter() + "' on '" + board[y][x].getLetter() + "') ...goof!");
        }
        board[y][x].setStone(stone);
    }

    private void validateCoordinates(int x, int y) {
        if(x >= BOARD_SIZE || y >= BOARD_SIZE) {
            throw new ScrabbleException("Board size exceeded (x = '" + x + "' / y = '" + y + "' ...klutz!");
        }
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

    public String toString() {
        String stringBoard = "";
        for (SimpleField[] row : board) {
            for (SimpleField field : row) {
                stringBoard += field.toString();
            }
            stringBoard += System.getProperty("line.separator") + "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" + System.getProperty("line.separator");
        }
        return stringBoard;
    }


    public boolean isEmpty() {
        for (int i = 0; i < board.length; i++) {
            SimpleField[] fields = board[i];
            for (int j = 0; j < fields.length; j++) {
                if(board[i][j].hasStone()) {
                    return false;
                }
            }
        }
        return true;
    }


}