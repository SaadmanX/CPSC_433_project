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
    private HashMap<String, Integer> preferences = new HashMap<>();
    private ArrayList<String> notCompatibleIdentifiers = new ArrayList<>();
    private List<Task> pairedList = new ArrayList<>();
    private boolean isGame; 
    private boolean isSpecialPractice;
    private int sumPreferences = 0;
    private Slot isCurrentlyAssignedTo;

    /**
     * Default constructor with parser
     * @param identifier
     * @param isGame
     */
    public Task(String identifier, boolean isGame) {
        this.identifier = identifier;
        this.isGame = isGame;
        parseIdentifier(); 
    }

    /**
     * Copy Constructor
     * @param another: constructor to copy
     */
    public Task (Task another){
        this.identifier = another.identifier;
        this.division = another.division;
        this.level = another.level;
        this.isGame = another.isGame;
        this.unwantedSlots = new ArrayList<>(another.unwantedSlots);
        this.preferences = new HashMap<>(another.preferences);
        this.notCompatibleIdentifiers = new ArrayList<>(another.notCompatibleIdentifiers);
        this.pairedList = new ArrayList<>(another.pairedList);
        this.isSpecialPractice = another.isSpecialPractice;
        this.sumPreferences = another.sumPreferences;
        this.isCurrentlyAssignedTo = another.isCurrentlyAssignedTo;
    }

    //Handy for update with 1 newest assignment only
    public int getSumPreferences(){
        return sumPreferences;
    }

    /**
     * Set shallow copy to the currentlyAssigned
     * @param slot: currently Assigned (shallow)
     */
    public void setCurrentAssign(Slot slot){
        this.isCurrentlyAssignedTo = slot;
    }

    /**
     * This function returns the current slot this task is assigned to
     * @return currently Assign slot, shallow copy
     */
    public Slot getCurrentAssigned(){
        return this.isCurrentlyAssignedTo;
    }


    public void addPreference(Slot slot, int value) {
        preferences.put(slot.getId(), value);
        sumPreferences += value;
    }

    /**
     * @param identifer: Slot id
     * @param isGame: Slot isGame
     * @return whether it is a preferred slot
     */
    public boolean isPreferredSlot(String identifer, boolean isGame) {
        if (preferences != null){
            return preferences.containsKey(identifer);
        }
        return false;
    }


    /**
     * SHALLOW LIST OF TASKS WILL BE RETURNED
     * @return list of tasks (will need further processing)
     */
    public List<Task> getPairs(){
        return pairedList;
    }

    public int getPreferenceValue(Slot slot) {
        return preferences.getOrDefault(slot, 0);
    }

    public void addPair(Task anotherTask) {
        pairedList.add(anotherTask);
    }

    /**
     * The function takes in another Tasks and checks if that deep copy is pair with current task
     * @param anotherTask: deep copy of original task
     * @return whether identifier matches
     */
    public boolean isPair(Task anotherTask) {
        for (Task t: pairedList){
            if (t.getIdentifier().equals(anotherTask.getIdentifier()))return true;
        }
        return false;
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

    /**
     * Add String of notCompatible instead of task
     * @param anotherTaskIdentifier
     */
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


    /**
     * Shallow copy of unwantedSlot
     * @param slot: slot to be added to unwanted
     */
    public void addUnwantedSlot(Slot slot){
        this.unwantedSlots.add(slot);
    }

    /**
     * Already processed the id and for game
     * @param slot: Slot to check
     * @return true if the id + forgame map
     */
    public boolean isUnwantedSlot(Slot slot){
        if (unwantedSlots != null){
            for (Slot s: unwantedSlots){
                if (s.getId().equals(slot.getId()) && s.forGame() == slot.forGame()){
                    return true;
                }
            }
        };
        return false;
    }

    /**
     * LIST of SLOTS, will need further processing with String
     * @return list of SLOT
     */
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