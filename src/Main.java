import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.SearchState;
import model.slots.GameSlot;
import model.slots.PracticeSlot;
import model.task.Game;
import model.task.Practice;
import search.AndTree;

public class Main {
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
        Map<String, String> assignments = new HashMap<>();
        List<Game> remainingGames = new ArrayList<>();
        List<Practice> remainingPractices = new ArrayList<>();
        List<GameSlot> availableGamesSlots = new ArrayList<>();
        List<PracticeSlot> availablePracticesSlots = new ArrayList<>();
        AndTree andTree = new AndTree(new SearchState(assignments, remainingGames, remainingPractices, 
            availableGamesSlots, availablePracticesSlots, 0), inputFileName, weightList, multiplierList);
        
        //This will parse inputs from file + preprocess data

        andTree.preprocess();
    }
}
