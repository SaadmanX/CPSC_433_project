package model.task;

import model.slots.Slot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Task {
    private String identifier;
    private String level;
    private String division;

    private List<Slot> unwantedSlots = new ArrayList<>();
    private HashMap<String, Integer> preferences = new HashMap<>();
    private ArrayList<String> notCompatibleIdentifiers = new ArrayList<>();
    private List<Task> pairedList = new ArrayList<>();
    private boolean isGame;
    private boolean isSpecialPractice;
    private int sumPreferences = 0;
    private Slot isCurrentlyAssignedTo;
    private boolean isU1519 = false;

    public Task(String identifier, boolean isGame) {
        this.identifier = identifier;
        this.isGame = isGame;
        parseIdentifier();
    }

    public boolean isU1519() {
        return this.isU1519;
    }

    public Task(Task another) {
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
    }

    public int getSumPreferences() {
        return sumPreferences;
    }

    public void setCurrentAssign(Slot slot) {
        this.isCurrentlyAssignedTo = slot;
    }

    public Slot getCurrentAssigned() {
        return this.isCurrentlyAssignedTo;
    }

    public void addPreference(Slot slot, int value) {
        preferences.put(slot.getId(), value);
        this.sumPreferences += value;
    }

    public boolean isPreferredSlot(String identifer, boolean isGame) {
        return preferences.containsKey(identifer);
    }

    public List<Task> getPairs() {
        return pairedList;
    }

    public int getPreferenceValue(Slot slot) {
        return preferences.getOrDefault(slot.getId(), 0);
    }

    public void addPair(Task anotherTask) {
        pairedList.add(anotherTask);
    }

    public boolean isPair(Task anotherTask) {
        return pairedList.stream().anyMatch(t -> t.getIdentifier().equals(anotherTask.getIdentifier()));
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

    public void setSpecialPractice(boolean isSpecialPractice) {
        this.isSpecialPractice = isSpecialPractice;
    }

    public void addNotCompatible(String anotherTaskIdentifier) {
        this.notCompatibleIdentifiers.add(anotherTaskIdentifier);
    }

    public boolean isNotCompatibleWith(Task otherTask) {
        return this.notCompatibleIdentifiers.contains(otherTask.getIdentifier());
    }

    public void addUnwantedSlot(Slot slot) {
        this.unwantedSlots.add(slot);
    }

    public boolean isUnwantedSlot(Slot slot) {
        return unwantedSlots.stream().anyMatch(s -> s.getId().equals(slot.getId()) && s.forGame() == slot.forGame());
    }

    public List<Slot> getUnwantedSlots() {
        return unwantedSlots;
    }

    private void parseIdentifier() {
        String[] parts = this.identifier.split(" ");
        if (parts.length >= 2) {
            this.level = parts[1];
            if (level.contains("U15") || level.contains("U16") || level.contains("U17") || level.contains("U19")) {
                this.isU1519 = true;
            }
        }

        this.division = "";
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("DIV") && i + 1 < parts.length) {
                this.division = parts[i + 1];
                break;
            }
        }
    }

    public int getUnwantedCount() {
        return unwantedSlots.size();
    }

    public int getNotCompatibleCount() {
        return notCompatibleIdentifiers.size();
    }

    @Override
    public String toString() {
        return String.format("Task[identifier=%s, level=%s, division=%s, isGame=%b, isSpecialPractice=%b]",
                identifier, level, division, isGame, isSpecialPractice);
    }

    public boolean hasDIV9Prefix() {
        return this.division.startsWith("9");
    }

    public int getPrefListSize(){
        return preferences.size();
    }

    public int getPriority() {
        int priority = 0;
    
        // Priority 1: Tasks with division starting with 9
        if (getDivision().startsWith("9"))priority++;
    
        // Priority 2: Tasks with the most unwanted slots
        priority += getUnwantedSlots().size();
    
        // Priority 3: Tasks with the most not compatible slots
        priority += getNotCompatibleCount();
    
        priority += getPrefListSize();

        priority += getPairs().size();

        return priority;
    }
}