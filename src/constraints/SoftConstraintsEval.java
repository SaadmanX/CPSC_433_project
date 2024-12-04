// package constraints;

// import java.util.ArrayList;
// import java.util.List;

// import model.Assignment;
// import model.slots.Slot;
// import model.task.Task;
// import model.constraints.Pair;
// import model.constraints.Preference;

// public class SoftConstraintsEval {
//     // Multiplier List (Order: minFill, pref, pair, secdiff)
//     private List<Integer> multiplierList;
    
//     // Penalty List (Order: gameMin, practiceMin, notPaired, section)
//     private List<Integer> penaltyList;

//     private List<Preference> preferenceList;

//     private List<Slot> allSlots;

//     private List<Pair> pairs;

//     public SoftConstraintsEval(List<Integer> multiplierList, List<Integer> penaltyList, List<Preference> prefer, List<Pair> pair, List<Slot> allSlots) {
//         this.multiplierList = multiplierList;
//         this.penaltyList = penaltyList;
//         this.preferenceList = prefer;
//         this.allSlots = allSlots;
//         this.pairs = pair;
//     }


//     //Calculate penalty difference to subtract
//     public int calculatePenalty(Assignment assignment, List<Assignment> curAssignments, int currentPenalty){
//         int penaltyDiff = 0;

//         // Min Fill Penalty
//         int minFilledDif = updateMinFillPenalty(assignment);
//         if (minFilledDif != -1)penaltyDiff +=  penaltyList.get(0);

//         //TODO Check logic again, basically it should be the same although many prefs for one task
//         // Preferences Penalty       
//         int preferenceDiff = updatePreferencesPenalty(assignment);
//         if (penaltyDiff != -1){
//             penaltyDiff += (preferenceDiff  * multiplierList.get(1));
//         }
        
//         //int pairingDiff = updatePairingPenalty(assignment, curAssignments);
//         //if (pairingDiff != -1)penaltyDiff += (pairingDiff * multiplierList.get(2));
                
//         // Section Difference Penalty
//         //penaltyDiff += updateSectionDifferencePenalty(assignment) * multiplierList.get(3);
                
//         return currentPenalty - penaltyDiff;
                   
//     }    
      


//     //Should only be called exactly once at the initial state, after only the newest assignment is evaluated
//     public int calculatePenalty(List<Assignment> assignments) {
//         int penalty = 0;

//         penalty += minFillPenalty(assignments) * multiplierList.get(0); 
//         System.out.println("penalty after min fill check: " + Integer.toString(penalty));

//         penalty += preferencesPenalty(assignments) * multiplierList.get(1); 
//         //penalty += preferencesPenalty() * multiplierList.get(1); 
//         System.out.println("penalty after preference check: " + Integer.toString(penalty));

//         penalty += pairingPenalty(assignments) * multiplierList.get(2); 
//         System.out.println("penalty after pair check: " + Integer.toString(penalty));

//         penalty += sectionDifferencePenalty(assignments) * multiplierList.get(3); 
//         System.out.println("penalty after section check: " + Integer.toString(penalty));

//         return penalty;
//     }

//     private int updatePreferencesPenalty(Assignment assignment) {
//         Slot slot = assignment.getSlot();
//         Task task = assignment.getTask();

//         if (task.isPreferredSlot(slot)){
//             return task.getPreferenceValue(slot);
//         }

//         return -1;
//     }

//     // private List<Slot> getAllSlots(List<Assignment> assignments) {
//     //     List<Slot> slots = new ArrayList<>();
//     //     for (Assignment assignment : assignments) {
//     //         if (!slots.contains(assignment.getSlot())) {
//     //             slots.add(assignment.getSlot());
//     //         }
//     //     }

//     //     return slots;
//     // }

//     private int minFillPenalty(List<Assignment> assignments) {
//         int penalty = 0;
//         for (Slot slot : allSlots) {
//             if (slot.getCurrentCount() < slot.getMin()) {
//                 penalty += (slot.getMin() - slot.getCurrentCount()); 
//             }
//         }

//         return penalty;
//     }

//     // Update Min Fill Penalty
//     private int updateMinFillPenalty(Assignment assignment) {
//         Slot slot = assignment.getSlot();
//         if (slot.getCurrentCount() >= slot.getMin())return -1;
        
//         return 1; //Difference between before and after is 1 * mult
//     }

//     private int preferencesPenalty(List<Assignment> assignments) {
//         int penalty = 0;

//         for (Assignment assignment : assignments) {
//             Slot assignedSlot = assignment.getSlot();
//             Task task = assignment.getTask();

//             if (!task.isPreferredSlot(assignedSlot)) {
//                 penalty += task.getPreferenceValue(assignedSlot); 
//             }
//         }

//         return penalty;
//     }

//     private int pairingPenalty(List<Assignment> assignments) {
//         int penalty = 0;

//         for (Assignment assignment: assignments) {
//             Task task = assignment.getTask();
//             if (task.getPairs().isEmpty())continue;
            
//             List<Task> neededToBePairedList = task.getPairs();
//             Slot slot = assignment.getSlot();

//             for (Task b: getTasksBySlot(assignments, slot)){
//                 if (!neededToBePairedList.isEmpty() && neededToBePairedList.contains(b))neededToBePairedList.remove(b);
//             }

