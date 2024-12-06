package constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;
import model.constraints.Pair;
import model.constraints.Preference;

public class SoftConstraintsEval {


    // Multiplier List (Order: minFill, pref, pair, secdiff)
    private List<Integer> multiplierList;
    
    // Penalty List (Order: gameMin, practiceMin, notPaired, section)
    private List<Integer> penaltyList;


    public int initialPenalty; //can be higher/lower after assignment, acts as a reference point of initial state

    public SoftConstraintsEval(List<Integer> multiplierList, List<Integer> penaltyList, List<Preference> prefer, List<Pair> pair, List<Slot> allSlots) {
        this.multiplierList = multiplierList;
        this.penaltyList = penaltyList;
    }

    public int calculatePenalty(List<Assignment> assignments) {
        int penalty = 0;
    
        for (Assignment assignment : assignments) {
            int assignmentPenalty = calculatePenalty(assignment);
            penalty += assignmentPenalty;
        }
    
        return penalty;
    }
    
    public int calculatePenalty(Assignment assignment) {
        
        int penalty = 0;
        Slot slot = assignment.getSlot();
        Task task = assignment.getTask();
    
        int minFillPenalty = evalMinFilled(slot);
        int prefPenalty = evalPreferencePenalty(task, slot);
        int pairPenalty = evalPairingPenalty(task);
        int secDiffPenalty = evalSecDiff(slot);
    
    
        penalty = minFillPenalty + prefPenalty + pairPenalty + secDiffPenalty;
        
        return penalty;
    }
    
    private boolean isOverlap(Task a, Task b) {
        boolean overlap = a.getCurrentAssigned().getStartTime().equals(b.getCurrentAssigned().getStartTime());
        return overlap;
    }
    
    private int evalMinFilled(Slot slot) {
    
        if (slot.getCurrentCount() < slot.getMin()) {
            int multiplier = multiplierList.get(0);
            int penalty = slot.forGame() ? penaltyList.get(0) : penaltyList.get(1);
            int difference = slot.getMin() - slot.getCurrentCount();
            int totalPenalty = difference * multiplier * penalty;
            
            return totalPenalty;
        }
    
        return 0;
    }
    
    private int evalPreferencePenalty(Task task, Slot slot) {
    
        if (!task.isPreferredSlot(slot)) {
            int penalty = (task.getSumPreferences() - task.getPreferenceValue(slot)) * multiplierList.get(1);
            return penalty;
        }
    
        return 0;
    }
    
    private int evalPairingPenalty(Task task) {
        if (task.getPairs() != null) {
            int counter = 0;
            int total = task.getPairs().size();
            
            for (Task t : task.getPairs()) {
                if (isOverlap(task, t)) {
                    counter++;
                }
            }
    
            int penalty = (total - counter) * multiplierList.get(2) * penaltyList.get(2);
            return penalty;
        }
    
        return 0;
    }
    
    private int evalSecDiff(Slot slot) {
        
        HashMap<String, Integer> ageFrequencyMap = new HashMap<>();
    
        for (Task t : slot.getAssignedTasks()) {
            if (t.getIsGame() && !t.getDivision().equals("")) {
                String level = t.getLevel();
                ageFrequencyMap.put(level, ageFrequencyMap.getOrDefault(level, 0) + 1);
            }
        }
    
        int numberOfPair = 0;
        for (Map.Entry<String, Integer> entry : ageFrequencyMap.entrySet()) {
            Integer n = entry.getValue();
            if (n > 1) {
                int pairs = (n * (n-1)) / 2;
                numberOfPair += pairs;
            }
        }
    
        int penalty = numberOfPair * multiplierList.get(3) * penaltyList.get(3);
        return penalty;
    }
}
