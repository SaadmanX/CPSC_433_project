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
    private boolean isU1519 = false;
    private int age;

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

    public boolean isU1519(){
        return this.isU1519;
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
        this.isU1519 = another.isU1519;
        this.age = another.age;
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

    public int getAge() {
        return this.age;
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

    public boolean isNotCompatibleWith(Task otherTask) {
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
    
    // private void parseIdentifier() {
    //     String[] parts = this.identifier.split(" "); 
    
    //     if (parts.length >= 2) {
    //         this.level = parts[1]; 
    //         if (level.contains("U15") || level.contains("U16") || level.contains("U17") || level.contains("U19")){
    //             this.isU1519 = true;
    //         }
    //         // this.tier = level.length() > 3 ? level.substring(3) : ""; 
    //     }
        
    //     // Find division number
    //     this.division = ""; 
    //     for (int i = 0; i < parts.length; i++) {
    //         if (parts[i].equals("DIV") && i + 1 < parts.length) {
    //             // Combine "DIV" with its number
    //             this.division = parts[i + 1];
    //             break;
    //         }
    //     }
    // }

    private void parseIdentifier() {
        String[] parts = this.identifier.split(" "); 
    
        if (parts.length >= 2) {
            this.level = parts[1];
            if (level.contains("U15") || level.contains("U16") || level.contains("U17") || level.contains("U19")){
                this.isU1519 = true;
            }
    
            // Extract age from level (parts[1])
            if (level.startsWith("U")) {
                // For youth teams (e.g., U07, U19)
                // Find index of 'T' if it exists, otherwise take full number
                int endIndex = level.indexOf('T');
                if (endIndex == -1) {
                    endIndex = level.length();
                }
                try {
                    this.age = Integer.parseInt(level.substring(1, endIndex));
                } catch (NumberFormatException e) {
                    this.age = 0;  // Default value if parsing fails
                }
            } else if (level.startsWith("O")) {
                // For adult teams (e.g., O19, O35)
                int endIndex = level.indexOf('T');
                if (endIndex == -1) {
                    endIndex = level.length();
                }
                try {
                    this.age = Integer.parseInt(level.substring(1, endIndex));
                } catch (NumberFormatException e) {
                    this.age = 0;  // Default value if parsing fails
                }
            }
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
        return String.format("Task[identifier=%s, level=%s, division=%s, age=%s, isGame=%b, isSpecialPractice=%b]",
                identifier, level, division, age, isGame, isSpecialPractice);
    }
}