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
    // private HashMap<String, List<Constraint>> constraints = new HashMap<>(); 
    private InputParser parser = new InputParser();
    private String inputFileName;
    private List<PartialAssignment> partialAssignments = new ArrayList<>();
    private List<Unwanted> unwantedList = new ArrayList<>();
    private List<Pair> pairList = new ArrayList<>();
    private List<Preference> preferencesList = new ArrayList<>();
    private List<Task> notCompatibles = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();
    private List<Slot> allSlots = new ArrayList<>();
    SearchState lastState;
    HardConstraintsEval hardChecker;
    SoftConstraintsEval softChecker;
    private int minEval = Integer.MAX_VALUE;
    ArrayList<Integer> weightList;
    ArrayList<Integer> multiplierList;
    private Map<Slot, List<Slot>> linkedSlotGroups = new HashMap<>();
    private Map<Slot, List<Task>> slotMap = new HashMap<>();

    public AndTree(SearchState root, String filename, ArrayList<Integer> weightList, ArrayList<Integer> multiplierList) {
        this.state = root;
        this.inputFileName = filename;
        this.weightList = weightList;
        this.multiplierList = multiplierList;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OutputHandler.writeToFile(lastState, inputFileName);
        }));
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

            // constraints.put("NotCompatible", parser.parseNotCompatible());
            makeNotCompatibleList();

            // makePairList();
            pairList = parser.parsePairs();
            preferencesList = parser.parsePreferences();
            unwantedList = parser.parseUnwanted();
            partialAssignments  = parser.parsePartialAssignments();

            softChecker = new SoftConstraintsEval(multiplierList, weightList, preferencesList, pairList, allSlots);
            state.setPenalty(softChecker.initialPenalty);

            hardChecker = new HardConstraintsEval(allTasks, allSlots);


        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }


    public void preprocess() {
        parseInput();
        buildLinkedSlots();

        if (!assignPartialAssignments()){
            System.out.println("FAILED WITH PARTIAL");
            System.exit(1);
        }
        assignPreferences();
        assignUnwanted();
        // buildSlotMap();
        System.out.println(slotMap);


    }

    // private void buildSlotMap() {
    //     for (Assignment a : state.getAssignments()) {
    //         Slot s = a.getSlot();
    //         slotMap.put(s, s.getAssignedTasks());
    //     }
    // }

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
                SearchState newestState = transitLinkedAssignment(state, task, slot, slotMap);
                if (state.equals(newestState)){
                    return false;
                }
                state = newestState;
            }
        }
        return true;
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

        // System.out.println(linkedSlotGroups);

    }

    private void printSlotMap(Map<Slot, List<Task>> sMap) {
        System.out.println("\n=== Slot Map Status ===");
        for (Map.Entry<Slot, List<Task>> entry : sMap.entrySet()) {
            Slot slot = entry.getKey();
            List<Task> tasks = entry.getValue();
            
            System.out.printf("Slot [%s %s] (max: %d, current: %d):\n", 
                slot.getDay(), 
                slot.getStartTime(),
                slot.getMax(),
                tasks.size()
            );
            
            if (tasks.isEmpty()) {
                System.out.println("  No tasks assigned");
            } else {
                for (Task task : tasks) {
                    System.out.printf("  - %s\n", task.getIdentifier());
                }
            }
            System.out.println();
        }
        System.out.println("=====================\n");
    }
    

    private List<Slot> findSlotsByDayAndTime(String day, String time, boolean forGame) {
        return allSlots.stream()
                .filter(slot -> slot.getDay().equals(day) && slot.getStartTime().equals(time) 
                && slot.forGame() == forGame)
                .toList();
    }

    private SearchState transitLinkedAssignment(SearchState currentState, Task task, Slot slot, Map<Slot, List<Task>> sMap) {
        
        if (task.isUnwantedSlot(slot)) {
            return currentState;
        }

        if (sMap.getOrDefault(slot, new ArrayList<>()).size() >= slot.getMax()) {
            return currentState;        
        }

        if (task.getIsGame() && slot.getDay().equals("TU") && slot.getStartTime().equals("11:00")){
            return currentState;
        }


        List<Slot> linkedSlots = linkedSlotGroups.getOrDefault(slot, Collections.emptyList());
        List<Assignment> linkedAssignments = new ArrayList<>();
        linkedAssignments.add(new Assignment(task, slot));

        for (Slot linkedSlot : linkedSlots) {
            if (!currentState.getAvailableSlots().contains(linkedSlot)) {
                return currentState;
            }
            if (sMap.getOrDefault(linkedSlot, new ArrayList<>()).size() >= linkedSlot.getMax()) {
                return currentState;
            }

            linkedAssignments.add(new Assignment(task, linkedSlot));
        }

        List<Assignment> newAssignments = new ArrayList<>(currentState.getAssignments());
        newAssignments.addAll(linkedAssignments);

        for (Assignment a : linkedAssignments) {
            if (!hardChecker.validate(a)) {
                return currentState; // Return the original state if validation fails
            }
            
            Slot s = a.getSlot();
            sMap.computeIfAbsent(s, k -> new ArrayList<>()).add(a.getTask());

            printSlotMap(sMap);

            // Create a new state with updated assignments and penalties
            SearchState newState = currentState.clone();
            newState.setAssignments(newAssignments);

            // Remove assigned slots from availability
            for (Slot assignedSlot : linkedAssignments.stream().map(Assignment::getSlot).toList()) {
                newState.updateRemainingSlots(assignedSlot);
            }

            //Remove slots and tasks
            List<Task> remainingTasks = newState.getRemainingTask();
            for (int i = 0; i < remainingTasks.size(); i++){
                Task cult = remainingTasks.get(i);
                if (cult.getIdentifier().equals(task.getIdentifier()))remainingTasks.remove(cult);
            }

            // Recalculate the penalty for the new state, huh... so this is the only time called
            int penalty = newState.getPenalty();
            newState.setPenalty(penalty + softChecker.calculatePenalty(newAssignments));
            return newState;
        }

    return state;
    }


    // private SearchState transitLinkedAssignment(SearchState currentState, Task task, Slot slot) {

    //     if (task.isUnwantedSlot(slot)) {
    //         return currentState;
    //     }


    //     if (task.getIsGame() && slot.getStartTime().equals("11:00")){
    //         return currentState;
    //     }

    //     Slot copySlot = new Slot(slot);

    //     Assignment newAssignment = new Assignment(task, copySlot);

    //     // task.setCurrentAssign(slot);

    //     if (!hardChecker.validate(newAssignment)){
    //         // update this for task -> slot
    //         // slot.setCurrentCount(slot.getCurrentCount() - 1);
    //         return currentState;
    //     }

    //     SearchState newState = currentState.clone();
    //     List<Assignment> newAssignments = newState.getAssignments();
    //     newState.addAssignment(newAssignment);
    //     newState.updateRemainingSlots(copySlot);
    //     slot.addAssignedTask(task);

    //     // System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    //     // System.out.println("Successfully assigned task: " + task);
    //     // System.out.println(copySlot);

    //     // // System.out.println("current count of this slot is: " + copySlot.getCurrentCount() + " and max is: " + copySlot.getMax());
    //     // System.out.println("tasks assigned to this slot: ");
    //     // for (Task t : copySlot.getAssignedTasks()) {
    //     //     System.out.println(t);
    //     // }
    //     // // System.out.println(copySlot);
    //     // System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");


    //         //Remove slots and tasks
    //     List<Task> remainingTasks = newState.getRemainingTask();
    //     for (int i = 0; i < remainingTasks.size(); i++){
    //         Task cult = remainingTasks.get(i);
    //         if (cult.getIdentifier().equals(task.getIdentifier())) {
    //             remainingTasks.remove(cult);
    //             // slot.setCurrentCount(slot.getCurrentCount() - 1);
    //         }
    //     }



    //     // Recalculate the penalty for the new state, huh... so this is the only time called
    //     int penalty = newState.getPenalty();
    //     newState.setPenalty(penalty + softChecker.calculatePenalty(newAssignments));
    //     return newState;
    // }



    private List<SearchState> generateNextStates(SearchState state, Task task, Map<Slot, List<Task>> sMap) {
        List<SearchState> states = new ArrayList<>();
        
        List<Slot> availableSlots = new ArrayList<>(state.getAvailableSlots());            
        
        for (Slot slot : availableSlots) {      
            if (slot.forGame() != task.getIsGame())continue;     
            SearchState newState = transitLinkedAssignment(state, task, slot, sMap);
            if (!newState.equals(state)) {
                states.add(newState);
            }
        }
        return states;
    }

    public void search() {

        // Start from the initial state
        dfs(state, slotMap);
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
    
    private void dfs(SearchState current, Map<Slot, List<Task>> sMap) {
        System.out.println("------------Current State with number of remaining tasks: " + current.getRemainingTask().size() + "-------------------");
        current.printState();
        System.out.println("--------------------------------------------");


        if (current.getRemainingTask().isEmpty()) {
            System.out.println("REACHED LEAF NODE.");
            if (current.getPenalty() < minEval) {
                System.out.println("New best state with penalty: " + current.getPenalty());
                minEval = current.getPenalty();
                lastState = current;
                return;
            }
            return;
        }
    
        // Prune states with penalty worse than the best solution
        if (current.getPenalty() > minEval) {
            return;
        }

        Task nextTask = current.getRemainingTask().get(0);
        // System.out.println(nextTask);

        List<SearchState> nextStates = generateNextStates(current, nextTask, sMap);
    
        for (SearchState nextState : nextStates) {
            Map<Slot, List<Task>> newMap = new HashMap<>();
            for (Map.Entry<Slot, List<Task>> entry : sMap.entrySet()) {
                newMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            dfs(nextState, newMap);


            // dfs(nextState, sMap); // Recursive DFS call
        }
    }

    
//     private void makePairList() {
//         List<Constraint> pairConstraints = constraints.get("Pairs");

//         if (pairConstraints == null || pairConstraints.isEmpty()) {
//             return;
//         }

//         for (Constraint constraint : pairConstraints) {
//             Pair nc = (Pair) constraint;

//             Task task1 = findTaskByIdentifier(nc.getTeam1Id());
//             Task task2 = findTaskByIdentifier(nc.getTeam2Id());
            
//             // Add first task if exists and not already in list
//             if (task1 != null) {
//                 if (!notCompatibles.contains(task1)) {
//                     notCompatibles.add(task1);
//                 }
//             }
//             // Add second task if exists and not already in list
//             if (task2 != null) {
//                 if (!notCompatibles.contains(task2)) {
//                     notCompatibles.add(task2);
//                 }
//             }
//         }
//     }

    private void makeNotCompatibleList() {
        List<Constraint> notCompatibleConstraints = parser.parseNotCompatible();
        
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
                task1.addNotCompatible(task2.getIdentifier());
            }
            // Add second task if exists and not already in list
            if (task2 != null) {
                task2.addNotCompatible(task1.getIdentifier());
            }
        }
    }
}

