package constraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;



public class HardConstraintsEval {

    // Validate all hard constraints for a list of assignments
    public boolean validate(List<Assignment> assignments, List<Slot> allSlots, List<Task> allTasks) {
        //System.out.println("\nValidating constraints for assignment: " + assignment);
        
        if (!maxConstraint(assignments)) {
            System.err.println("Failed: Max constraint violation");
            return false;
        }
        
        if (!noOverlappingPracticesAndGames(allSlots)) {
            System.err.println("Failed: Overlapping practices and games constraint violation");
            return false;
        }
        
        if (!eveningDivisionConstraint(assignments)) {
            System.err.println("Failed: Evening division constraint violation");
            return false;
        }
        
        if (!nonOverlappingTimeForCertainLevels(allSlots)) {
            System.err.println("Failed: Non-overlapping time for certain levels constraint violation");
            return false;
        }
    

        if (!noGamesOnTuesdayMeeting(assignments)) {
            System.err.println("Failed: Game scheduled on Tuesday at 11 constraint violation");
            return false;
        }

        if (!specialPracticeBookingConstraint(assignments)) {
            System.err.println("Failed: Special practice booking constraint violation");
            return false;
        }
        
        if (!specialGamePracticeBookingConstraint(assignments)) {
            System.err.println("Failed: Special game practice booking constraint violation");
            return false;
        }

        if (!notCompatibleConstraint(assignments)) {
            System.err.println("Failed: Special game practice booking constraint violation");
            return false;
        }
        System.out.println("Success: All constraints passed");
        return true;
    }

    //Max-Min Slot Capacity Constraint
    private boolean maxConstraint(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            if (assignment.getSlot().getMax() >= 0) {
                return false;
            }
        }
        return true;
    }

    // 2. Practices and Games Cannot Overlap
    private boolean noOverlappingPracticesAndGames(List<Slot> allSlots) {
        for (Slot s : allSlots) {
            if (s.getHasSameGameAsPractice()) {
                return false;
            }
        }
        return true;
    }    
    // 3. Evening Division Constraint
    private boolean eveningDivisionConstraint(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            boolean isEveningSlot = assignment.getSlot().getSlotStartTime() == 18.0;
            if (assignment.getTask().getDivision().startsWith("DIV 9")) {
                return isEveningSlot;
            }
        }
        return true;
    }

    // 4. Non-Overlapping Time for Certain Levels (e.g., U15/U16/U17/U19)
    private boolean nonOverlappingTimeForCertainLevels(List<Slot> slots) {
        for (Slot s : slots) {
            if (s.getU1519() > 1) {
                return false;
            }
        }

        return true;
    }

    // 8. Not Compatible Constraint
    private boolean notCompatibleConstraint(List<Assignment> assignments) {
        for (Assignment a : assignments) {
            for (Assignment b : assignments) {
                if (!a.equals(b) && a.getTask().isNotCompatibleWith(b.getTask())) {
                    return false;
                }
            }
        }
        return true;
    }

    // 9. Special Practice Booking Constraint
    private boolean specialPracticeBookingConstraint(List<Assignment> assignments) {
        for (Assignment a : assignments) {
            if (a.getTask().isSpecialPractice() && !(a.getSlot().getDay().matches("TU|TH") && a.getSlot().getSlotStartTime() == 18.0)) {
                return false;
            }
        }
        return true;
    }

    // 10. Special Game and Practice Booking Constraint. CMSA U12T1 and CMSA U12T1S practice and games cannot be placed in the in the same slot. same with CMSA U13T1 and CMSA U13T1S
    private boolean specialGamePracticeBookingConstraint(List<Assignment> assignments) {
        Map<Slot, Set<String>> slotTeams = new HashMap<>();
        
        for (Assignment a : assignments) {
            String taskId = a.getTask().getIdentifier();
            String teamType = "";
            
            if (taskId.startsWith("CMSA U12T1S")) teamType = "U12T1S";
            else if (taskId.startsWith("CMSA U12T1")) teamType = "U12T1";
            else if (taskId.startsWith("CMSA U13T1S")) teamType = "U13T1S";
            else if (taskId.startsWith("CMSA U13T1")) teamType = "U13T1";
            
            if (!teamType.isEmpty()) {
                slotTeams.computeIfAbsent(a.getSlot(), k -> new HashSet<>())
                        .add(teamType);
                
                Set<String> teamsInSlot = slotTeams.get(a.getSlot());
                if ((teamsInSlot.contains("U12T1") && teamsInSlot.contains("U12T1S")) ||
                    (teamsInSlot.contains("U13T1") && teamsInSlot.contains("U13T1S"))) {
                    return false;
                }
            }
        }
        return true;
    }


    private boolean noGamesOnTuesdayMeeting(List<Assignment> assignments) {
        for (Assignment a : assignments) {
            if (a.getTask().getIsGame() && a.getSlot().getDay().startsWith("TU") && a.getSlot().getSlotStartTime() == 11.0) {
                return false;
            }
        }
        return true;
    }
}
