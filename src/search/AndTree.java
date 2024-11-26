package search;

import model.Assignment;
import model.SearchState;
import model.constraints.Constraint;
import model.constraints.PartialAssignment;
import model.slots.Slot;
import model.task.Task;
import parser.InputParser;
import constraints.HardConstraintsEval;
import constraints.SoftConstraintsEval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

 //TODO: Logic for penalty/multiplier + preprocess of: Partial Assignment, Not Compatible 
//Preprocess for all the constraints list
//For example, if it invovles with 2 parties, 
//make HashMap to list of their Paired, Unwanted, Preferences, NotCompatible, and Partial Assignments
//These can be used to loop through and easier management of information

public class AndTree {
    private SearchState root;
    private HashMap<String, List<Constraint>> constraints = new HashMap<>(); 
    private InputParser parser = new InputParser();
    private String inputFileName;
    private List<SearchState> allStates = new ArrayList<>();
    private List<PartialAssignment> allPatials = new ArrayList<>();
    private List<Task> allTasks = new ArrayList<>();
    private List<Slot> allSlots = new ArrayList<>();

    public AndTree(SearchState root, String filename, ArrayList<Integer> weightList, ArrayList<Integer> multiplierList) {
        this.root = root;
        parser.setMultiplierList(multiplierList);
        parser.setWeightList(weightList);
        this.inputFileName = filename;
    }


    private void parse(){
        try {
            parser.parseFile(this.inputFileName);
            root.setAvailableGamesSlot(parser.parseGameSlots());
            root.setAvailablePracticesSlot(parser.parsePracticeSlots());;
            root.setGames(parser.parseGames());
            root.setPractices(parser.parsePractices());

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
            
            this.constraints.put("NotCompatible", parser.parseNotCompatible());
            this.constraints.put("Pairs", parser.parsePairs());
            this.constraints.put("Preferences", parser.parsePreferences());
            this.constraints.put("Unwanted", parser.parseUnwanted());
            this.allPatials = parser.parsePartialAssignments();

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
        this.allTasks = parser.getAllTasks(); //joint (will be fixed later)
        this.allSlots = parser.getAllSlots(); //joint
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
                String currentTaskId = allTasks.get(i).getIdentifier();
                //Assign it there and then to the required slot
                if (currentTaskId.equals(taskIdenfitier)){
                    for (int k = 0; k < allSlots.size(); k++){
                        String currentTime = allSlots.get(k).getStartTime();
                        String currentDay = allSlots.get(k).getDay();
                        if (currentDay.equals(day) && currentTime.equals(time)){
                            //Assign, then break
                            
                            break;
                        }
                    }

                }
            }

        }
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
