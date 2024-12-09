package search;

import model.Assignment;
import model.SearchState;
import model.constraints.PartialAssignment;
import model.slots.Slot;
import model.task.Task;
import parser.InputParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

import constraints.HardConstraintsEval;
import constraints.SoftConstraintsEval;

public class AndTree {
    private SearchState state;
    // private HashMap<String, List<Constraint>> constraints = new HashMap<>(); 
    private InputParser parser = new InputParser();
    private String inputFileName;
    private List<PartialAssignment> partialAssignments = new ArrayList<>();
    //private List<Task> notCompatibles = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();
    private List<Slot> allSlots = new ArrayList<>();
    SearchState lastState;
    HardConstraintsEval hardChecker;
    SoftConstraintsEval softChecker;
    private int minEval = Integer.MAX_VALUE;
    ArrayList<Integer> weightList;
    ArrayList<Integer> multiplierList;
    private boolean isSpecialBooking = false;
    List<String> specialList = new ArrayList<>();    
    
    private int calculateHeuristic(SearchState state) {
        //So this measures the leftOver heuristics, with potentiall
        //min game fullfilled, pairing fulfilled (so not included), instead using another strategy
        //secDiff or pref ONLY will because it strictly limits to the actual assignment task-slot assignment

        int totalEstimatedPenalty = 0;

        //Other 2 will try these 2 other heuristics, which is used to rather reduce the penalty,
        //Because future assignments potentially reduce the penalty down
        //So, the number of best case scenarios for remaining minFilledPen
        // = Number of slots leftovers - number of unassigned tasks (in best case)

        int numberOfMinGamesLeftToFill = 0;
        int numberOfMinPracticeLeftToFill = 0;
        
        for (Assignment a: state.getAssignments()){
            if (a.getSlot().getMin() > a.getSlot().getCurrentCount()){
                if (!a.getSlot().forGame()){
                    int diff = parser.maxMinPractice - (a.getSlot().getMin() - a.getSlot().getCurrentCount());
                    numberOfMinPracticeLeftToFill  += diff;
                } else {
                    int diff = parser.maxMinGame - (a.getSlot().getMin() - a.getSlot().getCurrentCount());
                    numberOfMinGamesLeftToFill  += diff;
                }
            }
        }

        for (Task task: state.getRemainingTask()){
            if (task.getIsGame())numberOfMinGamesLeftToFill --;
            else numberOfMinPracticeLeftToFill--;
        }

        //Parallelism here, so if all (even non optimal go for it, it will be prioritized the same)
        //Have to set a cap of 0 for all
        if (numberOfMinPracticeLeftToFill > 0)totalEstimatedPenalty += weightList.get(1) * numberOfMinPracticeLeftToFill;

        if (numberOfMinGamesLeftToFill > 0)totalEstimatedPenalty += weightList.get(0) * numberOfMinGamesLeftToFill;

        System.out.println("Total estimated min game filled: " + totalEstimatedPenalty);

        //TODO: For pairing

        for (Task task : state.getRemainingTask()) {
            int minPenalty = Integer.MAX_VALUE;
    
            for (Slot slot : state.getAvailableSlots()) {
                if (slot.forGame() != task.getIsGame()) continue; 
                Assignment assignment = new Assignment(task, slot);
                List<Integer> penaltyList = softChecker.updatePenalty(assignment, state);
                int penalty = 0;

                penalty += penaltyList.get(1); //directly add Pref pen
                penalty += penaltyList.get(3); //directly add secDiff

                //There are other strategy in SearchState for the last 2:

                minPenalty = Math.min(minPenalty, penalty);
            }
    
            totalEstimatedPenalty += minPenalty;
        }
    
        return totalEstimatedPenalty;
    }
    

    // private int calculateHeuristic(SearchState state) {
    //     int heuristicPenalty = 0;
    
    //     // MinFilled: Estimate penalty for slots not meeting minimum requirements
    //     for (Slot slot : state.getAvailableSlots()) {
    //         int deficit = slot.getMin() - slot.getCurrentCount();
    //         int assignableCount = countAssignableTasks(slot, state.getRemainingTask());
    //         if (deficit > assignableCount) {
    //             heuristicPenalty += (deficit - assignableCount) * minFillWeight(slot);
    //         }
    //     }
    
    //     // Pairing: Estimate penalty for unpaired tasks
    //     for (Task task : state.getRemainingTask()) {
    //         int unpairedCount = countUnpairedTasks(task, state.getAssignments());
    //         heuristicPenalty += unpairedCount * pairPenaltyWeight;
    //     }
    