//             penalty += neededToBePairedList.size() * penaltyList.get(2);
//         }

//         return penalty;
//     }

//     // private int pairingPenalty(List<Assignment> assignments) {
//     //     int penalty = 0;

//     //     for (Constraint c : pairs) {
//     //         Pair pair = (Pair) c;
//     //         String t1;
//     //         String t2;
//     //         t1 = pair.getTeam1Id();

//     //         t2 = pair.getTeam2Id();
//     //     }

//     //     for (Assignment a : assignments) {

//     //     }

//     //     return penalty;
//     // }


//     private List<Task> getTasksBySlot(List<Assignment> assignments, Slot slot){
//         List<Task> result = new ArrayList<>();
//         for (Assignment assignment: assignments){
//             if (assignment.getSlot().getDay().equals(slot.getDay())
//             && assignment.getSlot().getStartTime().equals(slot.getStartTime())
//             && assignment.getSlot().forGame() == slot.forGame()){
//                 result.add(assignment.getTask());
//             }
//         }
//         return result;
//     }

//     // TODO: Minimize the difference in task assignment across sections
//     //Different divisional games within a single age/tier group should be scheduled 
//     //at different times. For each pair of divisions that is scheduled into the same slot, 
//     //we add a penalty pensection to the Eval-value of an assignment.
//     private int sectionDifferencePenalty(List<Assignment> assignments) {
//         int penalty = 0;
    
//         penalty += Math.abs(assignments.size() % 2) * penaltyList.get(3); 
        
//         return penalty;
//     }
// }


package constraints;

import java.util.ArrayList;
import java.util.List;
import model.Assignment;
import model.slots.Slot;
import model.task.Task;
import model.constraints.Constraint;
import model.constraints.Pair;
import model.constraints.Preference;

public class SoftConstraintsEval {

    // Multiplier List (Order: minFill, pref, pair, secdiff)
    private List<Integer> multiplierList;

    // Penalty List (Order: gameMin, practiceMin, notPaird, section)
    private List<Integer> penaltyList;

    private List<Preference> preferenceList;
    private List<Slot> allSlots;
    private List<Constraint> pairs;
    public SoftConstraintsEval(List<Integer> multiplierList, List<Integer> penaltyList, List<Preference> prefer, List<Constraint> pair, List<Slot> allSlots) {
        this.multiplierList = multiplierList;
        this.penaltyList = penaltyList;
        this.preferenceList = prefer;
        this.allSlots = allSlots;
        this.pairs = pair;
    }

    public int calculatePenalty(List<Assignment> assignments) {
        int penalty = 0;

        // Min Fill Constraint
        penalty += minFillPenalty(assignments) * multiplierList.get(0); 
        System.out.println("penalty after min fill check: " + Integer.toString(penalty));

        // Preferences
        // penalty += preferencesPenalty(assignments) * multiplierList.get(1); 
        penalty += preferencesPenalty() * multiplierList.get(1); 
        System.out.println("penalty after preference check: " + Integer.toString(penalty));

        //  Tasks that should be paired
        penalty += pairingPenalty(assignments) * multiplierList.get(2); 
        System.out.println("penalty after pair check: " + Integer.toString(penalty));

        // Section Difference: Minimize uneven section
        penalty += sectionDifferencePenalty(assignments) * multiplierList.get(3); 
        System.out.println("penalty after section check: " + Integer.toString(penalty));

        return penalty;
    }

    // private List<Slot> getAllSlots(List<Assignment> assignments) {
    //     List<Slot> slots = new ArrayList<>();
    //     for (Assignment assignment : assignments) {
    //         if (!slots.contains(assignment.getSlot())) {
    //             slots.add(assignment.getSlot());
    //         }
    //     }

    //     return slots;
    // }

    private int minFillPenalty(List<Assignment> assignments) {
        int penalty = 0;

        for (Slot slot : allSlots) {
            // Count the number of assignments for this slot
            long count = assignments.stream().filter(a -> a.getSlot().equals(slot)).count();

            if (count < slot.getMin()) {
                penalty += (slot.getMin() - (int) count) * penaltyList.get(0); 
            }
        }

        return penalty;
    }

    // private int preferencesPenalty(List<Assignment> assignments) {
    //     int penalty = 0;

    //     for (Assignment assignment : assignments) {
    //         Slot assignedSlot = assignment.getSlot();
    //         Task task = assignment.getTask();

    //         if (!task.isPreferredSlot(assignedSlot)) {
    //             penalty += task.getPreferenceValue(assignedSlot); 
    //         }
    //     }
    //     return penalty;
    // }
    private int preferencesPenalty() {
        int penalty = 0;
        for (Preference p : preferenceList) {
            penalty += p.getPenalty();
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

            penalty += neededToBePairedList.size() * penaltyList.get(2);
        }

        return penalty;
    }

    // ** maybe add component such that each task carries a list of slots its assigned to. this will speed up this function more
    // private int pairingPenalty(List<Assignment> assignments) {
    //     int penalty = 0;
    //     for (Constraint c : pairs) {
    //         Pair pair = (Pair) c;
    //         String t1;
    //         String t2;
    //         t1 = pair.getTeam1Id();
    //         t2 = pair.getTeam2Id();
    //     }
    //     for (Assignment a : assignments) {
    //     }
    //     return penalty;
    // }
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
