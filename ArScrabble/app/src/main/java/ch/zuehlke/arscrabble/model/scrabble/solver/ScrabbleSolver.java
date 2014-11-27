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
