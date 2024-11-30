import java.util.ArrayList;
import java.util.List;

import model.Assignment;
import model.SearchState;
import model.slots.Slot;
import model.task.Task;
import search.AndTree;

public class Main {

    // for test commit
    public static void main(String[] args) {
        ArrayList<Integer> weightList = new ArrayList<>();
        ArrayList<Integer> multiplierList = new ArrayList<>();

        //Parse from command line here
        String inputFileName = args[0];
        for (int i = 1; i < 5; i++){
            weightList.add(Integer.parseInt(args[i]));
            multiplierList.add(Integer.parseInt(args[i+4]));
        }

        //Initial state
        List<Assignment> assignments = new ArrayList<>();
        List<Task> remainingTasks = new ArrayList<>();
        List<Slot> availableSlots = new ArrayList<>();
        AndTree andTree = new AndTree(new SearchState(assignments, remainingTasks, 
            availableSlots, Integer.MAX_VALUE), inputFileName, weightList, multiplierList);
        
        //This will parse inputs from file + preprocess data

        andTree.preprocess();
        andTree.search();
    }
}
