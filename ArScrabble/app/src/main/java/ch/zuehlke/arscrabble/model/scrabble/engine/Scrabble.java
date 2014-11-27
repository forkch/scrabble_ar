package ch.zuehlke.arscrabble.model.scrabble.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chsueess on 27.11.14.
 */
public class Scrabble {
    private static final int MAX_NUMBER_OF_PLAYERS = 4;
    private static final int MIN_NUMBER_OF_PLAYERS = 2;

    private Board board;
    private StoneBag stoneBag = new StoneBag();
    private List<Player> players = new ArrayList<Player>();
    private int activePlayerIndex;

    public void addPlayer(Player player) {
        if(players.size() > MAX_NUMBER_OF_PLAYERS) {
            throw new RuntimeException("'" + players.size() + "' are enough for a soccer game, but too much for Scrabble...idiot!");
        }
        players.add(player);
    }

    public void start() {
       board = new Board();
       if(players.size() < MIN_NUMBER_OF_PLAYERS) {
            throw new RuntimeException("First time Scrabble? You have to be at least two players...dumb ass!");
       }
        activePlayerIndex = 0;
    }

    public StoneBag getStoneBag() {
        return stoneBag;
    }

    public void placeWord(Word word) {
        Rack rack = players.get(activePlayerIndex).getRack();

        int x = word.getX();
        int y = word.getY();
        Direction direction = word.getDirection();

        if(! hasLegalPosition(word)) {
            throw new RuntimeException("You can not position your word without trouching an existing one... nice try!");
        }

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
    }

    private boolean hasLegalPosition(Word word) {
        if(board.isEmpty()) {
            return doesTouchTheCenter(word);
        } else {
            return doesTouchOtherWord(word);
        }
    }

    private boolean doesTouchOtherWord(Word word) {
        return true;
        /*if(Direction.DOWN.equals(word.getDirection())) {
            // TODO Check IndexOutOfBounce
            for(int height=0; height<word.size(); height++) {
                for(int width=0; width<3; width++) {
                    if(board.getFields()[word.getX() + width - 1][word.getY()+ height].hasStone()) {
                        return true;
                    }
                }
            }
            if(board.getFields()[word.getX()][word.getY() - 1].hasStone()) {
                return true;
            }

            if(board.getFields()[word.getX()][word.getY() + word.size() + 1].hasStone()) {
                return true;
            }
            return false;
        } else if(Direction.RIGHT.equals(word.getDirection())) {
            // TODO Check as well!
        }
        throw new RuntimeException("No word direction defined... looser!");*/
    }

    private boolean doesTouchTheCenter(Word word) {
        int boardCenter = (Board.BOARD_SIZE - 1) / 2;
        boolean legalX = word.getX() <= boardCenter && (word.getX()+word.size()) >= boardCenter;
        boolean legalY = word.getY() <= boardCenter && (word.getY()+word.size()) >= boardCenter;
        return legalX && legalY;
    }

    private int nextPlayer() {
        int nextPlayerIndex = activePlayerIndex + 1;
        if(nextPlayerIndex < players.size()) {
            return nextPlayerIndex;
        } else {
            return 0;
        }
    }

    public Board getBoard() {
        return board;
    }
}