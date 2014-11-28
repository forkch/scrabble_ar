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
            throw new RuntimeException("First time Scrabble? You have to be at least two players ...dumb ass!");
       }
        activePlayerIndex = 0;
    }

    public Turn newTurn(Letter... letters) {
        Player activePlayer = players.get(activePlayerIndex);
        for(Letter letter : letters) {
            activePlayer.getRack().addStone(stoneBag.pop(letter));
        }

        if(! activePlayer.getRack().isFull() && stoneBag.hasStones()) {
            throw new ScrabbleException("'" + activePlayer.getRack().size() + "' are a wrong amount of stones. Take less / more...do it!");
        }

        return new Turn(board, activePlayer);
    }

    public StoneBag getStoneBag() {
        return stoneBag;
    }


    private int nextPlayer() {
        int nextPlayerIndex = activePlayerIndex + 1;
        if(nextPlayerIndex < players.size()) {
            return nextPlayerIndex;
        } else {
            return 0;
        }
    }

    public void executeTurn(Turn turn) {
        turn.validate();
        paintPlayers();
        for(TurnStep turnStep : turn.getSteps()) {
            board.placeStone(turnStep.getStone(), turnStep.getX(), turnStep.getY());
        }
        paintBoard();

        activePlayerIndex = nextPlayer();
    }

    private void paintBoard() {
        System.out.println(board.toString());
    }

    private void paintPlayers() {
        for(Player player : players) {
            System.out.println(player.toString());
        }
    }

    public Board getBoard() {
        return board;
    }

    public Player getActivePlayer(){
        return players.get(activePlayerIndex);
    }
}