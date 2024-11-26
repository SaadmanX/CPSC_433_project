package search;

import model.Assignment;
import model.SearchState;
import model.constraints.Constraint;
import model.constraints.PartialAssignment;
import model.slots.GameSlot;
import model.slots.PracticeSlot;
import model.slots.Slot;
import model.task.Task;
import parser.InputParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import constraints.HardConstraintsEval;
import constraints.SoftConstraintsEval;

 //TODO: Logic for penalty/multiplier + Handle NotCompatible
//Preprocess for all the constraints list
//For example, if it invovles with 2 parties, 
//make HashMap to list of their Paired, Unwanted, Preferences, NotCompatible, and Partial Assignments
//These can be used to loop through and easier management of information

public class AndTree {
    private SearchState state;
    private HashMap<String, List<Constraint>> constraints = new HashMap<>(); 
    private InputParser parser = new InputParser();
    private String inputFileName;
    private List<SearchState> allStates = new ArrayList<>();
    private List<PartialAssignment> allPatials = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();
    private List<Slot> allSlots = new ArrayList<>();
    HardConstraintsEval hardChecker = new HardConstraintsEval();
    SoftConstraintsEval softChecker = new SoftConstraintsEval();

    public AndTree(SearchState root, String filename, ArrayList<Integer> weightList, ArrayList<Integer> multiplierList) {
        this.state = root;
        parser.setMultiplierList(multiplierList);
        parser.setWeightList(weightList);
        this.inputFileName = filename;
    }


    private void parse(){
        try {
            parser.parseFile(this.inputFileName);
            state.setAvailableGamesSlot(parser.parseGameSlots());
            state.setAvailablePracticesSlot(parser.parsePracticeSlots());;
            parser.parseGames();
            parser.parsePractices();
            
            this.constraints.put("NotCompatible", parser.parseNotCompatible());
            this.constraints.put("Pairs", parser.parsePairs());
            this.constraints.put("Preferences", parser.parsePreferences());
            this.constraints.put("Unwanted", parser.parseUnwanted());
            this.allPatials = parser.parsePartialAssignments();

            this.allTasks = parser.getAllTasks();
            this.allSlots = parser.getAllSlots();

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
    public void preprocess(){
        parse();
        assignPartialAssignment();
        //assignNotCompatible();
    }

    private void assignPartialAssignment(){
        //Assign games to their schedules, then check for HardConstraints Eval
        for (int i = 0; i < allPatials.size(); i++) {
            String taskIdenfitier = allPatials.get(i).getTaskIdentifier();
            String day = allPatials.get(i).getDay();
            String time = allPatials.get(i).getTime();

            for (int j = 0; j < allTasks.size(); j++){
                String currentTaskId = allTasks.get(j).getIdentifier();
                //Assign it there and then to the required slot
                if (currentTaskId.equals(taskIdenfitier)){
                    for (int k = 0; k < allSlots.size(); k++){
                        String currentTime = allSlots.get(k).getStartTime();
                        String currentDay = allSlots.get(k).getDay();
                        if (currentDay.equals(day) && currentTime.equals(time)){
                            //Transit, break
                            Assignment nAssignment = new Assignment(allTasks.get(j), allSlots.get(k));
                            transitNext(nAssignment);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void handleNotCompatible(){
        /**
         * 
         * for (Task task : allTasks) {
            if (task.getIdentifier().equals(team1Id)){
                task.addNotCompatible(team2Id);
            } else if (task.getIdentifier().equals(team2Id)){
                task.addNotCompatible(team1Id);
                }
            }
        */
    }

    //Transits to next assignment
    public void transitNext(Assignment newAssignment){
        List<Assignment> assignments = state.getAssignments();
        assignments.add(newAssignment); //test new assignment
        if (!hardChecker.validate(assignments)){
            return;
        }
        int penalty = softChecker.calculatePenalty(assignments);
        
        //Set new state
        this.state.setAssignments(assignments);
        this.state.setPenalty(penalty);

        boolean isGame = false;

        //Remove current task, and update state
        for (Iterator<Task> iterator = allTasks.iterator(); iterator.hasNext(); ) {
            Task task = iterator.next();
            if (task.getIdentifier().equals(newAssignment.getTask().getIdentifier())) {
                isGame = newAssignment.getTask().getIsGame();
                iterator.remove(); 
                break;  
            }
        }

        //Reset max capacity (Wrong, needs to be in the remaining slots)
        if (isGame){
            state.setRemainingGamesSlots(newAssignment.getSlot());

        } else {
            state.setRemainingPracticesSlots(newAssignment.getSlot());
        }
       
        System.out.println("Successfully transits to a new state where: ");
        System.out.println("Assignment: ");
        for (int a = 0; a < assignments.size(); ++a){
            System.out.println(assignments.get(a));
        }
        System.out.println("Remaining Tasks: ");
        for (int b = 0; b < state.getRemaininngTask().size(); b++){
            System.out.println(state.getRemaininngTask().get(b).toString());
        }

        System.out.println("Remaining Games Slots:");

        for (int c = 0; c < state.getAvailablePracticesSlots().size(); c++){
            System.out.println(state.getAvailableGamesSlots().get(c));
        }

        System.out.println("Remaining Practices Slots:");
        for (int d = 0; d < state.getAvailablePracticesSlots().size(); d++){
            System.out.println(state.getAvailablePracticesSlots().get(d));
        }

        System.out.println("Penalty: " + penalty);
    }

    public void chooseNext(){
        //With remaining Task, propagates possibilities and choose the appropriate one
    }

    //TODO: Search algorithm
    public SearchState search() {
        /** 
        PriorityQueue<SearchState> queue = new PriorityQueue<>((a, b) -> a.getPenalty() - b.getPenalty());
        queue.add(root);

        while (!queue.isEmpty()) {
            SearchState current = queue.poll();

            if (HardConstraintsEval.validate(current)) {
                current.setPenalty(SoftConstraintsEval.calculatePenalty(current));

                if (current.getRemainingGames().isEmpty() && current.getRemainingPractices().isEmpty()) {
                    return current; // Solution found
                }

                // Generate and enqueue child states
            }
        }
        */
        return null;
        
    }
}
