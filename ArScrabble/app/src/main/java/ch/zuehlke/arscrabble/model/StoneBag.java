package ch.zuehlke.arscrabble.model;

/**
 * Created by chsueess on 27.11.14.
 */
public class StoneBag {
    public Stone popStone() {
        return new Stone(Letter.L, StoneType.PHYSICAL);
    }
}
