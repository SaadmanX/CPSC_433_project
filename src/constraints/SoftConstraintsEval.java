package constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Assignment;
import model.SearchState;
import model.slots.Slot;
import model.task.Task;


/**
 * This class only calculates for the partial of each new assignment, total logic handled in states
 */
public class SoftConstraintsEval {

    // Multiplier List (Order: minFill, pref, pair, secdiff)
    private List<Integer> multiplierList;

    // Penalty List (Order: gameMin, practiceMin, notPaired, section)
    private List<Integer> penaltyList; 

    public SoftConstraintsEval(List<Integer> multiplierList, List<Integer> penaltyList) {
        this.multiplierList = multiplierList;
        this.penaltyList = penaltyList;
    }
    
    public int updatePenalty(Assignment assignment, SearchState state) {

        Slot slot = assignment.getSlot();
        Task task = assignment.getTask();
    
        // ^^ check to see if any weight * penalty == 0. in which case we dont bother doing them
        int minFillPenaltyDiff = updateMinFilled(slot) * multiplierList.get(0);
        int prefPenaltyDiff = evalPreferencePenalty(task, slot) * multiplierList.get(1);
        int pairPenaltyDiff = evalPairingPenalty(task, slot) * multiplierList.get(2);

        int secDiffPenaltyDiff = evalSecDiff(slot) * multiplierList.get(3);

        if (minFillPenaltyDiff != 0){
            if (task.getIsGame())state.setMinGameFillPenalty(state.getMinGameFillPenalty() - minFillPenaltyDiff);
            else state.setMinPracticeFillPenalty(state.getMinPracticeFillPenalty() - minFillPenaltyDiff);
        }

        if (prefPenaltyDiff != 0)state.setPrefPenalty(state.getPrefPenalty() - prefPenaltyDiff);
        if (pairPenaltyDiff != 0)state.setPairPenalty(state.getPairPenalty() - pairPenaltyDiff);
        if (secDiffPenaltyDiff != 0)state.setSecDiffPenalty(state.getSecDiffPenalty() - secDiffPenaltyDiff);
        
        state.updatePenalty();

        return state.getPenalty();
    }
    
    private boolean isOverlap(Slot slot1, Slot slot2) {
        if (slot1 == null || slot2 == null)return false;  
        if (!slot1.getDay().equals(slot2.getDay())) {
            return false; 
        }

        if (slot1.getId().equals(slot2.getId()) && slot1.forGame() == slot2.forGame()){
            return true;
        }

        double start1 = slot1.getSlotStartTime();
        double start2 = slot2.getSlotStartTime();
    
        double duration1 = getSlotDuration(slot1);
        double duration2 = getSlotDuration(slot2);
    
        double end1 = start1 + duration1;
        double end2 = start2 + duration2;
    
        // Slots overlap if:
        // Start1 is before end2 and Start2 is before end1
        return (start1 < end2 && start2 < end1);
    }
    
    // Helper function to determine the duration of a slot
    private double getSlotDuration(Slot slot) {
        if (slot.forGame()) {
            if (slot.getDay().equals("TU")) return 1.5; 
            return 1.0; 
        } else {
            if (slot.getDay().equals("TU")) return 1.5; 
            if (slot.getDay().equals("FR")) return 2.0; 
            return 1.0; 
        }
    }
    
    
    private int updateMinFilled(Slot slot) {
        //Below scenario means it just met the minCondition or is just added another task in
        //Compared to previous time the slot been updated, the diff is 1
        //So change to remove the final difference penalty
        if (slot.getCurrentCount() <= slot.getMin()){ //The difference should be 1, so only the weight is returned for multiplication
            return slot.forGame() ? penaltyList.get(0) : penaltyList.get(1);
        }
    
        return 0;
    }
    
    private int evalPreferencePenalty(Task task, Slot slot) {
        if (task.isPreferredSlot(slot.getId(), slot.forGame())) 
            return task.getPreferenceValue(slot);
        
        return 0; //no change ofc, if not actual

    }
    
    private int evalPairingPenalty(Task task, Slot slot) {
        if (!task.getPairs().isEmpty()) {
            int counter = 0;
            for (Task t : task.getPairs()) {
                if (slot.getAssignedTasks().stream().anyMatch(c -> c.getIdentifier().equals(t.getIdentifier())))counter++;
            }
    
            return counter * penaltyList.get(2);
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
    
        int penalty = numberOfPair * penaltyList.get(3);
        return penalty;
    }
}
