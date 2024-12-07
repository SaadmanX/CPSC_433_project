package constraints;

import java.util.ArrayList;
import java.util.List;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;

//TODO: LOGIC FOR OVERLAPPING AGAIN
//TODO: GAMES: ON MONDAY/ FRIDAY: 1 hour session, from 8-21, TUES: 1.5, from 8:00-20:00
//TODO: PRACTICES: Same as games for M/F: 1 hour and T, F: 2 hour session from 8:00-20:00
public class HardConstraintsEval {

    public boolean validate(Assignment newAssignment, List<Assignment> previouAssignments) {
        if (!maxConstraint(newAssignment.getSlot())) {
            System.out.println("hard constraint failed: max constraint");
            return false;
        }
        
        if (!noOverlappingPracticesAndGames(newAssignment)) {
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

        if (!specialPracticeBookingConstraint(newAssignment)) {
            System.out.println("hard constraint failed: special practice");
            return false;
        }
        
        if (!specialGamePracticeBookingConstraint(newAssignment)) {
            System.out.println("hard constraint failed: special game practice");
            return false;
        }

        if (!notCompatibleConstraint(newAssignment)) {
            System.out.println("hard constraint failed: not compatible");
            return false;
        }

        return true;
    }

    private boolean maxConstraint(Slot slot) {
        //System.out.println("Max: " + slot.getMax() + " vs Curr: " + slot.getCurrentCount());
        boolean result = slot.getCurrentCount() <= slot.getMax();
        return result;
    }

    //TODO: THIS MF TOO
    // not the same slot. if new assignment is a game, look at the practice slots, and vice versa
    private boolean noOverlappingPracticesAndGames(Assignment newAssignment) {    

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

        double start1 = slot1.getSlotStartTime();
        double start2 = slot2.getSlotStartTime();

        if (start1 == start2)return true;
    
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
                // System.out.println(existingTask + " is not compatible with " + newTask);
                return false;
            }
        }
        return true;
    }

    private boolean specialPracticeBookingConstraint(Assignment assignment) {
        
        if (assignment.getTask().isSpecialPractice()) {
            Slot slot = assignment.getSlot();
            boolean result = slot.getDay().matches("TU|TH") && slot.getSlotStartTime() == 18.0;
            return result;
        }
        return true;
    }

    private boolean specialGamePracticeBookingConstraint(Assignment newAssignment) {
        Task newTask = newAssignment.getTask();
        String newTaskId = newTask.getIdentifier();
        String newTeamType = "";
        
        if (newTaskId.startsWith("CMSA U12T1S")) newTeamType = "U12T1S";
        else if (newTaskId.startsWith("CMSA U12T1")) newTeamType = "U12T1";
        else if (newTaskId.startsWith("CMSA U13T1S")) newTeamType = "U13T1S";
        else if (newTaskId.startsWith("CMSA U13T1")) newTeamType = "U13T1";
                
        if (!newTeamType.isEmpty()) {
            for (Task existingTask : newAssignment.getSlot().getAssignedTasks()) {
                String existingId = existingTask.getIdentifier();
                String existingTeamType = "";
                
                if (existingId.startsWith("CMSA U12T1S")) existingTeamType = "U12T1S";
                else if (existingId.startsWith("CMSA U12T1")) existingTeamType = "U12T1";
                else if (existingId.startsWith("CMSA U13T1S")) existingTeamType = "U13T1S";
                else if (existingId.startsWith("CMSA U13T1")) existingTeamType = "U13T1";
                
                if ((newTeamType.equals("U12T1") && existingTeamType.equals("U12T1S")) ||
                    (newTeamType.equals("U12T1S") && existingTeamType.equals("U12T1")) ||
                    (newTeamType.equals("U13T1") && existingTeamType.equals("U13T1S")) ||
                    (newTeamType.equals("U13T1S") && existingTeamType.equals("U13T1"))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean noGamesOnTuesdayMeeting(Assignment assignment) {
        if (assignment.getTask().getIsGame()) {
            Slot slot = assignment.getSlot();
            boolean result = !(slot.getDay().equals("TU") && slot.getStartTime().equals("11:00"));
            return result;
        }
        return true;
    }
}