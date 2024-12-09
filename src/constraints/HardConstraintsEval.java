package constraints;

import java.util.List;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;


public class HardConstraintsEval {

    public boolean validate(Assignment newAssignment, List<Assignment> previouAssignments, boolean isSpecialBooking) {
        if (!maxConstraint(newAssignment.getSlot())) {
            //System.out.println("hard constraint failed: max constraint");
            return false;
        }
        
        if (!noOverlappingPracticesAndGames(newAssignment, previouAssignments)) {
            //System.out.println("hard constraint failed: overlapping game and practice");
            return false;
        }
        
        if (!eveningDivisionConstraint(newAssignment)) {
            //System.out.println("hard constraint failed: evening div");
            return false;
        }

        if (!nonOverlappingTimeForCertainLevels(newAssignment, previouAssignments)) {
            //System.out.println("hard constraint failed: overlaping levels");
            return false;
            
        }

        if (!noGamesOnTuesdayMeeting(newAssignment)) {
            //System.out.println("hard constraint failed: game on tuesday meeting");
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
            //System.out.println("hard constraint failed: not compatible");
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
            
            // practices with no div values
            if (!curTask.getIsGame() && curTask.getDivision().equals("")) {
                // check to see if GAMEs identifier contains PRACTIEC's identifier minus the training PRC <NUM>
                if (prevTask.getIdentifier().contains(curTask.getIdentifier().subSequence(0, curTask.getIdentifier().length() - 7))) {
                    Slot prevSlot = assignment.getSlot();
                    return !isOverlap(prevSlot, curSlot);
                }
            }

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


    // private boolean nonOverlappingTimeForCertainLevels(Assignment assignment , List<Assignment> previousAssignments) {
    //     Task task = assignment.getTask();
        
    //     if (!task.isU1519()) {
    //         return true; 
    //     }
        
    //     Slot currentSlot = assignment.getSlot();
    //     for (Assignment prevAssignment : previousAssignments) {
    //         Task prevTask = prevAssignment.getTask();
    //         Slot prevSlot = prevAssignment.getSlot();
        
    //         if (prevTask.isU1519()) {
    //             if (isOverlap(prevSlot, currentSlot)) {
    //                 //System.out.println("Overlap detected between: " + prevTask.getIdentifier() + " and " + task.getIdentifier());
    //                 return false;
    //             }
    //         }
    //     }
        
    //     return true;
    // }

    private boolean nonOverlappingTimeForCertainLevels(Assignment assignment , List<Assignment> previousAssignments) {
        Task task = assignment.getTask();
        
        if (!task.isU1519() || !task.getIsGame()) {
            return true; 
        }
        
        Slot currentSlot = assignment.getSlot();
        for (Assignment prevAssignment : previousAssignments) {
            Task prevTask = prevAssignment.getTask();
            Slot prevSlot = prevAssignment.getSlot();
        
            if (prevTask.getIsGame() && prevTask.isU1519() && prevTask.getAge() != task.getAge()) {
                if (isOverlap(prevSlot, currentSlot)) {
                    // System.out.println("Overlap detected between: " + prevTask.getIdentifier() + " and " + task.getIdentifier());
                    return false;
                }
            }
        }
        
        return true;
    }

    private boolean isOverlap(Slot slot1, Slot slot2) {
        return (slot1.getDay().equals(slot2.getDay()) && slot1.getStartTime().equals(slot2.getStartTime()));
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