package constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;
import model.constraints.Pair;
import model.constraints.Preference;

//TODO: (prefList another multiplier in the subfunction), and all these values must be calculated separately

public class SoftConstraintsEval {
    // Multiplier List (Order: minFill, pref, pair, secdiff)
    private List<Integer> multiplierList;
    
    // Penalty List (Order: gameMin, practiceMin, notPaired, section)
    //private List<Integer> penaltyList;

   //private List<Preference> preferenceList;

   // private List<Slot> allSlots;

    public int initialPenalty; //can be higher/lower after assignment, acts as a reference point of initial state

    //private List<Pair> pairs;

    public SoftConstraintsEval(List<Integer> multiplierList, List<Integer> penaltyList, List<Preference> prefer, List<Pair> pair, List<Slot> allSlots) {
        this.multiplierList = multiplierList;
        //this.penaltyList = penaltyList;
        //this.preferenceList = prefer;
        //this.allSlots = allSlots;
        //this.pairs = pair;
    }

    //max 3 here for the link transit
    public int calculatePenalty(List<Assignment> assignments){
        int penalty = 0;

        for (Assignment assignment : assignments) {
            penalty += calculatePenalty(assignment);
        }

        return penalty;
    }

    public int calculatePenalty(Assignment assignment){
        int penalty = 0;

        Slot slot = assignment.getSlot();
        Task task = assignment.getTask();

        // Min Fill Penalty
        penalty += evalMinFilled(slot);

        // Pref Penalty update
        penalty += evalPreferencePenalty(task, slot);

        // Pairing
        penalty += evalPairingPenalty(task);
                
        // Section Difference Penalty
        penalty += evalSecDiff(task, slot);
                
        return penalty;
                    
    }    

    private boolean isOverlap(Task a, Task b){
        return (a.getCurrentAssigned().getStartTime().equals(b.getCurrentAssigned().getStartTime()));
    }

    private int evalMinFilled(Slot slot){
        if (slot.getCurrentCount() < slot.getMin()){
            return (slot.getMin() - slot.getCurrentCount()) * multiplierList.get(0);
        }

        return 0;
    }

    private int evalPreferencePenalty(Task task, Slot slot){
        if (task.isPreferredSlot(slot)){
            
            return (task.getSumPreferences() - task.getPreferenceValue(slot)) * multiplierList.get(1);
        }
        return 0;
    }

    private int evalPairingPenalty(Task task){
        int counter = 0;
        if (task.getPairs() != null){
            int total = task.getPairs().size();
            for (Task t: task.getPairs()){
                //find if they are paired
                if (isOverlap(task, t)){
                    counter++;
                }
            }

            return (total - counter) * multiplierList.get(2);
        }

        return 0;
    }

    private int evalSecDiff(Task task, Slot slot){
        HashMap<String, Integer> ageFrequencyMap = new HashMap<>();

        for (Task t : slot.getAssignedTasks()) {
            if (t.getIsGame() && !t.getDivision().equals("")){
                String tier = t.getTier();
                ageFrequencyMap.put(tier, ageFrequencyMap.getOrDefault(tier, 0) + 1);
            }
        }

        int numberOfPair = 0;
        for (Map.Entry<String, Integer> entry : ageFrequencyMap.entrySet()) {
            Integer n = entry.getValue(); //frequency
        
            if (n > 1){
                numberOfPair += n * (n-1) / 2;
            }
        }
        return numberOfPair * multiplierList.get(3);
    }
}
