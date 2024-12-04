// package constraints;

// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;

// import model.Assignment;
// import model.slots.Slot;
// import model.task.Task;


// public class HardConstraintsEval {

//     // Validate all hard constraints for a list of assignments
//     public boolean validate(List<Assignment> assignments, List<Slot> slots, List<Slot> commonSlots) {
//         if (!maxConstraint(slots)) {
//             System.err.println("Failed: Max constraint violation");
//             return false;
//         }
        
//         if (!noOverlappingPracticesAndGames(commonSlots)) {
//             System.err.println("Failed: Overlapping practices and games constraint violation");
//             return false;
//         }
        
//         if (!eveningDivisionConstraint(assignments)) {
//             System.err.println("Failed: Evening division constraint violation");
//             return false;
//         }

//         if (!nonOverlappingTimeForCertainLevels(slots)) {
//             System.err.println("Failed: Non-overlapping time for certain levels constraint violation");
//             return false;
//         }
    

//         if (!noGamesOnTuesdayMeeting(assignments)) {
//             System.err.println("Failed: Game scheduled on Tuesday at 11 constraint violation");
//             return false;
//         }

//         if (!specialPracticeBookingConstraint(assignments)) {
//             System.err.println("Failed: Special practice booking constraint violation");
//             return false;
//         }
        
//         if (!specialGamePracticeBookingConstraint(assignments)) {
//             System.err.println("Failed: Special game practice booking constraint violation");
//             return false;
//         }
//         if (!notCompatibleConstraint(assignments)) {
//             System.err.println("Failed: Special game practice booking constraint violation");
//             return false;
//         }
//         System.out.println("Success: All constraints passed");
//         return true;
//     }

//     //Max-Min Slot Capacity Constraint
//     private boolean maxConstraint(List<Slot> slots) {
//         for (Slot slot : slots) {
//             return slot.getCurrentCount() <= slot.getMax();
//         }
//         return true;
//     }


//     // 2. Practices and Games Cannot Overlap
//     private boolean noOverlappingPracticesAndGames(List<Slot> slots) {
//         // ** not the most optimal, i know. but it is correctly checking now. and its not that bad since it only checks overlapping slots for game and practice
//         for (Slot s : slots) {
//             for (Task t1 : s.getAssignedTasks()) {
//                 for (Task t2 : s.getAssignedTasks()) {
//                     if (t1.equals(t2)) {
//                         continue;
//                     }
//                     if (t1.getIsGame() == t2.getIsGame()) {
//                         continue;
//                     }
//                     String task1 = t1.getIdentifier();
//                     String task2 = t2.getIdentifier();
//                     if (t1.getIsGame()) {
//                         if (task1.equals(task2.substring(0, task2.length() - 6))) {
//                             return false;
//                         }
//                     } else {
//                         if (task2.equals(task1.substring(0, task1.length() - 6))) {
//                             return false;
//                         }
//                     }
//                 }
//             }
//         }
//         return true;
//     }    
    
//     // 3. Evening Division Constraint
//     private boolean eveningDivisionConstraint(List<Assignment> assignments) {
//         for (Assignment assignment : assignments) {
//             boolean isEveningSlot = assignment.getSlot().getSlotStartTime() == 18.0;
//             if (assignment.getTask().getDivision().startsWith("DIV 9")) {
//                 return isEveningSlot;
//             }
//         }
//         return true;
//     }

//     // 4. Non-Overlapping Time for Certain Levels (e.g., U15/U16/U17/U19)
//     private boolean nonOverlappingTimeForCertainLevels(List<Slot> slots) {
//         for (Slot s : slots) {
//             if (s.getU1519() > 1) {
//                 return false;
//             }
//         }
//         return true;
//     }

//     // && still O(n^2)
//     // 8. Not Compatible Constraint
//     private boolean notCompatibleConstraint(List<Assignment> assignments) {
//         for (Assignment a : assignments) {
//             Slot s = a.getSlot();
//             for (Task t : s.getAssignedTasks()) {
//                 if (!a.getTask().equals(t) && a.getTask().isNotCompatibleWith(t)) {
//                     return false;
//                 }
//             }
//         }
//         return true;
//     }

//     // 9. Special Practice Booking Constraint
//     private boolean specialPracticeBookingConstraint(List<Assignment> assignments) {
//         for (Assignment a : assignments) {
//             if (a.getTask().isSpecialPractice() && !(a.getSlot().getDay().matches("TU|TH") && a.getSlot().getSlotStartTime() == 18.0)) {
//                 return false;
//             }
//         }
//         return true;
//     }

//     // 10. Special Game and Practice Booking Constraint. CMSA U12T1 and CMSA U12T1S practice and games cannot be placed in the in the same slot. same with CMSA U13T1 and CMSA U13T1S
//     private boolean specialGamePracticeBookingConstraint(List<Assignment> assignments) {
//         Map<Slot, Set<String>> slotTeams = new HashMap<>();
        
//         for (Assignment a : assignments) {
//             String taskId = a.getTask().getIdentifier();
//             String teamType = "";
            
//             if (taskId.startsWith("CMSA U12T1S")) teamType = "U12T1S";
//             else if (taskId.startsWith("CMSA U12T1")) teamType = "U12T1";
//             else if (taskId.startsWith("CMSA U13T1S")) teamType = "U13T1S";
//             else if (taskId.startsWith("CMSA U13T1")) teamType = "U13T1";
            
//             if (!teamType.isEmpty()) {
//                 slotTeams.computeIfAbsent(a.getSlot(), k -> new HashSet<>())
//                         .add(teamType);

//                 Set<String> teamsInSlot = slotTeams.get(a.getSlot());
//                 if ((teamsInSlot.contains("U12T1") && teamsInSlot.contains("U12T1S")) ||
//                     (teamsInSlot.contains("U13T1") && teamsInSlot.contains("U13T1S"))) {
//                     return false;
//                 }
//             }
//         }
//         return true;
//     }

//     private boolean noGamesOnTuesdayMeeting(List<Assignment> assignments) {
//         for (Assignment a : assignments) {
//             if (a.getTask().getIsGame() && a.getSlot().getDay().startsWith("TU") && a.getSlot().getSlotStartTime() == 11.0) {
//                 return false;
//             }
//         }
//         return true;
//     }
// }



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
