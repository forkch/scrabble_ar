package ch.zuehlke.arscrabble.model.scrabble.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chsueess on 27.11.14.
 */
public class ScrabbleSample {
    public static void main(String[] args) {
        Scrabble game = new Scrabble();

        Player stefan = new Player("Stefan", new Rack(getStefansStones(game.getStoneBag())));
        Player benjamin = new Player("Benjamin", new Rack(getBenjaminsStones(game.getStoneBag())));

        game.addPlayer(stefan);
        game.addPlayer(benjamin);
        
        game.start();

        Turn turn1 = game.newTurn().placeStone(7,5,Letter.M)
                .placeStone(7,6,Letter.A)
                .placeStone(7,7,Letter.U)
                .placeStone(7,8,Letter.S);

        game.executeTurn(turn1);

        Turn turn2 = game.newTurn().placeStone(6,6,Letter.H)
                .placeStone(8,6,Letter.S)
                .placeStone(9,6,Letter.T);

        game.executeTurn(turn2);

        Turn turn3 = game.newTurn(Letter.A, Letter.N, Letter.Z, Letter.U)
                .placeStone(9,7,Letter.A)
                .placeStone(9,8,Letter.N)
                .placeStone(9,9,Letter.Z);

        game.executeTurn(turn3);
    }

    private static List<Stone> getStefansStones(StoneBag stoneBag) {
        List<Stone> stefansStones = new ArrayList<Stone>();
        stefansStones.add(stoneBag.pop(Letter.U));
        stefansStones.add(stoneBag.pop(Letter.D));
        stefansStones.add(stoneBag.pop(Letter.F));
        stefansStones.add(stoneBag.pop(Letter.B));
        stefansStones.add(stoneBag.pop(Letter.M));
        stefansStones.add(stoneBag.pop(Letter.A));
        stefansStones.add(stoneBag.pop(Letter.S));
        return stefansStones;
    }

    private static List<Stone> getBenjaminsStones(StoneBag stoneBag) {
        List<Stone> benjaminsStones = new ArrayList<Stone>();
        benjaminsStones.add(stoneBag.pop(Letter.G));
        benjaminsStones.add(stoneBag.pop(Letter.H));
        benjaminsStones.add(stoneBag.pop(Letter.S));
        benjaminsStones.add(stoneBag.pop(Letter.T));
        benjaminsStones.add(stoneBag.pop(Letter.M));
        benjaminsStones.add(stoneBag.pop(Letter.O));
        benjaminsStones.add(stoneBag.pop(Letter.K));
        return benjaminsStones;
    }
}