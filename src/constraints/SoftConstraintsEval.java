package constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Assignment;
import model.SearchState;
import model.slots.Slot;
import model.task.Task;

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

        int secDiffPenaltyDiff = evalSecDiff(slot, task) * multiplierList.get(3);

        if (minFillPenaltyDiff != 0){
            if (task.getIsGame())state.setMinGameFillPenalty(state.getMinGameFillPenalty() - minFillPenaltyDiff);
            else state.setMinPracticeFillPenalty(state.getMinPracticeFillPenalty() - minFillPenaltyDiff);
        }

        if (prefPenaltyDiff != 0)state.setPrefPenalty(state.getPrefPenalty() - prefPenaltyDiff);
        if (pairPenaltyDiff != 0)state.setPairPenalty(state.getPairPenalty() - pairPenaltyDiff);
        if (secDiffPenaltyDiff != 0)state.setSecDiffPenalty(state.getSecDiffPenalty() + secDiffPenaltyDiff);
        
        state.updatePenalty();

        return state.getPenalty();
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
    
    private int evalSecDiff(Slot slot, Task task) {
        if (!task.getIsGame() || task.getDivision().equals(""))return 0;

        int n = slot.getTierTaskCount(task.getLevel());
        
        int pairs = (n * (n-1)) / 2;
              
        int penalty = pairs * penaltyList.get(3);
        return penalty;
    }
}