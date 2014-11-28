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
//        ScrabbleSolver solver = new ScrabbleSolver(null, new {"Haus", "Bild", "Baum", "Katzen", "Pflanzen"});
    }

    public ScrabbleSolver(Scrabble game, List<String> wordList) {
        this.game = game;

        index = new ArrayList<IndexedWord>();
        for(String word : wordList) {

            word = word.toLowerCase();
            String sorted = sortWord(word);
            IndexedWord indexedWord = new IndexedWord(word, sorted, 3);
            index.add(indexedWord);
//            System.out.println(indexedWord.getWord() + "-->" + indexedWord.getSortedWord());
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

        Rack rack = player.getRack();
        String regex = createRegex(rack.getStonesAsString());
        List<VirtualStone> virtualStones = new ArrayList<VirtualStone>();

        for(IndexedWord indexedWord : index) {
            if(indexedWord.getSortedWord().matches(regex)) {
                int i = 2;
                for(char character : indexedWord.getWord().toCharArray()) {
                    virtualStones.add(new VirtualStone(rack.pop(Letter.getLetterFor(character)), i, 3));
                }
                return virtualStones;
            }
        }

        return virtualStones;
    }

}
