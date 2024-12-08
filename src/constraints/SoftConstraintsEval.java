package constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;

public class SoftConstraintsEval {


    // Multiplier List (Order: minFill, pref, pair, secdiff)
    private List<Integer> multiplierList;
    
    // Penalty List (Order: gameMin, practiceMin, notPaired, section)
    private List<Integer> penaltyList;


    public int initialPenalty; //can be higher/lower after assignment, acts as a reference point of initial state

    public SoftConstraintsEval(List<Integer> multiplierList, List<Integer> penaltyList, List<Slot> allSlots) {
        this.multiplierList = multiplierList;
        this.penaltyList = penaltyList;
        initialPenalty = 0;
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
        System.out.println("------------- calculating pen values -------------");
    
        // ^^ check to see if any weight * penalty == 0. in which case we dont bother doing them
        int minFillPenalty = evalMinFilled(slot);
        System.out.println("minFill penalty : " + minFillPenalty);
        int prefPenalty = evalPreferencePenalty(task, slot);
        System.out.println("preference penalty : " + prefPenalty);
        int pairPenalty = evalPairingPenalty(task);
        System.out.println("pairing penalty : " + pairPenalty);
        int secDiffPenalty = evalSecDiff(slot);
        System.out.println("SecDiff penalty : " + secDiffPenalty);
    
    
        penalty = minFillPenalty + prefPenalty + pairPenalty + secDiffPenalty;
        System.out.println("Total penalty: " + penalty); 
        System.out.println("-------------  -------------");
        return penalty;
    }
    
    private boolean isOverlap(Task a, Task b) {
        //So this only checks b because a is passed from currently assigned task,
        //only b can cause null ptr
        if (b.getCurrentAssigned() == null)return false;
        boolean overlap = a.getCurrentAssigned().getStartTime().equals(b.getCurrentAssigned().getStartTime()) && a.getCurrentAssigned().getDay().equals(b.getCurrentAssigned().getDay());
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
        if (!task.isPreferredSlot(slot.getId(), slot.forGame())) {
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
