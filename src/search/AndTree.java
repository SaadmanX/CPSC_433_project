package search;

import model.Assignment;
import model.SearchState;
import model.constraints.Constraint;
import model.constraints.PartialAssignment;
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
import java.util.PriorityQueue;
import constraints.HardConstraintsEval;
import constraints.SoftConstraintsEval;

public class AndTree {
    private SearchState state;
    private HashMap<String, List<Constraint>> constraints = new HashMap<>(); 
    private InputParser parser = new InputParser();
    private String inputFileName;
    private List<PartialAssignment> partialAssignments = new ArrayList<>();
    private List<Unwanted> unwantedList = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();
    private List<Slot> allSlots = new ArrayList<>();
    private Map<Slot, List<Slot>> linkedSlotGroups = new HashMap<>();
    SearchState lastState;
    HardConstraintsEval hardChecker = new HardConstraintsEval();
    SoftConstraintsEval softChecker;

    public AndTree(SearchState root, String filename, ArrayList<Integer> weightList, ArrayList<Integer> multiplierList) {
        this.state = root;
        this.inputFileName = filename;
        softChecker = new SoftConstraintsEval(multiplierList, weightList);
    }

    private void parseInput(){
        try {
            parser.parseFile(this.inputFileName);

            parser.parseGameSlots();
            parser.parsePracticeSlots();

            parser.parseGames();
            parser.parsePractices();
            
            constraints.put("NotCompatible", parser.parseNotCompatible());
            constraints.put("Pairs", parser.parsePairs());
            constraints.put("Preferences", parser.parsePreferences());
            unwantedList = parser.parseUnwanted();
            partialAssignments  = parser.parsePartialAssignments();

            allTasks = parser.getAllTasks();
            allSlots = parser.getAllSlots();
            
            state.setRemainingSlots(allSlots);
            state.setRemainingTask(allTasks);

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

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }

    
    public void preprocess() {
        parseInput();
        buildLinkedSlots();
        assignPartialAssignments();
        assignUnwanted();

        // Validate partial assignments against the current state
        HardConstraintsEval hardChecker = new HardConstraintsEval();
        if (!hardChecker.validatePartialAssignmentsForState(partialAssignments, state)) {
            throw new IllegalStateException("Preprocessing failed: Partial assignments are not satisfied in the current state.");
        }
    }

    private void assignUnwanted(){
        for (Unwanted unwanted : unwantedList) {
            Task task = findTaskByIdentifier(unwanted.getTaskIdentifier());
            Slot slot = findSlotByDayAndTime(unwanted.getDay(), unwanted.getTime(), task.getIsGame());
            if (task != null && slot != null) {
                task.addUnwantedSlot(slot);
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
                    linkedSlots.addAll(findSlotsByDayAndTime("WE", slot.getStartTime()));
                    linkedSlots.addAll(findSlotsByDayAndTime("FR", slot.getStartTime()));
                }
                case "TU" -> linkedSlots.addAll(findSlotsByDayAndTime("TH", slot.getStartTime()));
                case "WE" -> linkedSlots.addAll(findSlotsByDayAndTime("FR", slot.getStartTime()));
                default -> {
                }
            }

            linkedSlotGroups.put(slot, linkedSlots);
        }
    }

    private List<Slot> findSlotsByDayAndTime(String day, String time) {
        return allSlots.stream()
                .filter(slot -> slot.getDay().equals(day) && slot.getStartTime().equals(time))
                .toList();
    }

    private SearchState transitLinkedAssignment(SearchState state, Task task, Slot slot) {
        // Check if the slot is in the unwanted list for the current task
        if (task.isUnwantedSlot(slot)) {
            return state;
        }
        // Retrieve linked slots for the given slot
        List<Slot> linkedSlots = linkedSlotGroups.getOrDefault(slot, Collections.emptyList());
        List<Assignment> linkedAssignments = new ArrayList<>();
    
        // Add the primary assignment
        linkedAssignments.add(new Assignment(task, slot));
    
        // Add linked assignments while checking availability
        for (Slot linkedSlot : linkedSlots) {
            if (!state.getAvailableSlots().contains(linkedSlot)) {
                // If any linked slot is unavailable, the assignment fails
                return state;
            }
            linkedAssignments.add(new Assignment(task, linkedSlot));
        }
    
        // Validate all assignments together
        List<Assignment> newAssignments = new ArrayList<>(state.getAssignments());
        newAssignments.addAll(linkedAssignments);
        if (!hardChecker.validate(newAssignments)) {
            return state; // Return the original state if validation fails
        }
    
        // Create a new state with updated assignments and penalties
        SearchState newState = state.clone();
        newState.getAssignments().addAll(linkedAssignments);
    
        // Remove assigned slots from availability
        for (Slot assignedSlot : linkedAssignments.stream().map(Assignment::getSlot).toList()) {
            newState.updateRemainingSlots(assignedSlot);
        }
    
        // Recalculate the penalty for the new state
        newState.setPenalty(softChecker.calculatePenalty(newState.getAssignments()));
        newState.printState();
        return newState;
    }
    
    private void assignPartialAssignments() {
        for (PartialAssignment partial : partialAssignments) {
            Task task = findTaskByIdentifier(partial.getTaskIdentifier());
            Slot slot = findSlotByDayAndTime(partial.getDay(), partial.getTime(), task.getIsGame());
            if (task != null && slot != null) {
                state = transitLinkedAssignment(state, task, slot);
            }
        }
    }


    public void search() {
        PriorityQueue<SearchState> queue = new PriorityQueue<>(Comparator.comparingInt(SearchState::getPenalty));
        queue.add(state);

        while (!queue.isEmpty()) {
            SearchState current = queue.poll();

            // Check if this is a valid solution
            if (hardChecker.validate(current.getAssignments()) && current.getRemainingTask().isEmpty()) {
                System.out.println("Solution Found!");
                current.printState();
                return;
            }

            // Generate the next states
            for (Task task : current.getRemainingTask()) {
                List<SearchState> nextStates = generateNextStates(current, task);
                queue.addAll(nextStates);
            }
        }
        System.out.println("No solution found.");
    }

    private List<SearchState> generateNextStates(SearchState state, Task task) {
        List<SearchState> states = new ArrayList<>();
        for (Slot slot : state.getAvailableSlots()) {
            // Use the unified linked assignment logic
            SearchState newState = transitLinkedAssignment(state, task, slot);
            if (!newState.equals(state)) {
                states.add(newState);
            }
        }
        return states;
    }

    
    //TODO: fleaf
    public void chooseNext(){
        //With remaining Task, propagates possibilities and choose the appropriate one
    }
}