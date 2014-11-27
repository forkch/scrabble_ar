package ch.zuehlke.arscrabble.model.scrabble.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chsueess on 27.11.14.
 */
public class Turn {
    private Player player;
    private Board board;
    private List<TurnStep> step = new ArrayList<TurnStep>();

    public Turn(Board board, Player player) {
        this.board = board;
        this.player = player;
    }

    public Turn placeStone(int x, int y, Letter letter) {
        Stone stone = player.getRack().pop(letter);

        if(! board.isStoneWithLetter(letter, x, y)) {
            if(stone == null) {
                throw new RuntimeException("The rack does not contain a stone with the letter '" + letter + "' and the board does not help as well ...go to hell!");
            } else {
                step.add(new TurnStep(x, y, stone));
            }
        }

        return this;
    }

    public void validate() {
        // TODO Validate Turn!
    }

    public List<TurnStep> getSteps() {
        return step;
    }

     /*public void placeWord(Word word) {
        Rack rack = players.get(activePlayerIndex).getRack();

        int x = word.getX();
        int y = word.getY();
        Direction direction = word.getDirection();

        for(Letter letter : word.getLetters()) {
            Stone stone = rack.pop(letter);

            if(! board.isStoneWithLetter(letter, x, y)) {
                if(stone == null) {
                    throw new RuntimeException("The rack does not contain a stone with the letter '" + letter + "' and the board does not help as well ...go to hell!");
                } else {
                    board.placeStone(stone, x, y);
                }
            }

            if(Direction.DOWN.equals(direction)) {
                y++;
            } else if (Direction.RIGHT.equals(direction)) {
                x++;
            }
        }

        activePlayerIndex = nextPlayer();
    }*/

    /*private boolean doesTouchTheCenter(Word word) {
        int boardCenter = (Board.BOARD_SIZE - 1) / 2;
        boolean legalX = word.getX() <= boardCenter && (word.getX()+word.size()) >= boardCenter;
        boolean legalY = word.getY() <= boardCenter && (word.getY()+word.size()) >= boardCenter;
        return legalX && legalY;
    }*/
}
