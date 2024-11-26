package model.task;

import java.util.ArrayList;

public class Task {
    private String identifier;
    ArrayList<String> notCompatibleIdentifier = new ArrayList<>();
    boolean isGame;

    public Task(String identifier, boolean isGame) {
        this.identifier = identifier;
        this.isGame = isGame;
    }

    public String getIdentifier(){
        return this.identifier;
    }

    public void addNotCompatible(String anotherTaskIdentifier){
        this.notCompatibleIdentifier.add(anotherTaskIdentifier);
    }

    public boolean getIsGame(){
        return this.isGame;
    }

    @Override
    public String toString() {
        return String.format("Task[identifier=%s]", identifier);
    }
}