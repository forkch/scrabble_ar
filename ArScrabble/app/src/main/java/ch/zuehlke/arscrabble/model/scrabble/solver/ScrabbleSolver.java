package ch.zuehlke.arscrabble.model.scrabble.solver;

import java.util.ArrayList;
import java.util.List;

import ch.zuehlke.arscrabble.model.scrabble.engine.Player;
import ch.zuehlke.arscrabble.model.scrabble.engine.Scrabble;
import ch.zuehlke.arscrabble.model.scrabble.engine.Stone;

/**
 * Created by chsueess on 27.11.14.
 */
public class ScrabbleSolver {
    private Scrabble game;

    public ScrabbleSolver(Scrabble game) {
        this.game = game;
    }

    public List<VirtualStone> getWord(Player player) {
        List<VirtualStone> virtualStones = new ArrayList<VirtualStone>();
        List<Stone> stones = player.getRack().getStones();
        for (int i=0; i< stones.size(); i++) {
            Stone stone = stones.get(i);
            virtualStones.add(new VirtualStone(stone, 1, i));
        }
        return virtualStones;
    }

}
