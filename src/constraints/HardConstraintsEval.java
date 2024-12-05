package constraints;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;

public class HardConstraintsEval {
    
    // Validate just the new assignment's hard constraints
    public boolean validate(Assignment newAssignment) {
        if (!maxConstraint(newAssignment.getSlot())) {
            System.err.println("Failed: Max constraint violation");
            return false;
        }
        
        if (!noOverlappingPracticesAndGames(newAssignment)) {
            System.err.println("Failed: Overlapping practices and games constraint violation");
            return false;
        }
        
        if (!eveningDivisionConstraint(newAssignment)) {
            System.err.println("Failed: Evening division constraint violation");
            return false;
        }

        if (!nonOverlappingTimeForCertainLevels(newAssignment.getSlot())) {
            System.err.println("Failed: Non-overlapping time for certain levels constraint violation");
            return false;
        }
    
        if (!noGamesOnTuesdayMeeting(newAssignment)) {
            System.err.println("Failed: Game scheduled on Tuesday at 11 constraint violation");
            return false;
        }

        if (!specialPracticeBookingConstraint(newAssignment)) {
            System.err.println("Failed: Special practice booking constraint violation");
            return false;
        }
        
        if (!specialGamePracticeBookingConstraint(newAssignment)) {
            System.err.println("Failed: Special game practice booking constraint violation");
            return false;
        }

        if (!notCompatibleConstraint(newAssignment)) {
            System.err.println("Failed: Not compatible constraint violation");
            return false;
        }

        return true;
    }

    // Max Slot Capacity Constraint - O(1)
    private boolean maxConstraint(Slot slot) {
        return slot.getCurrentCount() <= slot.getMax();
    }

    // Check if new assignment creates practice/game overlap - O(k) where k is tasks in slot
    private boolean noOverlappingPracticesAndGames(Assignment newAssignment) {
        Task newTask = newAssignment.getTask();
        Slot slot = newAssignment.getSlot();
        String newId = newTask.getIdentifier();
        
        for (Task existingTask : slot.getAssignedTasks()) {
            if (newTask.getIsGame() != existingTask.getIsGame()) {
                String existingId = existingTask.getIdentifier();
                if (newTask.getIsGame()) {
                    if (newId.equals(existingId.substring(0, existingId.length() - 6))) {
                        return false;
                    }
                } else {
                    if (existingId.equals(newId.substring(0, newId.length() - 6))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    // Evening Division Constraint - O(1)
    private boolean eveningDivisionConstraint(Assignment assignment) {
        if (assignment.getTask().getDivision().startsWith("DIV 9")) {
            return assignment.getSlot().getSlotStartTime() == 18.0;
        }
        return true;
    }

    // Non-Overlapping Time for U15-U19 - O(1)
    private boolean nonOverlappingTimeForCertainLevels(Slot slot) {
        return slot.getU1519() <= 1;
    }

    // Not Compatible Constraint - O(k) where k is tasks in slot
    private boolean notCompatibleConstraint(Assignment newAssignment) {
        Task newTask = newAssignment.getTask();
        Slot slot = newAssignment.getSlot();
        
        for (Task existingTask : slot.getAssignedTasks()) {
            if (!newTask.equals(existingTask) && newTask.isNotCompatibleWith(existingTask)) {
                return false;
            }
        }
        return true;
    }

    // Special Practice Booking Constraint - O(1)
    private boolean specialPracticeBookingConstraint(Assignment assignment) {
        if (assignment.getTask().isSpecialPractice()) {
            Slot slot = assignment.getSlot();
            return slot.getDay().matches("TU|TH") && slot.getSlotStartTime() == 18.0;
        }
        return true;
    }

    // Special Game and Practice Booking Constraint - O(k) where k is tasks in slot
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

    // No Games on Tuesday Meeting - O(1)
    private boolean noGamesOnTuesdayMeeting(Assignment assignment) {
        if (assignment.getTask().getIsGame()) {
            Slot slot = assignment.getSlot();
            return !(slot.getDay().equals("TU") && slot.getSlotStartTime() == 11.0);
        }
        return true;
    }
}
