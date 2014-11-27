package ch.zuehlke.arscrabble.model;

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
}
