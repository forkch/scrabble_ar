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

        game.placeWord(new Word(7, 5, Direction.DOWN, Letter.M, Letter.A, Letter.U, Letter.S));

        game.placeWord(new Word(7, 10, Direction.DOWN, Letter.H, Letter.O, Letter.S, Letter.T));

        game.getBoard().paint();
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