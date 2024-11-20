package model;

public class Game {
    private String identifier;

    public Game(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return String.format("Game[identifier=%s]", identifier);
    }
}
