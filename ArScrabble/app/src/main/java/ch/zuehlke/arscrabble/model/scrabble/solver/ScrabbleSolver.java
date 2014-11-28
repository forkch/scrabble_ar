package ch.zuehlke.arscrabble.model.scrabble.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.zuehlke.arscrabble.model.scrabble.engine.Letter;
import ch.zuehlke.arscrabble.model.scrabble.engine.Player;
import ch.zuehlke.arscrabble.model.scrabble.engine.Rack;
import ch.zuehlke.arscrabble.model.scrabble.engine.Scrabble;
import ch.zuehlke.arscrabble.model.scrabble.engine.Stone;
import ch.zuehlke.arscrabble.model.scrabble.engine.StoneBag;

/**
 * Created by chsueess on 27.11.14.
 */
public class ScrabbleSolver {
    private Scrabble game;
    private List<IndexedWord> index;

    public static void main(String[] args) {
        ScrabbleSolver solver = new ScrabbleSolver(null, new String[]{"Haus", "Bild", "Baum", "Katzen", "Pflanzen"});
    }

    public ScrabbleSolver(Scrabble game, String[] wordList) {
        this.game = game;

        index = new ArrayList<IndexedWord>();
        for(String word : wordList) {

            word = word.toLowerCase();
            String sorted = sortWord(word);
            IndexedWord indexedWord = new IndexedWord(word, sorted, 3);
            index.add(indexedWord);
            System.out.println(indexedWord.getWord() + "-->" + indexedWord.getSortedWord());
        }

        StoneBag stoneBag = new StoneBag();
        List<Stone> stefansStones = new ArrayList<Stone>();
        stefansStones.add(stoneBag.pop(Letter.U));
        stefansStones.add(stoneBag.pop(Letter.D));
        stefansStones.add(stoneBag.pop(Letter.A));
        stefansStones.add(stoneBag.pop(Letter.H));
        stefansStones.add(stoneBag.pop(Letter.M));
        stefansStones.add(stoneBag.pop(Letter.A));
        stefansStones.add(stoneBag.pop(Letter.S));

       String regex = createRegex( new Player("Hans", new Rack(stefansStones)).getRack().getStonesAsString());

       System.out.println(regex);

       for(IndexedWord indexedWord : index) {
           if(indexedWord.getSortedWord().matches(regex)) {
               System.out.println(indexedWord.getWord());
           }
       }


    }

    private String sortWord(String word) {
        char[] chars = word.toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    private String createRegex(String letters) {
        letters = sortWord(letters).toLowerCase();

        // a{0,2}
        String regex = "";
        char last = 0;
        int amount = 1;
        for(char character : letters.toCharArray()) {
            if(character == last) {
                amount++;
            } else {
                if(last != 0) {
                    regex += "{0," + amount + "}";
                }
                regex += character;
                last = character;
                amount = 1;
            }
        }
        regex += "{0," + amount + "}";

        return regex;
    }

    public List<VirtualStone> getWord(Player player) {
        List<VirtualStone> virtualStones = new ArrayList<VirtualStone>();
        List<Stone> stones = player.getRack().getStones();
        virtualStones.add(new VirtualStone(stones.get(0), 1, 8));
        virtualStones.add(new VirtualStone(stones.get(1), 2, 8));
        virtualStones.add(new VirtualStone(stones.get(2), 3, 8));
        virtualStones.add(new VirtualStone(stones.get(3), 4, 8));
        virtualStones.add(new VirtualStone(stones.get(4), 5, 8));
        virtualStones.add(new VirtualStone(stones.get(5), 6, 8));
        virtualStones.add(new VirtualStone(stones.get(6), 7, 8));

        return virtualStones;
    }

}
