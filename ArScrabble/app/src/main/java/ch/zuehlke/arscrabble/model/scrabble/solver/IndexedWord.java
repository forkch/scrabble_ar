package ch.zuehlke.arscrabble.model.scrabble.solver;

/**
 * Created by chsueess on 28.11.14.
 */
public class IndexedWord {
    private String word;
    private String sortedWord;
    private int points;

    public IndexedWord(String word, String sortedWord, int points) {
        this.word = word;
        this.sortedWord = sortedWord;
        this.points = points;
    }

    public String getWord() {
        return word;
    }

    public String getSortedWord() {
        return sortedWord;
    }

    public int getPoints() {
        return points;
    }
}
