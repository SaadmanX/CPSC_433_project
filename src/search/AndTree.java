package search;

import model.Assignment;
import model.SearchState;
import model.constraints.PartialAssignment;
import model.slots.Slot;
import model.task.Task;
import parser.InputParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
            System.out.println("MAX SECDIFF INITIAL= " + state.getPairPenalty());

            softChecker = new SoftConstraintsEval(multiplierList, weightList);

            hardChecker = new HardConstraintsEval();


        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
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
        
        newState.setPenalty(softChecker.updatePenalty(newAssignment, newState));
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
        //System.out.println("------------Current State with number of remaining tasks: " + current.getRemainingTask().size() + "-------------------");
        //current.printState();
        //System.out.println("--------------------------------------------");
        //System.out.println(current.getRemainingTask().size());

        if (current.getRemainingTask().isEmpty()) {
            System.out.println("REACHED LEAF NODE.");
                if (current.getPenalty() < minEval) {
                    System.out.println("New best state with penalty: " + current.getPenalty());
                    minEval = current.getPenalty();
                    lastState = current;
                }
            return;
        }
    
        // Prune states with penalty worse than the best solution
        //if (current.getPenalty() > minEval) {
        //    return;
        //}

        Task nextTask = current.getRemainingTask().get(0);

        List<SearchState> nextStates = generateNextStates(current, nextTask);
    
        for (SearchState nextState : nextStates) {
            
            dfs(nextState); // Recursive DFS call
        }
    }

}
