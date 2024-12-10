package search;

import model.Assignment;
import model.SearchState;
import model.constraints.PartialAssignment;
import model.slots.Slot;
import model.task.Task;
import parser.InputParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

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
    
    //Key = size,  maps to all the states with that size of assignments
    HashMap<Integer, List<SearchState>> seenStateMap = new HashMap<>();
    
    private int calculateMinFilledHeuristic(SearchState state){
        int total = 0;

        int numberOfMinGamesLeftToFill = parser.maxMinGame;
        int numberOfMinPracticeLeftToFill = parser.maxMinPractice;
        
        for (Assignment a: state.getAssignments()){
            if (a.getSlot().getMin() > a.getSlot().getCurrentCount()){
                if (!a.getSlot().forGame()){
                    numberOfMinPracticeLeftToFill -= (a.getSlot().getMin() - a.getSlot().getCurrentCount());
                } else {
                    numberOfMinPracticeLeftToFill -= (a.getSlot().getMin() - a.getSlot().getCurrentCount());
                }
            }
        }

        for (Task task: state.getRemainingTask()){
            if (task.getIsGame())numberOfMinGamesLeftToFill --;
            else numberOfMinPracticeLeftToFill--;
        }

        //Parallelism here, so if all (even non optimal go for it, it will be prioritized the same)
        //Have to set a cap of 0 for all
        if (numberOfMinPracticeLeftToFill > 0)total+= weightList.get(1) * numberOfMinPracticeLeftToFill;
        else {
            //But if everything is 0... it will also ignore the state with the same heuristic all over...
            if (state.getMinPracticeFillPenalty() > 0)total -= state.getMinPracticeFillPenalty(); //Rebase to 0
        }
        if (numberOfMinGamesLeftToFill > 0)total += weightList.get(0) * numberOfMinGamesLeftToFill;
        else {
            if (state.getMinGameFillPenalty() > 0)total-= state.getMinGameFillPenalty();
        }

        return total;
    }
    

    private int calculateHeuristic(SearchState state) {
    
        int totalEstimatedPenalty = 0;
        totalEstimatedPenalty -= state.getMinGameFillPenalty() + state.getMinPracticeFillPenalty();

        //Other 2 will try these 2 other heuristics, which is used to rather reduce the penalty,
        //Because future assignments potentially reduce the penalty down
        //System.out.println("Total estimated min game filled: " + totalEstimatedPenalty);

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
        List<SearchState> states = new ArrayList<>();
        
        List<Slot> availableSlots = new ArrayList<>(state.getAvailableSlots());            
        
        for (Slot slot : availableSlots) {      
            if (slot.forGame() != task.getIsGame())continue;     
            SearchState newState = transitLinkedAssignment(state, task, slot);
            if (!newState.equals(state)) {
                states.add(newState);
            } 
        }

        // Sort states based on the heuristic value
        states.sort(Comparator.comparingInt(this::calculateHeuristic));

        return states;
    }

    private int calculateTotalCost(SearchState state) {
        int g = state.getPenalty(); 
        int h = calculateHeuristic(state); 
        return g + h; 
    }
    

    public void search() {
        PriorityQueue<SearchState> openSet = new PriorityQueue<>(Comparator.comparingInt(this::calculateTotalCost));
        
        openSet.add(state); // Add the root state to the open set

        while (!openSet.isEmpty()) {
            SearchState current = openSet.poll(); 
           
            if (current.getRemainingTask().isEmpty()) {
                if (current.getPenalty() < minEval) {
                    System.out.println("New best state found with penalty: " + current.getPenalty());
                    minEval = current.getPenalty();
                    lastState = current;
                }
                continue; 
            }
           
            if (seenStateMap.containsKey(current.getAssignments().size())) {
                List<SearchState> toCompareSearchStates = seenStateMap.get(current.getAssignments().size());
                boolean match = false;
                for (SearchState s: toCompareSearchStates){
                    if (s.compareSearchState(current)){
                        System.out.println("I THINK I'VE SEEN THIS FILM BEFORE");
                        match = true;
                        break;
                    }
                }

                if (match)continue; //skip seen state
                //Add it in otherwise
                toCompareSearchStates.add(current);
                seenStateMap.put(current.getAssignments().size(), toCompareSearchStates);

            } else {
                List<SearchState> newList = new ArrayList<>();
                newList.add(current);
                seenStateMap.put(current.getAssignments().size(), newList);
            }

            // Generate and process next states
            Task nextTask = current.getRemainingTask().get(0);
            List<SearchState> nextStates = generateNextStates(current, nextTask);

            for (SearchState nextState : nextStates) {
                // Add valid next states to the open set
                openSet.add(nextState);
            }
        }

        if (lastState != null) {
            System.out.println("Best solution found with penalty: " + minEval);
            lastState.printState();
        } else {
            System.out.println("No valid solution found.");
        }
    }
}
