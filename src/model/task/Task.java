package model.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.slots.Slot;

public class Task {
    private String identifier;
    private String level; 
    private String division; 
    // private String tier;

    private List<Slot> unwantedSlots = new ArrayList<>();
    private HashMap<Slot, Integer> preferences = new HashMap<>();
    private ArrayList<String> notCompatibleIdentifiers = new ArrayList<>();
    private List<Task> pairedList = new ArrayList<>();
    private boolean isGame; 
    private boolean isSpecialPractice;
    private int sumPreferences = 0;
    private Slot isCurrentlyAssignedTo;

    public Task(String identifier, boolean isGame) {
        this.identifier = identifier;
        this.isGame = isGame;
        parseIdentifier(); 
    }

    public Task (Task another){
        this.identifier = another.identifier;
        this.division = another.division;
        this.level = another.level;
        this.isGame = another.isGame;
        this.unwantedSlots = another.unwantedSlots;
        this.preferences = another.preferences;
        this.notCompatibleIdentifiers = another.notCompatibleIdentifiers;
        this.pairedList = another.pairedList;
        this.isSpecialPractice = another.isSpecialPractice;
        this.sumPreferences = another.sumPreferences;
        this.isCurrentlyAssignedTo = another.isCurrentlyAssignedTo;
    }

    //Handy for update with 1 newest assignment only
    public int getSumPreferences(){
        return sumPreferences;
    }

    public void setCurrentAssign(Slot slot){
        this.isCurrentlyAssignedTo = slot;
    }

    public Slot getCurrentAssigned(){
        return this.isCurrentlyAssignedTo;
    }

    public void addPreference(Slot slot, int value) {
        preferences.put(slot, value);
        sumPreferences += value;
    }

    public boolean isPreferredSlot(Slot slot) {
        if (preferences != null)return preferences.containsKey(slot);
        return false;
    }

    public List<Task> getPairs(){
        return pairedList;
    }

    public int getPreferenceValue(Slot slot) {
        return preferences.getOrDefault(slot, 0);
    }

    public void addPair(Task anotherTask) {
        pairedList.add(anotherTask);
        anotherTask.addPair(this);
    }

    public boolean isPair(Task anotherTask) {
        return pairedList.contains(anotherTask);
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

    public void printNotCombatible() {
        System.out.println(notCompatibleIdentifiers);
    }

    public boolean isNotCompatibleWith(Task otherTask) {
        // System.out.println("6666666666666666666666666666666 Printing other task:: ");
        // System.out.println(otherTask.getIdentifier());
        for (String notc : notCompatibleIdentifiers) {
            if (otherTask.getIdentifier().equals(notc)) {
                return true;
            }
        }
        return false;
        // return this.notCompatibleIdentifiers.contains(otherTask.getIdentifier());
    }

    public void addUnwantedSlot(Slot slot){
        this.unwantedSlots.add(slot);
    }

    public boolean isUnwantedSlot(Slot slot){
        if (unwantedSlots != null)return unwantedSlots.contains(slot);
        return false;
    }

    public List<Slot> getUnwantedSlots(){
        return unwantedSlots;
    }
    
    private void parseIdentifier() {
        String[] parts = this.identifier.split(" "); 
    
        if (parts.length >= 2) {
            this.level = parts[1]; 
            // this.tier = level.length() > 3 ? level.substring(3) : ""; 
        }
        
        // Find division number
        this.division = ""; 
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("DIV") && i + 1 < parts.length) {
                // Combine "DIV" with its number
                this.division = parts[i + 1];
                break;
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("Task[identifier=%s, level=%s, division=%s, isGame=%b, isSpecialPractice=%b]",
                identifier, level, division, isGame, isSpecialPractice);
    }
}