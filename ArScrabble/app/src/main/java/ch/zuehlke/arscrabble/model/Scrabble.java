package ch.zuehlke.arscrabble.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chsueess on 27.11.14.
 */
public class Scrabble {
    private static final int MAX_NUMBER_OF_PLAYERS = 4;
    private static final int MIN_NUMBER_OF_PLAYERS = 2;

    private Board board;
    private StoneBag stoneBag;
    private List<Player> players = new ArrayList<Player>();

    public void addPlayer(Player player) {
        if(players.size() > MAX_NUMBER_OF_PLAYERS) {
            throw new RuntimeException("'" + players.size() + "' are enough for a soccer game, but too much for Scrabble...idiot!");
        }
        players.add(player);
    }

    public void start() {
       stoneBag = new StoneBag();
       board = new Board();
       if(players.size() < MIN_NUMBER_OF_PLAYERS) {
            throw new RuntimeException("First time Scrabble? You have to be at least two players...dumb ass!");
       }
    }

    public StoneBag getStoneBag() {
        return stoneBag;
    }
}