package search;

import model.Assignment;
import model.SearchState;
import model.constraints.Constraint;
import model.constraints.NotCompatible;
import model.constraints.Pair;
import model.constraints.PartialAssignment;
import model.constraints.Preference;
import model.constraints.Unwanted;
import model.slots.Slot;
import model.task.Task;
import parser.InputParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import constraints.HardConstraintsEval;
import constraints.SoftConstraintsEval;

public class AndTree {
    private SearchState state;
    private HashMap<String, List<Constraint>> constraints = new HashMap<>(); 
    private InputParser parser = new InputParser();
    private String inputFileName;
    private List<PartialAssignment> partialAssignments = new ArrayList<>();
    private List<Unwanted> unwantedList = new ArrayList<>();
    private List<Pair> pairList = new ArrayList<>();
    private List<Preference> preferencesList = new ArrayList<>();
    private List<Task> notCompatibles = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();
    private List<Slot> allSlots = new ArrayList<>();
    //private Map<Slot, List<Slot>> linkedSlotGroups = new HashMap<>();
    SearchState lastState;
    HardConstraintsEval hardChecker = new HardConstraintsEval();
    SoftConstraintsEval softChecker;
    private int minEval = Integer.MAX_VALUE;
    ArrayList<Integer> weightList;
    ArrayList<Integer> multiplierList;

    public AndTree(SearchState root, String filename, ArrayList<Integer> weightList, ArrayList<Integer> multiplierList) {
        this.state = root;
        this.inputFileName = filename;
        this.weightList = weightList;
        this.multiplierList = multiplierList;
    }

