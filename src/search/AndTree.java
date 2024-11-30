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
    private List<Preference> preferencesList = new ArrayList<>();
    private List<Task> notCompatibles = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();
    private List<Slot> allSlots = new ArrayList<>();
    private Map<Slot, List<Slot>> linkedSlotGroups = new HashMap<>();
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
            constraints.put("Pairs", parser.parsePairs());
            makePairList();

            preferencesList = parser.parsePreferences();
            unwantedList = parser.parseUnwanted();
            partialAssignments  = parser.parsePartialAssignments();

            //DEBUGGING
            //parser.parseNotCompatible().forEach(System.out::println);
            //parser.parseUnwanted().forEach(System.out::println);
            //parser.parsePairs().forEach(System.out::println);
            //parser.parsePartialAssignments().forEach(System.out::println);
            //parser.parsePreferences().forEach(System.out::println);

            //parser.parseGameSlots().forEach(System.out::println);
            //parser.parseGames().forEach(System.out::println);
            //parser.parsePracticeSlots().forEach(System.out::println);
            //parser.parsePractices().forEach(System.out::println);
            softChecker = new SoftConstraintsEval(multiplierList, weightList, preferencesList, constraints.get("Pairs"), allSlots);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }


    public void preprocess() {
        parseInput();
        buildLinkedSlots();

        // System.out.println("initial state before preprocess");
        // state.printState();

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

    private List<Slot> findSlotsByDayAndTime(String day, String time, boolean forGame) {
        return allSlots.stream()
                .filter(slot -> slot.getDay().equals(day) && slot.getStartTime().equals(time) 
                && slot.forGame() == forGame)
                .toList();
    }

    private SearchState transitLinkedAssignment(SearchState state, Task task, Slot slot) {
        if (task.isUnwantedSlot(slot)) {
            return state;
        }
        List<Slot> linkedSlots = linkedSlotGroups.getOrDefault(slot, Collections.emptyList());
        List<Assignment> linkedAssignments = new ArrayList<>();
        linkedAssignments.add(new Assignment(task, slot));

        for (Slot linkedSlot : linkedSlots) {
            if (!state.getAvailableSlots().contains(linkedSlot)) {
                return state;
            }
            linkedAssignments.add(new Assignment(task, linkedSlot));
        }

        List<Assignment> newAssignments = new ArrayList<>(state.getAssignments());
        newAssignments.addAll(linkedAssignments);
        if (!hardChecker.validate(newAssignments)) {
            return state; // Return the original state if validation fails
        }

        // Create a new state with updated assignments and penalties
        SearchState newState = state.clone();
        newState.setAssignments(newAssignments);

        // Remove assigned slots from availability
        for (Slot assignedSlot : linkedAssignments.stream().map(Assignment::getSlot).toList()) {
            newState.updateRemainingSlots(assignedSlot);
        }

        //Remove slots and tasks
        List<Task> remainingTask = newState.getRemainingTask();
        remainingTask.remove(task);
        newState.setRemainingTask(remainingTask);

        // Recalculate the penalty for the new state
        newState.setPenalty(softChecker.calculatePenalty(newState.getAssignments()));
        return newState;
    }

    // Faster tracking of assignPartial
    private boolean assignPartialAssignments() {
        for (PartialAssignment partial : partialAssignments) {
            Task task = findTaskByIdentifier(partial.getTaskIdentifier());
            Slot slot = findSlotByDayAndTime(partial.getDay(), partial.getTime(), task.getIsGame());
           // System.out.println(task);
           // System.out.println(slot);
            if (task != null && slot != null) {
                if (state.equals(transitLinkedAssignment(state, task, slot))){
                    return false;
                }
            }
        }

        return true;
    }


    private List<SearchState> generateNextStates(SearchState state, Task task) {
        List<SearchState> states = new ArrayList<>();
        
        List<Slot> availableSlots = new ArrayList<>(state.getAvailableSlots());            
        
        for (Slot slot : availableSlots) {                
            SearchState newState = transitLinkedAssignment(state, task, slot);
            if (!newState.equals(state)) {
                states.add(newState);
            } 
        }
        System.out.println("=================================generating next states end================================");
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
        System.out.println("------------Current State-------------------");
        current.printState();
        System.out.println("--------------------------------------------");

        if (current.getRemainingTask().isEmpty()) {
            System.out.println("Reached leaf node.");
            if (hardChecker.validate(current.getAssignments())) {
                if (current.getPenalty() <= minEval) {
                    System.out.println("New best state with penalty: " + current.getPenalty());
                    minEval = current.getPenalty();
                    lastState = current;
                }
            }
            return;
        }
    
        // Prune states with penalty worse than the best solution
        if (current.getPenalty() > minEval) {
            return;
        }
    
        Task nextTask = current.getRemainingTask().get(0);
        System.out.println(nextTask);

        List<SearchState> nextStates = generateNextStates(current, nextTask);
    
        for (SearchState nextState : nextStates) {
            dfs(nextState); // Recursive DFS call
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

