import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import model.Assignment;
import model.SearchState;
import model.slots.Slot;
import model.task.Task;
import search.AndTree;

public class Main {

    // for test commit
    public static void main(String[] args) throws FileNotFoundException {
        ArrayList<Integer> weightList = new ArrayList<>();
        ArrayList<Integer> multiplierList = new ArrayList<>();

        // PrintStream out = new PrintStream(new FileOutputStream("out_trace.txt"));
        // System.setOut(out);

        //Parse from command line here
        String inputFileName = args[0];
        for (int i = 1; i < 5; i++){
            multiplierList.add(Integer.parseInt(args[i]));
            weightList.add(Integer.parseInt(args[i+4]));
        }

        //Initial state
        List<Assignment> assignments = new ArrayList<>();
        List<Task> remainingTasks = new ArrayList<>();
        List<Slot> availableSlots = new ArrayList<>();
        AndTree andTree = new AndTree(new SearchState(assignments, remainingTasks, 
            availableSlots, 0), inputFileName, multiplierList, weightList);
        
        //This will parse inputs from file + preprocess data

        long startTime = System.currentTimeMillis();

        // long maxTimeMillis = 900000;

        andTree.preprocess();
        andTree.search();

        long endTime = System.currentTimeMillis();
        double totalTimeSeconds = (endTime - startTime) / 1000.0;
        
        System.out.println("\nTotal execution time: " + totalTimeSeconds + " seconds");
    }
}
