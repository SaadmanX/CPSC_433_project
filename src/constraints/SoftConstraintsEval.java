package constraints;

import java.util.ArrayList;
import java.util.List;
import model.Assignment;
import model.slots.Slot;
import model.task.Task;

public class SoftConstraintsEval {
    
    // Multiplier List (Order: minFill, pref, pair, secdiff)
    private List<Integer> multiplierList;
    
    // Penalty List (Order: gameMin, practiceMin, notPaird, section)
    private List<Integer> penaltyList;

    public SoftConstraintsEval(List<Integer> multiplierList, List<Integer> penaltyList) {
        this.multiplierList = multiplierList;
        this.penaltyList = penaltyList;
    }

    public int calculatePenalty(List<Assignment> assignments) {
        int penalty = 0;

        // Min Fill Constraint
        penalty += minFillPenalty(assignments) * multiplierList.get(0); 

        // Preferences
        penalty += preferencesPenalty(assignments) * multiplierList.get(1); 

        //  Tasks that should be paired
        penalty += pairingPenalty(assignments) * multiplierList.get(2); 

        // Section Difference: Minimize uneven section
        penalty += sectionDifferencePenalty(assignments) * multiplierList.get(3); 

        return penalty;
    }

    private List<Slot> getAllSlots(List<Assignment> assignments) {
        List<Slot> slots = new ArrayList<>();
        for (Assignment assignment : assignments) {
            if (!slots.contains(assignment.getSlot())) {
                slots.add(assignment.getSlot());
            }
        }

        return slots;
    }

    private int minFillPenalty(List<Assignment> assignments) {
        int penalty = 0;

        for (Slot slot : getAllSlots(assignments)) {
            // Count the number of assignments for this slot
            long count = assignments.stream().filter(a -> a.getSlot().equals(slot)).count();

            if (count < slot.getMin()) {
                penalty += (slot.getMin() - (int) count) * penaltyList.get(0); 
            }
        }

        return penalty;
    }

    private int preferencesPenalty(List<Assignment> assignments) {
        int penalty = 0;

        for (Assignment assignment : assignments) {
            Slot assignedSlot = assignment.getSlot();
            Task task = assignment.getTask();

            if (!task.isPreferredSlot(assignedSlot)) {
                penalty += task.getPreferenceValue(assignedSlot); 
            }
        }

        return penalty;
    }

    private int pairingPenalty(List<Assignment> assignments) {
        int penalty = 0;

        for (Assignment assignment: assignments) {
            Task task = assignment.getTask();
            if (task.getPairs().isEmpty())continue;
            
            List<Task> neededToBePairedList = task.getPairs();
            Slot slot = assignment.getSlot();

            for (Task b: getTasksBySlot(assignments, slot)){
                if (!neededToBePairedList.isEmpty() && neededToBePairedList.contains(b))neededToBePairedList.remove(b);
            }

            return penalty += neededToBePairedList.size() * penaltyList.get(2);
        }

        return penalty;
    }

    private List<Task> getTasksBySlot(List<Assignment> assignments, Slot slot){
        List<Task> result = new ArrayList<>();
        for (Assignment assignment: assignments){
            if (assignment.getSlot().getDay().equals(slot.getDay())
            && assignment.getSlot().getStartTime().equals(slot.getStartTime())
            && assignment.getSlot().forGame() == slot.forGame()){
                result.add(assignment.getTask());
            }
        }
        return result;
    }

    // TODO: Minimize the difference in task assignment across sections
    private int sectionDifferencePenalty(List<Assignment> assignments) {
        int penalty = 0;
    
        penalty += Math.abs(assignments.size() % 2) * penaltyList.get(3); 
        
        return penalty;
    }
}