    //     // Preference: Assign penalty for unassigned tasks based on least preferred slot
    //     for (Task task : state.getRemainingTask()) {
    //         heuristicPenalty += calculateLeastPreferredSlotPenalty(task, state.getAvailableSlots());
    //     }
    
    //     // SecDiff: Predict overlap conflicts in slots
    //     for (Slot slot : state.getAvailableSlots()) {
    //         heuristicPenalty += calculateSectionConflictPenalty(slot, state.getRemainingTask());
    //     }
    
    //     return heuristicPenalty;
    // }
    

    public AndTree(SearchState root, String filename, ArrayList<Integer> multiplierList, ArrayList<Integer> weightList) {
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
            if (!parser.specialTasks.isEmpty()){
                specialList = parser.specialTasks;
                isSpecialBooking = true;
            }
            //System.out.println("IS SPECIAL BOOKING: " + isSpecialBooking);

            state.setRemainingSlots(allSlots);
            state.setRemainingTask(allTasks);

            parser.parseNotCompatible();

            parser.parsePairs();

            parser.parsePreferences();

            parser.parseUnwanted();

            partialAssignments  = parser.parsePartialAssignments();
            state.setMinGameFillPenalty(parser.maxMinGame * weightList.get(0) * multiplierList.get(0));
            state.setMinPracticeFillPenalty(parser.maxMinPractice * weightList.get(1) * multiplierList.get(0));
            state.setPairPenalty(parser.maxPairs * weightList.get(2) * multiplierList.get(2));
            state.setPrefPenalty(parser.maxPreferencesValue * multiplierList.get(1));

            state.updatePenalty();
            //System.out.println("MAX PENALTY = " + state.getPenalty());
            //System.out.println("MAX SECDIFF INITIAL= " + state.getPairPenalty());

            softChecker = new SoftConstraintsEval(multiplierList, weightList);

            hardChecker = new HardConstraintsEval();


        } catch (IOException e) {
            //System.err.println("Error reading file: " + e.getMessage());
        }

    }


    public void preprocess() {
        parseInput();
        if (!crossCheckIsSpecialBooking()){
            System.out.println("MISERABLY FAILED WITH SPECIAL BOOKING");
            System.exit(1);
        }

        if (!assignPartialAssignments()){
            System.out.println("FAILED WITH PARTIAL");
            System.exit(1);
        }
    }

    private boolean crossCheckIsSpecialBooking(){
        if (!isSpecialBooking) {
            return true;
        }
        Slot special18Slot = findSlotByDayAndTime("TU", "18:00", false);
        if (special18Slot == null){
            //System.out.println("line 120");
            return false;
        }
        //So it might print out partial fails if there is not enough 18:00 slot ahaha

        if (specialList.contains("CMSA U13T1S")){

            partialAssignments.add(new PartialAssignment("CMSA U13T1S", "TU", "18:00"));
        }
        if (specialList.contains("CMSA U12T1S")){
            partialAssignments.add(new PartialAssignment("CMSA U12T1S", "TU", "18:00"));
        }

        return true;
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

    private SearchState transitLinkedAssignment(SearchState currentState, Task task, Slot slot) {
        // Clone the task and slot
        Task clonedTask = new Task(task);  // Assuming a proper clone constructor
        Slot clonedSlot = new Slot(slot);  // Clone slot
    
        //No game for meeting here
        if (task.getIsGame() && slot.getDay().equals("TU") && slot.getStartTime().equals("11:00")){
            return currentState;
        }

        // Use cloned objects instead of the original ones
        if (clonedTask.isUnwantedSlot(clonedSlot)) {
            // System.out.println(slot);
            return currentState;
        }

        Assignment newAssignment = new Assignment(clonedTask, clonedSlot);

        if (!hardChecker.validate(newAssignment, currentState.getAssignments(), isSpecialBooking)){
            return currentState;
        }

        SearchState newState = currentState.clone();
        
        //Update assignment, task, and slot in newest state
        newState.addAssignment(newAssignment);
        newState.updateRemainingSlots(clonedSlot);
        newState.removeTask(clonedTask);
        
        List<Integer> newPenList = (softChecker.updatePenalty(newAssignment, newState));

        if (newPenList.get(0) != Integer.MAX_VALUE)
            if (task.getIsGame())newState.setMinGameFillPenalty(newPenList.get(0));
            else newState.setMinPracticeFillPenalty(newPenList.get(0));

        if (newPenList.get(1) != Integer.MAX_VALUE)newState.setPrefPenalty(newPenList.get(1));
        if (newPenList.get(2) != Integer.MAX_VALUE)newState.setPairPenalty(newPenList.get(2));
        if (newPenList.get(3) != Integer.MAX_VALUE)newState.setSecDiffPenalty(newPenList.get(3));

        newState.updatePenalty();

        newState.printState();
        return newState;
    }

    private List<SearchState> generateNextStates(SearchState state, Task task) {
        List<Slot> availableSlots = new ArrayList<>(state.getAvailableSlots());
        List<SearchState> states = Collections.synchronizedList(new ArrayList<>());
        int threadCount = Runtime.getRuntime().availableProcessors(); // Number of threads to use
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // Divide slots among threads
        int chunkSize = (int) Math.ceil((double) availableSlots.size() / threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < availableSlots.size(); i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, availableSlots.size());

            futures.add(executor.submit(() -> {
                for (int j = start; j < end; j++) {
                    Slot slot = availableSlots.get(j);

                    if (slot.forGame() != task.getIsGame()) continue;

                    SearchState newState = transitLinkedAssignment(state, task, slot);
                    if (!newState.equals(state)) {
                        states.add(newState);
                    }
                }
            }));
        }

        // Wait for all threads to finish
        try {
            for (Future<?> future : futures) {
                future.get(); // Wait for task completion
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown(); // Shut down the thread pool
        }

        // Sort states based on the heuristic value
        states.sort(Comparator.comparingInt(this::calculateHeuristic));

        return states;
    }

    
    // private List<SearchState> generateNextStates(SearchState state, Task task) {
    //     List<SearchState> states = new ArrayList<>();
        
    //     List<Slot> availableSlots = new ArrayList<>(state.getAvailableSlots());            
        
    //     for (Slot slot : availableSlots) {      
    //         if (slot.forGame() != task.getIsGame())continue;     
    //         SearchState newState = transitLinkedAssignment(state, task, slot);
    //         if (!newState.equals(state)) {
    //             states.add(newState);
    //         } 
    //     }

    //     // Sort states based on the heuristic value
    //     states.sort(Comparator.comparingInt(this::calculateHeuristic));

    //     return states;
    // }
    

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

    private void dfs(SearchState root) {
        // Create a thread pool for parallel processing
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            // Use a shared priority queue for managing states to explore
            PriorityBlockingQueue<SearchState> queue = new PriorityBlockingQueue<>(1000, Comparator.comparingInt(SearchState::getPenalty));
            queue.add(root); // Start with the root state

            List<Future<?>> futures = new ArrayList<>();
            
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() -> {
                    while (!queue.isEmpty()) {
                        SearchState current;
                        
                        // Safely poll the next state to process
                        synchronized (queue) {
                            current = queue.poll();
                            if (current == null) continue;
                       
                            int estimatedPenalty = calculateHeuristic(current);
                            
                            //Drop the minFilled, will be handled with estimatedPen
                            int fValue = current.getPenalty() + estimatedPenalty - current.getMinGameFillPenalty() 
                                - current.getMinPracticeFillPenalty();

                            if (fValue >= minEval) continue;
                        }

                        // Check if it's a terminal state
                        if (current.getRemainingTask().isEmpty()) {
                            synchronized (this) {
                                if (current.getPenalty() < minEval) {
                                    minEval = current.getPenalty();
                                    lastState = current;
                                }
                            }
                            continue;
                        }

                        // Generate next states and add them to the queue
                        Task nextTask = current.getRemainingTask().get(0);
                        List<SearchState> nextStates = generateNextStates(current, nextTask);

                        synchronized (queue) {
                            queue.addAll(nextStates);
                        }
                    }
                }));
            }

            // Wait for all threads to complete
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown(); // Shut down the thread pool
        }
    }


    // private void dfs(SearchState current) {
    //     //System.out.println("------------Current State with number of remaining tasks: " + current.getRemainingTask().size() + "-------------------");
    //     //current.printState();
    //     //System.out.println("--------------------------------------------");
    //     //System.out.println(current.getRemainingTask().size());

    //     if (current.getRemainingTask().isEmpty()) {
    //         //System.out.println("REACHED LEAF NODE.");
    //             if (current.getPenalty() < minEval) {
    //                 //System.out.println("New best state with penalty: " + current.getPenalty());
    //                 minEval = current.getPenalty();
    //                 lastState = current;
    //             }
    //         return;
    //     }
    
    //     // Prune states with penalty worse than the best solution
    //     //if (current.getPenalty() > minEval) {
    //     //    return;
    //     //}

    //     Task nextTask = current.getRemainingTask().get(0);

    //     List<SearchState> nextStates = generateNextStates(current, nextTask);
    
    //     for (SearchState nextState : nextStates) {
            
    //         dfs(nextState); // Recursive DFS call
    //     }
    // }

}