    private void parseInput(){
        try {
            parser.parseFile(this.inputFileName);

            parser.parseGameSlots();
            parser.parsePracticeSlots();

            parser.parseGames();
            parser.parsePractices();

            allTasks = parser.getAllTasks();
            allSlots = parser.getAllSlots();

            state.setRemainingSlots(allSlots);
            state.setRemainingTask(allTasks);

            constraints.put("NotCompatible", parser.parseNotCompatible());
            makeNotCompatibleList();
          
            makePairList();
            pairList = parser.parsePairs();
            preferencesList = parser.parsePreferences();
            unwantedList = parser.parseUnwanted();
            partialAssignments  = parser.parsePartialAssignments();

            softChecker = new SoftConstraintsEval(multiplierList, weightList, preferencesList, pairList, allSlots);
            state.setPenalty(softChecker.initialPenalty);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }


    public void preprocess() {
        parseInput();
        //buildLinkedSlots();

        if (!assignPartialAssignments()){
            System.out.println("FAILED WITH PARTIAL");
            System.exit(1);
        }
        assignPreferences();
        assignUnwanted();

    }

    private void assignUnwanted(){
        for (Unwanted unwanted : unwantedList) {
            Task task = findTaskByIdentifier(unwanted.getTaskIdentifier());
            if (task == null)continue;
            Slot slot = findSlotByDayAndTime(unwanted.getDay(), unwanted.getTime(), task.getIsGame());
            if (slot != null) {
                task.addUnwantedSlot(slot);
            } 
        }
    }

    private void assignPreferences(){
        for (Preference preference: preferencesList){
            Task task = findTaskByIdentifier(preference.getTaskIdentifier());
            if (task == null)continue;
            Slot slot = findSlotByDayAndTime(preference.getDay(), preference.getTime(), task.getIsGame());
            if (slot != null) {
                task.addPreference(slot, preference.getPenalty());
            }
        }
    }

    private Task findTaskByIdentifier(String identifier) {
        return allTasks.stream().filter(task -> task.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    private Slot findSlotByDayAndTime(String day, String time, boolean isGame) {
        return allSlots.stream()
                .filter(slot -> slot.getDay().equals(day) && slot.getStartTime().equals(time) && slot.forGame() == isGame)
                .findFirst()
                .orElse(null);
    }

    private boolean assignPartialAssignments() {

        for (PartialAssignment partial : partialAssignments) {
            Task task = findTaskByIdentifier(partial.getTaskIdentifier());
            Slot slot = findSlotByDayAndTime(partial.getDay(), partial.getTime(), task.getIsGame());
            if (task != null && slot != null) {
                SearchState newestState = transitLinkedAssignment(state, task, slot);
                if (state.equals(newestState)){
                    return false;
                }
                state = newestState;
            }
        }
        return true;
    }


    /**
    private void buildLinkedSlots() {
        for (Slot slot : allSlots) {
            List<Slot> linkedSlots = new ArrayList<>();

            // Build linked slots based on problem constraints
            switch (slot.getDay()) {
                case "MO" -> {
                    linkedSlots.addAll(findSlotsByDayAndTime("WE", slot.getStartTime(), slot.forGame()));
                    linkedSlots.addAll(findSlotsByDayAndTime("FR", slot.getStartTime(), slot.forGame()));
                }
                case "TU" -> linkedSlots.addAll(findSlotsByDayAndTime("TH", slot.getStartTime(), slot.forGame()));
                case "WE" -> linkedSlots.addAll(findSlotsByDayAndTime("FR", slot.getStartTime(), slot.forGame()));
                default -> {
                }
            }

            linkedSlotGroups.put(slot, linkedSlots);
        }
    }

     */
    private List<Slot> findSlotsByDayAndTime(String day, String time, boolean forGame) {
        return allSlots.stream()
                .filter(slot -> slot.getDay().equals(day) && slot.getStartTime().equals(time) 
                && slot.forGame() == forGame)
                .toList();
    }

    private SearchState transitLinkedAssignment(SearchState currentState, Task task, Slot slot) {
        if (task.isUnwantedSlot(slot)) {
            return currentState;
        }

        // Check if this is a valid assignment considering linked days
        if (!isValidDayAssignment(task, slot, currentState)) {
            return currentState;
        }

        Assignment newAssignment = new Assignment(task, slot);
        if (!hardChecker.validate(newAssignment)) {
            return currentState;
        }

        SearchState newState = currentState.clone();
        List<Assignment> newAssignments = newState.getAssignments();
        newState.addAssignment(newAssignment);
        newState.updateRemainingSlots(slot);

        // Remove assigned task
        List<Task> remainingTasks = newState.getRemainingTask();
        remainingTasks.removeIf(t -> t.getIdentifier().equals(task.getIdentifier()));

        // Recalculate penalty
        int penalty = newState.getPenalty();
        newState.setPenalty(penalty + softChecker.calculatePenalty(newAssignments));

        return newState;
    }

    private boolean isValidDayAssignment(Task task, Slot slot, SearchState currentState) {
        String day = slot.getDay();
        
        // Check linked days pattern
        switch (day) {
            case "MO":
                // For Monday, if WE/FR slots don't exist in input, we should still allow the assignment
                List<Slot> wedSlots = findSlotsByDayAndTime("WE", slot.getStartTime(), slot.forGame());
                List<Slot> friSlots = findSlotsByDayAndTime("FR", slot.getStartTime(), slot.forGame());
                // Only check availability if the slots exist in input
                return (wedSlots.isEmpty() && friSlots.isEmpty()) || 
                       hasAvailableLinkedSlots(currentState, task, "WE", "FR", slot.getStartTime());
            case "TU":
                // For Tuesday, if TH slots don't exist in input, we should still allow the assignment
                List<Slot> thuSlots = findSlotsByDayAndTime("TH", slot.getStartTime(), slot.forGame());
                return thuSlots.isEmpty() || 
                       hasAvailableLinkedSlots(currentState, task, "TH", null, slot.getStartTime());
            case "WE":
                // For Wednesday, if FR slots don't exist in input, we should still allow the assignment
                List<Slot> fridaySlots = findSlotsByDayAndTime("FR", slot.getStartTime(), slot.forGame());
                return fridaySlots.isEmpty() || 
                       hasAvailableLinkedSlots(currentState, task, "FR", null, slot.getStartTime());
            default:
                return true;
        }
    }

    private boolean hasAvailableLinkedSlots(SearchState state, Task task, String day1, String day2, String time) {
        if (state == null || task == null || day1 == null || time == null) {
            return false;
        }

        boolean day1Available = state.getAvailableSlots().stream()
                .anyMatch(s -> s.getDay().equals(day1) && 
                             s.getStartTime().equals(time) && 
                             s.forGame() == task.getIsGame());
        
        if (day2 == null) return day1Available;
        
        boolean day2Available = state.getAvailableSlots().stream()
                .anyMatch(s -> s.getDay().equals(day2) && 
                             s.getStartTime().equals(time) && 
                             s.forGame() == task.getIsGame());
        
        return day1Available && day2Available;
    }

    private List<SearchState> generateNextStates(SearchState state, Task task) {
        List<SearchState> states = new ArrayList<>();
        
        List<Slot> availableSlots = new ArrayList<>(state.getAvailableSlots());            
        
        for (Slot slot : availableSlots) {           
            if (slot.forGame() != task.getIsGame())continue;     
            SearchState newState = transitLinkedAssignment(state, task, slot);
            if (!newState.equals(state)) {
                states.add(newState);
            } 
        }
        //System.out.println("=================================generating next states end================================");
        return states;
    }

    public void search() {
        // Start from the initial state
        dfs(state);
        
        
        // After DFS, print the best state found
        if (lastState != null) {
            System.out.println("Best solution found with penalty: " + minEval);
            System.out.println("--------------------------------------------");
            lastState.printState();
            System.out.println("--------------------------------------------");
        } else {
            System.out.println("No solution found.");
        }
    }
    
    private void dfs(SearchState current) {
        if (current.getRemainingTask().isEmpty()) {
            if (current.getPenalty() < minEval) {
                minEval = current.getPenalty();
                lastState = current;
                System.out.println("Found better solution with penalty: " + minEval);
            }
            return;
        }

        // Only prune if penalty is significantly worse
        if (current.getPenalty() > minEval * 1.5) {
            return;
        }

        Task nextTask = current.getRemainingTask().get(0);
        List<SearchState> nextStates = generateNextStates(current, nextTask);

        if (nextStates.isEmpty()) {
            System.out.println("Warning: No valid next states found for task: " + nextTask.getIdentifier());
        }

        // Sort states by penalty to try most promising first
        nextStates.sort(Comparator.comparingInt(SearchState::getPenalty));

        for (SearchState nextState : nextStates) {
            dfs(nextState);
        }
    }
    
    private void makePairList() {
        List<Constraint> pairConstraints = constraints.get("Pairs");

        if (pairConstraints == null || pairConstraints.isEmpty()) {
            return;
        }

        for (Constraint constraint : pairConstraints) {
            Pair nc = (Pair) constraint;

            Task task1 = findTaskByIdentifier(nc.getTeam1Id());
            Task task2 = findTaskByIdentifier(nc.getTeam2Id());
            
            // Add first task if exists and not already in list
            if (task1 != null) {
                if (!notCompatibles.contains(task1)) {
                    notCompatibles.add(task1);
                }
            }
            // Add second task if exists and not already in list
            if (task2 != null) {
                if (!notCompatibles.contains(task2)) {
                    notCompatibles.add(task2);
                }
            }
        }
    }

    private void makeNotCompatibleList() {
        List<Constraint> notCompatibleConstraints = constraints.get("NotCompatible");
        
        if (notCompatibleConstraints == null || notCompatibleConstraints.isEmpty()) {
            return;
        }
        
    
        // For each not compatible constraint
        for (Constraint constraint : notCompatibleConstraints) {
            NotCompatible nc = (NotCompatible) constraint;

            Task task1 = findTaskByIdentifier(nc.getTeam1Id());
            Task task2 = findTaskByIdentifier(nc.getTeam2Id());
            
            // Add first task if exists and not already in list
            if (task1 != null) {
                if (!notCompatibles.contains(task1)) {
                    notCompatibles.add(task1);
                }
            }
            // Add second task if exists and not already in list
            if (task2 != null) {
                if (!notCompatibles.contains(task2)) {
                    notCompatibles.add(task2);
                }
            }
        }
    }
}

