package constraints;

import java.util.List;
import model.Assignment;
import model.SearchState;
import model.constraints.PartialAssignment;

public class HardConstraintsEval {

    // Validate that the current state satisfies all partial assignments
    public boolean validatePartialAssignmentsForState(List<PartialAssignment> partialAssignments, SearchState currentState) {
        for (PartialAssignment partial : partialAssignments) {
            /** 
            System.out.println("\n=== Checking Partial Assignment ===");
            System.out.println("Partial Assignment requires:");
            System.out.println("Task: " + partial.getTaskIdentifier());
            System.out.println("Day: " + partial.getDay());
            System.out.println("Time: " + partial.getTime());
            */
            boolean isSatisfied = currentState.getAssignments().stream().anyMatch(assignment -> {
                /* 
                System.out.println("\nComparing with Current Assignment:");
                System.out.println("Current Task: " + assignment.getTask().getIdentifier());
                System.out.println("Current Day: " + assignment.getSlot().getDay());
                System.out.println("Current Time: " + assignment.getSlot().getStartTime());
                */
                
                boolean taskMatches = assignment.getTask().getIdentifier().equals(partial.getTaskIdentifier());
                boolean dayMatches = assignment.getSlot().getDay().equals(partial.getDay());
                boolean timeMatches = assignment.getSlot().getStartTime().equals(partial.getTime());
                
                /* 
                System.out.println("\nResults:");
                System.out.println("Task Match: " + taskMatches);
                System.out.println("Day Match: " + dayMatches); 
                System.out.println("Time Match: " + timeMatches);
                System.out.println("Overall Match: " + (taskMatches && dayMatches && timeMatches));
                */
                
                return taskMatches && dayMatches && timeMatches;
            });
            
            if (!isSatisfied) {
                System.err.println("FAILED: Partial Assignment not satisfied: " + partial);
                return false;
            }
        }
        //System.out.println("\nSUCCESS: All Partial Assignments satisfied!");
        return true;
    }    
    // Validate all hard constraints for a list of assignments
    public boolean validate(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            //System.out.println("\nValidating constraints for assignment: " + assignment);
            
            if (!maxConstraint(assignment)) {
                System.err.println("Failed: Max constraint violation");
                return false;
            }
            
            //if (!noOverlappingPracticesAndGames(assignments)) {
            //    System.err.println("Failed: Overlapping practices and games constraint violation");
            //    return false;
            //}
            
            if (!eveningDivisionConstraint(assignment)) {
                System.err.println("Failed: Evening division constraint violation");
                return false;
            }
            
            //if (!nonOverlappingTimeForCertainLevels(assignments)) {
            //    System.err.println("Failed: Non-overlapping time for certain levels constraint violation");
            //    return false;
            //}
        

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
        //System.out.println("Success: All constraints passed");
        return true;
    }

    //Max-Min Slot Capacity Constraint
    private boolean maxConstraint(Assignment assignment) {
        return assignment.getSlot().getMax() >= 0;
    }

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
