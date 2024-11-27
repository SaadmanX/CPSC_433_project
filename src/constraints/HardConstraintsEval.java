package constraints;

import java.util.List;
import model.Assignment;
import model.SearchState;
import model.constraints.PartialAssignment;

public class HardConstraintsEval {

    // Validate that the current state satisfies all partial assignments
    public boolean validatePartialAssignmentsForState(List<PartialAssignment> partialAssignments, SearchState currentState) {
        for (PartialAssignment partial : partialAssignments) {
            boolean isSatisfied = currentState.getAssignments().stream().anyMatch(assignment -> 
                assignment.getTask().getIdentifier().equals(partial.getTaskIdentifier())
                && assignment.getSlot().getDay().equals(partial.getDay())
                && assignment.getSlot().getStartTime().equals(partial.getTime())
            );

            if (!isSatisfied) {
                System.err.println("Partial Assignment not satisfied: " + partial);
                return false;
            }
        }
        return true;
    }
    
    
    // Validate all hard constraints for a list of assignments
    public boolean validate(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            if (!maxConstraint(assignment)) return false;
            if (!noOverlappingPracticesAndGames(assignments)) return false;
            if (!eveningDivisionConstraint(assignment)) return false;
            if (!nonOverlappingTimeForCertainLevels(assignments)) return false;
            //if (!unwantedSlotConstraint(assignment)) return false;
            if (!notCompatibleConstraint(assignments)) return false;
            if (!specialPracticeBookingConstraint(assignments)) return false;
        }
        return true;
    }

    //Max-Min Slot Capacity Constraint
    private boolean maxConstraint(Assignment assignment) {
        return assignment.getSlot().getMax() > 0;
    }

    // 2. Practices and Games Cannot Overlap
    private boolean noOverlappingPracticesAndGames(List<Assignment> assignments) {
        for (Assignment a : assignments) {
            for (Assignment b : assignments) {
                if (!a.equals(b) && a.getSlot().equals(b.getSlot())
                        && a.getTask().getDivision().equals(b.getTask().getDivision())) {
                    return false;
                }
            }
        }
        return true;
    }

    // 3. Evening Division Constraint
    private boolean eveningDivisionConstraint(Assignment assignment) {
        boolean isEveningSlot = assignment.getSlot().getStartTime().compareTo("18:00") >= 0;
        if (assignment.getTask().getDivision().startsWith("DIV 9")) {
            return isEveningSlot;
        }
        return true;
    }

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
}
