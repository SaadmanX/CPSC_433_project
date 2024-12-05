package constraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;
import model.constraints.Pair;
import model.constraints.Preference;

//TODO: Something wrong, beacause penaltyList is unused, have to check this again 
//TODO: (it's another multiplier in the subfunction), and all these values must be calculated separately
//TODO: WIll need to separate it into subfunctions for this reason + readibility
public class SoftConstraintsEval {
    // Multiplier List (Order: minFill, pref, pair, secdiff)
    private List<Integer> multiplierList;
    
    // Penalty List (Order: gameMin, practiceMin, notPaired, section)
    private List<Integer> penaltyList;

    private List<Preference> preferenceList;

    private List<Slot> allSlots;

    public int initialPenalty; //can be higher/lower after assignment, acts as a reference point of initial state

    private List<Pair> pairs;

    public SoftConstraintsEval(List<Integer> multiplierList, List<Integer> penaltyList, List<Preference> prefer, List<Pair> pair, List<Slot> allSlots) {
        this.multiplierList = multiplierList;
        this.penaltyList = penaltyList;
        this.preferenceList = prefer;
        this.allSlots = allSlots;
        this.pairs = pair;

        initialPenalty = calculateInitialPenalty();
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
        if (slot.getCurrentCount() < slot.getMin()){
            penalty += (slot.getMin() - slot.getCurrentCount()) * multiplierList.get(0);
        }

        // Pref Penalty update
        if (task.isPreferredSlot(slot))penalty -=  task.getPreferenceValue(slot) * multiplierList.get(1);

        // Pairing
        // Basically this needs to be iterated... can't think of another way, but this is so slow
        int counter = 0;
        if (task.getPairs() != null){
            int total = task.getPairs().size();
            for (Task t: task.getPairs()){
                //find if they are paired
                if (isOverlap(task, t)){
                    counter++;
                }
            }

            penalty += (total - counter) * multiplierList.get(2);
        }
                
        // Section Difference Penalty

        //Different divisional games within a single age/tier group should be scheduled 
        //at different times. For each pair of divisions that is scheduled into the same slot, 
        //we add a penalty pensection to the Eval-value of an assignment.

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
        penalty += numberOfPair * multiplierList.get(3);
                
        return penalty;
                    
    }    

    private boolean isOverlap(Task a, Task b){
        return (a.getCurrentAssigned().getStartTime().equals(b.getCurrentAssigned().getStartTime()));
    }


    //Should only be called exactly once at the initial state, after only the newest assignment is evaluated
    public int calculateInitialPenalty() {
        int penalty = 0;

        penalty += minFillPenalty() * multiplierList.get(0); 
        //System.out.println("penalty after min fill check: " + Integer.toString(penalty));

        penalty += preferencesPenalty() * multiplierList.get(1); 
        //penalty += preferencesPenalty() * multiplierList.get(1); 
        //System.out.println("penalty after preference check: " + Integer.toString(penalty));

        penalty += pairingPenalty() * multiplierList.get(2); 
        //System.out.println("penalty after pair check: " + Integer.toString(penalty));

        //No initial assignment, no penalty for sec diff

        return penalty;
    }


    private int minFillPenalty() {
        int penalty = 0;
        for (Slot slot : allSlots) {
            penalty += slot.getMin();
        }

        return penalty;
    }

    private int preferencesPenalty() {
        int penalty = 0;

        for (Preference pref : preferenceList) {
            penalty += pref.getPenalty();
        }

        return penalty;
    }

    private int pairingPenalty() {
       return pairs.size();
    }

}