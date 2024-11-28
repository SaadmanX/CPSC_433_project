package model.task;

import java.util.ArrayList;
import java.util.List;

import model.slots.Slot;

public class Task {
    private String identifier;
    private String level; 
    private String division; 
    private String tier;
    private List<Slot> unwantedSlots;
    private ArrayList<String> notCompatibleIdentifiers = new ArrayList<>();
    private boolean isGame; 
    private boolean isSpecialPractice;

    public Task(String identifier, boolean isGame) {
        this.identifier = identifier;
        this.isGame = isGame;
        parseIdentifier(); 
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getLevel() {
        return this.level;
    }

    public String getDivision() {
        return this.division;
    }

    public String getTier() {
        return this.tier;
    }

    public boolean getIsGame() {
        return this.isGame;
    }

    public boolean isSpecialPractice() {
        return this.isSpecialPractice;
    }

    // Method to set special practice flag
    public void setSpecialPractice(boolean isSpecialPractice) {
        this.isSpecialPractice = isSpecialPractice;
    }

    public void addNotCompatible(String anotherTaskIdentifier) {
        this.notCompatibleIdentifiers.add(anotherTaskIdentifier);
    }

    public boolean isNotCompatibleWith(Task otherTask) {
        return this.notCompatibleIdentifiers.contains(otherTask.getIdentifier());
    }

    public void addUnwantedSlot(Slot slot){
        this.unwantedSlots.add(slot);
    }

    public boolean isUnwantedSlot(Slot slot){
        if (unwantedSlots != null) return unwantedSlots.contains(slot);
        return false; 
    }

    private void parseIdentifier() {
        String[] parts = this.identifier.split(" "); 

        if (parts.length >= 2) {
            this.level = parts[1]; 
            this.tier = level.length() > 3 ? level.substring(3) : ""; 
        }
        this.division = ""; 
        for (String part : parts) {
            if (part.startsWith("DIV")) {
                this.division = part;
                break;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Task[identifier=%s, level=%s, division=%s, tier=%s, isGame=%b, isSpecialPractice=%b]",
                identifier, level, division, tier, isGame, isSpecialPractice);
    }
}
