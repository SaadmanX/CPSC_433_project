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
    private List<Preference> preferencesList = new ArrayList<>();
    private List<Task> notCompatibles = new ArrayList<>();
    private List<Task> pairs = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();
    private List<Slot> allSlots = new ArrayList<>();
    private Map<Slot, List<Slot>> linkedSlotGroups = new HashMap<>();
    SearchState lastState;
    HardConstraintsEval hardChecker = new HardConstraintsEval();
    SoftConstraintsEval softChecker;
    private boolean backtracking = false;
    private int minEval = Integer.MAX_VALUE;

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

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }


    public void preprocess() {
        parseInput();
        buildLinkedSlots();
        assignPartialAssignments();
        assignPreferences();
        assignUnwanted();

        // Validate partial assignments against the current state
        HardConstraintsEval hardChecker = new HardConstraintsEval();
        if (!hardChecker.validatePartialAssignmentsForState(partialAssignments, state)) {
            System.out.println("FAILED WITH PARTIAL");
            System.exit(1);
            //throw new IllegalStateException("Preprocessing failed: Partial assignments are not satisfied in the current state.");
        }

        search();
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
            System.out.println(task);
            System.out.println(slot);
            if (task != null && slot != null) {
                state = transitLinkedAssignment(state, task, slot);
            }
        }
    }


    // public void search() {
    //     PriorityQueue<SearchState> queue = new PriorityQueue<>(Comparator.comparingInt(SearchState::getPenalty));
    //     queue.add(state);

    //     while (!queue.isEmpty()) {
    //         SearchState current = queue.poll();

    //         // Check if this is a valid solution
    //         //* what is the condition for the solution?? there was a trail of thought that I forgot I had before. */
    //         if (hardChecker.validate(current.getAssignments()) && current.getRemainingTask().isEmpty()) {
    //             System.out.println("Solution Found!");
    //             current.printState();
    //             return;
    //         }

    //         // Generate the next states
    //         // for (Task task : current.getRemainingTask()) {
    //         //     List<SearchState> nextStates = generateNextStates(current, task);
    //         //     queue.addAll(nextStates);
    //         // }

    //         chooseNext(current, queue);
    //     }
    //     System.out.println("No solution found.");
    // }

    // private List<SearchState> generateNextStates(SearchState state, Task task) {
    //     List<SearchState> states = new ArrayList<>();
    //     for (Slot slot : state.getAvailableSlots()) {
    //         // Use the unified linked assignment logic
    //         SearchState newState = transitLinkedAssignment(state, task, slot);
    //         if (!newState.equals(state)) {
    //             newState.setPenalty(softChecker.calculatePenalty(newState.getAssignments()));
    //             states.add(newState);
    //         }
    //     }
    //     return states;
    // }

    private List<SearchState> generateNextStates(SearchState state, Task task) {
        List<SearchState> states = new ArrayList<>();
        
        System.out.println("\nGenerating next states for task: " + task.getIdentifier());
        System.out.println("Current available slots size: " + state.getAvailableSlots().size());
        
        try {
            // Create a new list to avoid concurrent modification
            List<Slot> availableSlots = new ArrayList<>(state.getAvailableSlots());
            System.out.println("Copied available slots size: " + availableSlots.size());
            
            for (Slot slot : availableSlots) {
                System.out.println("\nTrying slot: " + slot.toString());
                
                // Use the unified linked assignment logic
                System.out.println("Attempting transitLinkedAssignment...");
                SearchState newState = transitLinkedAssignment(state, task, slot);
                
                System.out.println("After transitLinkedAssignment:");
                System.out.println("New state equals current state? " + newState.equals(state));
                
                if (!newState.equals(state)) {
                    System.out.println("Adding new state to states list");
                    System.out.println("New state assignments size: " + newState.getAssignments().size());
                    System.out.println("New state remaining tasks size: " + newState.getRemainingTask().size());
                    System.out.println("New state available slots size: " + newState.getAvailableSlots().size());
                    states.add(newState);
                } else {
                    System.out.println("Skipping state as it equals current state");
                }
            }
            
            System.out.println("\nGenerated " + states.size() + " new states");
            
        } catch (Exception e) {
            System.out.println("Exception caught in generateNextStates:");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Exception message: " + e.getMessage());
            e.printStackTrace();
        }
        
        return states;
    }
    
    /*
     * this area is being used for notes for myself. read if you want, but the stuff here is pretty messed up
     */

    /*
     * my idea on how the flow should go:
     *  a new queue is started with the preassignments state (or empty state if preassign is empty)
     *  take the beginning of the queue and set it as current state to check
     *  check for hardconstraint, and see if the current state is the solution
     *  otherwise, and here comes the fun part, CHOOSENEXT STATE
     *      order states in terms of priority of tasks present in some of constraint lists.
     *          for the sake of simplicity, lets assume that for every task added in the queue from current state, the penalty of the next state will be as follows:
     *              if task taken from unwanted: 5
     *              if task taken from compatible: 4
     *              if task taken from preference: 3
     *              if task taken from pair: 2
     *              if task taken from none (remaining tasks): 1
     *      loop through queue, keep adding states and checking for solution, marking them as valid or invalid solutions, and BACKTRACKING
     *      for this context of using queues, I am backtracking using "continue", essentially going to the next available state added before the after state
     */

    public void search() {
        System.out.println("===========================STARTED SEARCH PROCESS=================================");
        PriorityQueue<SearchState> queue = new PriorityQueue<>(Comparator.comparingInt(SearchState::getPenalty));
        queue.add(state);
    
        while (!queue.isEmpty()) {
            SearchState current = queue.poll();
            System.out.println("current state at head: ");
            current.printState();

            // Check if this is a valid complete solution
            if (current.getRemainingTask().isEmpty()) {
                System.out.println("current state has no remaining tasks");
                if (hardChecker.validate(current.getAssignments())) {
                    System.out.println("current state is valid through hard constraints");
                    if (current.getPenalty() < minEval) {
                        System.out.println("current state is the lead state for the search process");
                        minEval = current.getPenalty();
                        lastState = current;
                        System.out.println("--------------current min eval: " + Integer.toString(minEval) + " ------------------------");
                    }
                }
                System.out.println("backtracking to next state");
                System.out.println("maybe causing problems here. just a hunch");
                // Backtrack by continuing to next state in queue
                continue;
            }
    
            // If current penalty is already worse than best found, backtrack
            if (current.getPenalty() >= minEval) {
                System.out.println("current state may be worse than lead state. so no use going forward. backtrack");
                continue;
            }
    
            // Generate and add all possible next states to maintain completeness
            List<Task> remainingTasks = current.getRemainingTask();
            Task nextTask = selectNextTask(remainingTasks);
            
            // Generate all possible states for this task
            List<SearchState> nextStates = generateNextStates(current, nextTask);
            
            if (nextStates.isEmpty()) {
                // No valid moves for this task, backtrack
                System.out.println("no next state found from current state, so backtracking");
                continue;
            }
    
            // Add all possible states to queue
            // This allows backtracking by keeping alternative paths available
            System.out.println("adding all the nextstates in the queue");
            queue.addAll(nextStates);
        }
    
        if (lastState != null) {
            System.out.println("Best solution found with penalty: " + minEval);
            lastState.printState();
        } else {
            System.out.println("No solution found.");
        }
    }
        

    private Task selectNextTask(List<Task> remainingTasks) {
        // Try to find task in priority order
        // 1. Unwanted
        for (Task task : remainingTasks) {
            for (Unwanted u : unwantedList) {
                if (u.getTaskIdentifier().equals(task.getIdentifier())) {
                    System.out.println("Selected unwanted task: ");
                    System.out.println(task);
                    return task;
                }
            }
        }
    
        // 2. NotCompatible
        for (Task task : remainingTasks) {
            if (notCompatibles.contains(task)) {
                System.out.println("Selected not compatible task: ");
                System.out.println(task);

                return task;
            }
        }
    
        // 3. Preference
        for (Task task : remainingTasks) {
            for (Preference p : preferencesList) {
                if (p.getTaskIdentifier().equals(task.getIdentifier())) {
                    System.out.println("Selected preferred task: ");
                    System.out.println(task);
                    return task;
                }
            }
        }
    
        // 4. Pairs
        for (Task task : remainingTasks) {
            if (pairs.contains(task)) {
                System.out.println("Selected paired task: ");
                System.out.println(task);
                return task;
            }
        }
    
        // 5. If no priority task found, return first remaining task
        System.out.println("Selected non priority remaining task: ");
        System.out.println(remainingTasks.get(0));
        return remainingTasks.get(0);
    }
    

    // private void chooseNext(SearchState current, PriorityQueue<SearchState> queue) {
    //     if (queue.isEmpty()) {
    //         // remaining tasks and remaining slots have already been checked when generating the states
    //         // if the last remaining state is not valid, set backtracking to true and set the penalty of the state high
    //         if (!hardChecker.validate(state.getAssignments())) {
    //             backtracking = true;
    //         }
    //         // minEval = Math.min(minEval, softChecker.calculatePenalty(state.getAssignments()));
    //         state.setPenalty(Integer.MAX_VALUE);
    //     }

    //     addUnwantedToQueue(current, queue);
    //     addNotCompatibleToQueue(current, queue);
    //     addPreferenceToQueue(current, queue);
    //     addPairToQueue(current, queue);
    //     addRemainingTasksToQueue(current, queue);
    //     // ** needs refinement for this. still not clear how the flow will work

    //     // when states is not empty, add states to the queue depending on the priority list
        
    // }

    // // check to see if any unwanted tasks are present in current tasks.
    // // if they are present, for each task, generate new states from the current state, and add them to the queue
    // private void addUnwantedToQueue(SearchState current, PriorityQueue<SearchState> queue) {
    //     for (Unwanted u : unwantedList) {
    //         for (Task t : current.getRemainingTask()) {
    //             if (u.getTaskIdentifier().equals(t.getIdentifier())) {
    //                 List<SearchState> nextStates = generateNextStates(current, t);
    //                 queue.addAll(nextStates);
    //             }
    //         }
    //     }
    // }

    // // check to see if any prefered assignment tasks are present in current tasks.
    // // if they are present, for each task, generate new states from the current state, and add them to the queue
    // private void addPreferenceToQueue(SearchState current, PriorityQueue<SearchState> queue) {
    //     for (Preference p : preferencesList) {
    //         for (Task t : current.getRemainingTask()) {
    //             if (p.getTaskIdentifier().equals(t.getIdentifier())) {
    //                 List<SearchState> nextStates = generateNextStates(current, t);
    //                 queue.addAll(nextStates);
    //             } 
    //         }
    //     }
    // }

    // private void addNotCompatibleToQueue(SearchState current, PriorityQueue<SearchState> queue) {
    //     for (Task c : notCompatibles) {
    //         for (Task t : current.getRemainingTask()) {
    //             if (c.getIdentifier().equals(t.getIdentifier())) {
    //                 List<SearchState> nextStates = generateNextStates(current, t);
    //                 queue.addAll(nextStates);
    //             } 
    //         }
    //     }
    // }

    // private void addPairToQueue(SearchState current, PriorityQueue<SearchState> queue) {
    //     for (Task p : pairs) {
    //         for (Task t : current.getRemainingTask()) {
    //             if (p.getIdentifier().equals(t.getIdentifier())) {
    //                 List<SearchState> nextStates = generateNextStates(current, t);
    //                 queue.addAll(nextStates);
    //             } 
    //         }
    //     }
    // }

    // private void addRemainingTasksToQueue(SearchState current, PriorityQueue<SearchState> queue) {
    //     for (Task t : current.getRemainingTask()) {
    //         List<SearchState> nextStates = generateNextStates(current, t);
    //         queue.addAll(nextStates);
    //     }
    // }

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

