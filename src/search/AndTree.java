package search;

import model.Assignment;
import model.SearchState;
import model.constraints.PartialAssignment;
import model.slots.Slot;
import model.task.Task;
import parser.InputParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

    HashMap<Integer, List<SearchState>> seenStateMap = new HashMap<>();

    private long startTime;


    public AndTree(SearchState root, String filename, ArrayList<Integer> multiplierList, ArrayList<Integer> weightList) {
        this.state = root;
        this.inputFileName = filename;
        this.weightList = weightList;
        this.multiplierList = multiplierList;

        // output file init
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OutputHandler.writeToFile(lastState, inputFileName);
        }));
    }

    // parses the input file. 
    private void parseInput(){
        try {
            this.startTime = System.currentTimeMillis(); 
            
            parser.parseFile(this.inputFileName);

            // parse the slots
            parser.parseGameSlots();
            parser.parsePracticeSlots();

            // parse the tasks
            parser.parseGames();
            parser.parsePractices();

            allTasks = parser.getAllTasks();
            allSlots = parser.getAllSlots();
            if (!parser.specialTasks.isEmpty()){
                specialList = parser.specialTasks;
                isSpecialBooking = true;
            }


            //System.out.println("IS SPECIAL BOOKING: " + isSpecialBooking);

            // set initial empty search state
            state.setRemainingSlots(allSlots);
            state.setRemainingTask(allTasks);

            // add the constraints
            parser.parseNotCompatible();
            parser.parsePairs();
            parser.parsePreferences();
            parser.parseUnwanted();

            // get the list of partial assignments
            partialAssignments  = parser.parsePartialAssignments();

            // sort the tasks and slots. this will make the search reach an optimal value faster in the dfs
            parser.sortTasks();
            parser.sortSlots();

            // set the current penalty values of the state
            state.setMinGameFillPenalty(parser.maxMinGame * weightList.get(0) * multiplierList.get(0));
            state.setMinPracticeFillPenalty(parser.maxMinPractice * weightList.get(1) * multiplierList.get(0));
            state.setPairPenalty(parser.maxPairs * weightList.get(2) * multiplierList.get(2));
            state.setPrefPenalty(parser.maxPreferencesValue * multiplierList.get(1));

            state.updatePenalty();
            //System.out.println("MAX PENALTY = " + state.getPenalty());
            System.out.println("MAX SECDIFF INITIAL= " + state.getPairPenalty());

            // initialize the constraint checkers
            softChecker = new SoftConstraintsEval(multiplierList, weightList);
            hardChecker = new HardConstraintsEval();


        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }

    // used to preprocess the search state before actually going into dfs
    public void preprocess() {
        parseInput();

        // check for special practice bookings
        if (!crossCheckIsSpecialBooking()){
            System.out.println("MISERABLY FAILED WITH SPECIAL BOOKING");
            System.exit(1);
        }

        // assign the partial assignments
        if (!assignPartialAssignments()){
            System.out.println("FAILED WITH PARTIAL");
            System.exit(1);
        }
    }

    // used to check and assign special practice bookings
    private boolean crossCheckIsSpecialBooking(){
        if (!isSpecialBooking) {
            return true;
        }

        Slot s = findSlotByDayAndTime("TU", "18:00", true);
        if (s == null) {
            System.err.println("FAILED WITH SPPS");
        }


        Slot special18Slot = findSlotByDayAndTime("TU", "18:00", false);
        if (special18Slot == null){
            //System.out.println("line 120");
            return false;
        }
        //TODO:
        //So it might print out partial fails if there is not enough 18:00 slot ahaha
        // this is fixed Hong^

        // adds the special bookings as partial assignments
        if (specialList.contains("CMSA U13T1S")){
            partialAssignments.add(new PartialAssignment("CMSA U13T1S", "TU", "18:00"));
        }
        if (specialList.contains("CMSA U12T1S")){
            partialAssignments.add(new PartialAssignment("CMSA U12T1S", "TU", "18:00"));
        }

        return true;
    }

    // helper functions to find task and slot by their string identifiers
    private Task findTaskByIdentifier(String identifier) {
        return allTasks.stream().filter(task -> task.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    private Slot findSlotByDayAndTime(String day, String time, boolean isGame) {
        return allSlots.stream()
                .filter(slot -> slot.getDay().equals(day) && slot.getStartTime().equals(time) && slot.forGame() == isGame)
                .findFirst()
                .orElse(null);
    }

    // assigns the partial assignemnts.
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

    // adds current state to the list of previous states. used for pruning the tree
    private void addToPrevStates(SearchState aState) {
        int key = aState.getAssignments().size();
    
        if (!seenStateMap.containsKey(key)) {
            List<SearchState> newStateList = new ArrayList<>();
            newStateList.add(aState);
            seenStateMap.put(key, newStateList);
        } else {
            seenStateMap.get(key).add(aState);
        }
    }

    // checks to see if the input state has been seen before. used for pruning
    private boolean checkAlreadySeenState(SearchState aState) {
        int len = aState.getAssignments().size();
        // System.out.println("i am here");

        if (!seenStateMap.containsKey(len)) {
            // System.out.println("good. didnt prune");
            return false;
        }

        for (SearchState seen : seenStateMap.get(len)) {
            if (aState.compareSearchState(seen)) {
                // System.out.println("PRUNE FOUND HERE =======================");
                // System.out.println(aState.getAssignments());
                // System.out.println(seen.getAssignments());
                // System.out.println("++++++++++++++++++++++++");
                // System.out.println(seen.getAssignments());
                return true;
            }
        }
        return false;
    }

    // assigns tasks to slots and updates the current state to a new state that contains the task in that slot.
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
        
        newState.setPenalty(softChecker.updatePenalty(newAssignment, newState));
        // newState.printState();
        return newState;
    }

    // generates new slots based on a task, and the current states available slots
    private List<SearchState> generateNextStates(SearchState state, Task task) {
        List<SearchState> states = new ArrayList<>();
        
        List<Slot> availableSlots = new ArrayList<>(state.getAvailableSlots());            
        
        for (Slot slot : availableSlots) {      
            if (slot.forGame() != task.getIsGame())continue;     
            SearchState newState = transitLinkedAssignment(state, task, slot);
            if (!newState.equals(state) && !checkAlreadySeenState(newState)) {  // ^^ comapre here exactly
                    states.add(newState);
                    addToPrevStates(newState);
                } 
            }
        return states;
    }

    // initiates the dfs search
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
    
    // main dfs search. terminates when all branches are explored (after pruning the tree), or when eval value of 0 is reached, or terminates after 15 minutes.
    private void dfs(SearchState current) {
        // System.out.println("------------Current State with number of remaining tasks: " + current.getRemainingTask().size() + "-------------------");
        // current.printState();
        // System.out.println("--------------------------------------------");
        // System.out.println(current.getRemainingTask().size() + " " + minEval);

        if (current.getRemainingTask().isEmpty()) {
            // System.out.println("REACHED LEAF NODE. best solution at penalty: " + minEval);
                if (current.getPenalty() < minEval) {
                    System.out.println("New best state with penalty: " + current.getPenalty());
                    minEval = current.getPenalty();
                    lastState = current;
                    lastState.printState();
                }   
            return;
        }

        // && may cause errors here
        if (minEval == 0) {
            System.out.println("MINEVAL of 0 REACHED");
            lastState.printState();
            System.exit(1);
        }

        if (System.currentTimeMillis() - startTime >= 15 * 60 * 1000) {
            // System.out.println("15 minutes have passed. Exiting program...");
            lastState.printState();
            System.exit(1); 
        }
    
        Task nextTask = current.getRemainingTask().get(0);

        List<SearchState> nextStates = generateNextStates(current, nextTask);
    
        for (SearchState nextState : nextStates) {
            
            dfs(nextState); // Recursive DFS call
        }
    }

}
