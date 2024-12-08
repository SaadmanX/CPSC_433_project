package constraints;

import java.util.List;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;


public class HardConstraintsEval {

    public boolean validate(Assignment newAssignment, List<Assignment> previouAssignments, boolean isSpecialBooking) {
        if (!maxConstraint(newAssignment.getSlot())) {
            System.out.println("hard constraint failed: max constraint");
            return false;
        }
        
        if (!noOverlappingPracticesAndGames(newAssignment, previouAssignments)) {
            System.out.println("hard constraint failed: overlapping game and practice");
            return false;
        }
        
        if (!eveningDivisionConstraint(newAssignment)) {
            System.out.println("hard constraint failed: evening div");
            return false;
        }

        if (!nonOverlappingTimeForCertainLevels(newAssignment, previouAssignments)) {
            System.out.println("hard constraint failed: overlaping levels");
            return false;
            
        }

        if (!noGamesOnTuesdayMeeting(newAssignment)) {
            System.out.println("hard constraint failed: game on tuesday meeting");
            return false;
        }

        //SPECIAL BOOKING IS VIP SO THIS IS HERE
    
        // if (isSpecialBooking) {
        //     if (!specialBookingConstraint(newAssignment)) {
        //         System.out.println("hard constraint failed: special practice");
        //         return false;
        //     }
        // }

        if (!notCompatibleConstraint(newAssignment)) {
            System.out.println("hard constraint failed: not compatible");
            return false;
        }

        return true;
    }

    private boolean maxConstraint(Slot slot) {
        boolean result = slot.getCurrentCount() <= slot.getMax();
        return result;
    }

    private boolean noOverlappingPracticesAndGames(Assignment newAssignment, List<Assignment> previousAssignments) {    
        Task curTask = newAssignment.getTask();
        Slot curSlot = newAssignment.getSlot();

        for (Assignment assignment: previousAssignments){
            Task prevTask = assignment.getTask();

            //Only 1 game and 1 practice
            if (curTask.getIsGame() == prevTask.getIsGame())continue;
            
            if (!curTask.getIsGame()){ // If it is practice
                if (!curTask.getIdentifier().contains(prevTask.getIdentifier())){
                    continue;
                }
            } else { //If it is game
                if (!prevTask.getIdentifier().contains(curTask.getIdentifier()))continue;
            }

            Slot prevSlot = assignment.getSlot();
            if (isOverlap(prevSlot, curSlot))return false;
        }

        return true;
    }


    private boolean eveningDivisionConstraint(Assignment assignment) {    
        if (assignment.getTask().getDivision().startsWith("9")) {
            boolean result = assignment.getSlot().getSlotStartTime() >= 18.0;
            return result;
        }
        return true;
    }


    private boolean nonOverlappingTimeForCertainLevels(Assignment assignment , List<Assignment> previousAssignments) {
        Task task = assignment.getTask();
        
        if (!task.isU1519()) {
            return true; 
        }
        
        Slot currentSlot = assignment.getSlot();
        for (Assignment prevAssignment : previousAssignments) {
            Task prevTask = prevAssignment.getTask();
            Slot prevSlot = prevAssignment.getSlot();
        
            if (prevTask.isU1519()) {
                if (isOverlap(prevSlot, currentSlot)) {
                    //System.out.println("Overlap detected between: " + prevTask.getIdentifier() + " and " + task.getIdentifier());
                    return false;
                }
            }
        }
        
        return true;
    }

    private boolean isOverlap(Slot slot1, Slot slot2) {
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
    

    private boolean notCompatibleConstraint(Assignment newAssignment) {
        Task newTask = newAssignment.getTask();
        Slot slot = newAssignment.getSlot();

        // newTask.printNotCombatible();
        for (Task existingTask : slot.getAssignedTasks()) {
            if (newTask.isNotCompatibleWith(existingTask)) {
                return false;
            }
        }
        return true;
    }

    
    // && I probably do not need this at all. since the special practices are being added to unwanted slots anyways
    //Any practices/games of CMSA U12T1 and practices/games of CMSA U13T1 cannot be scheduled overlap TU: 18
    // private boolean specialBookingConstraint(Assignment assignment) {
    //     if (assignment.getTask().getIdentifier().contains("CMSA U12T1S") || assignment.getTask().getIdentifier().contains("CMSA U13T1S"))
    //     {
    //         Slot temp = new Slot("TU", "18:00", 1, 1, false);
    //         if (!isOverlap(assignment.getSlot(), temp))return false;
    //     }

    //     return true;
    // }

    private boolean noGamesOnTuesdayMeeting(Assignment assignment) {
        if (assignment.getTask().getIsGame()) {
            Slot slot = assignment.getSlot();
            boolean result = !(slot.getDay().equals("TU") && slot.getStartTime().equals("11:00"));
            return result;
        }
        return true;
    }
}