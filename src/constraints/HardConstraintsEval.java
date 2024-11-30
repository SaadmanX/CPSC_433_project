package constraints;

import java.util.List;
import model.Assignment;

public class HardConstraintsEval {

    // Validate all hard constraints for a list of assignments
    public boolean validate(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            //System.out.println("\nValidating constraints for assignment: " + assignment);
            
            if (!maxConstraint(assignment)) {
                System.err.println("Failed: Max constraint violation");
                return false;
            }
            
            if (!noOverlappingPracticesAndGames(assignments)) {
                System.err.println("Failed: Overlapping practices and games constraint violation");
                return false;
            }
            
            if (!eveningDivisionConstraint(assignment)) {
                System.err.println("Failed: Evening division constraint violation");
                return false;
            }
            
            if (!nonOverlappingTimeForCertainLevels(assignments)) {
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
        }
        System.out.println("Success: All constraints passed");
        return true;
    }

    //Max-Min Slot Capacity Constraint
    private boolean maxConstraint(Assignment assignment) {
        return assignment.getSlot().getMax() >= 0;
    }

    // ^^ can be better if we store slots in tasks, much less comparisons. actually can be O(n) if we store tasks in assignment, and a boolean method called hasSamePracticeAsGame().
    // 2. Practices and Games Cannot Overlap
    private boolean noOverlappingPracticesAndGames(List<Assignment> assignments) {
        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            for (int j = i + 1; j < assignments.size(); j++) {
                Assignment b = assignments.get(j);
                if ((a.getTask().getIsGame() != b.getTask().getIsGame()) &&
                    a.getSlot().equals(b.getSlot()) &&
                    a.getTask().getDivision().equals(b.getTask().getDivision())) {
                    return false;
                }
            }
        }
        return true;
    }    
    // 3. Evening Division Constraint
    private boolean eveningDivisionConstraint(Assignment assignment) {
        boolean isEveningSlot = assignment.getSlot().getSlotStartTime() == 18.0;
        if (assignment.getTask().getDivision().startsWith("DIV 9")) {
            return isEveningSlot;
        }
        return true;
    }

    // ^^ can be O(n) if we store slots for every task
    // 4. Non-Overlapping Time for Certain Levels (e.g., U15/U16/U17/U19)
    private boolean nonOverlappingTimeForCertainLevels(List<Assignment> assignments) {
        for (Assignment a : assignments) {
            for (Assignment b : assignments) {
                if (!a.equals(b)
                        && a.getSlot().equals(b.getSlot())
                        && a.getTask().getLevel().matches("U1[5-9]")
                        && b.getTask().getLevel().matches("U1[5-9]")) {
                    return false;
                }
            }
        }
        return true;
    }

    // ^^ can be O(n) if we store slots for every task
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
        for (Assignment a : assignments) {
            for (Assignment b : assignments) {
                if (a.equals(b)) {
                    continue;
                }
                
                boolean isU12Conflict = (
                    ((a.getTask().getIdentifier().matches("^CMSA U12T1 .*") && 
                        b.getTask().getIdentifier().matches("^CMSA U12T1S .*")) ||
                    (b.getTask().getIdentifier().matches("^CMSA U12T1 .*") && 
                        a.getTask().getIdentifier().matches("^CMSA U12T1S .*"))) &&
                    a.getSlot().equals(b.getSlot())
                );
                
                boolean isU13Conflict = (
                    ((a.getTask().getIdentifier().matches("^CMSA U13T1 .*") && 
                        b.getTask().getIdentifier().matches("^CMSA U13T1S .*")) ||
                    (b.getTask().getIdentifier().matches("^CMSA U13T1 .*") && 
                        a.getTask().getIdentifier().matches("^CMSA U13T1S .*"))) &&
                    a.getSlot().equals(b.getSlot())
                );
                    
                if (isU12Conflict || isU13Conflict) {
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
