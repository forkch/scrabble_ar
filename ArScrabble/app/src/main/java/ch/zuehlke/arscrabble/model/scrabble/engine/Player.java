package ch.zuehlke.arscrabble.model.scrabble.engine;

/**
 * Created by chsueess on 27.11.14.
 */
public class Player {
    private String name;
    private Rack rack;

    public Player(String name, Rack rack) {
        this.name = name;
        this.rack = rack;
    }

    public String getName() {
        return name;
    }

    public Rack getRack() {
        return rack;
    }
}
