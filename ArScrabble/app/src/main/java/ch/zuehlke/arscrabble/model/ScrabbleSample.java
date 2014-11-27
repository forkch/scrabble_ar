package ch.zuehlke.arscrabble.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chsueess on 27.11.14.
 */
public class ScrabbleSample {
    public static void main(String[] args) {
        Scrabble game = new Scrabble();


        List<Stone> playerOneRack = new ArrayList<Stone>();
        playerOneRack.add(game.getStoneBag().popStone());
        List<Stone> playerTwoRack = new ArrayList<Stone>();

        game.addPlayer(new Player("Stefan", new Rack(playerOneRack)));
        game.addPlayer(new Player("Benjamin", new Rack(playerTwoRack)));
        game.start();


    }
}