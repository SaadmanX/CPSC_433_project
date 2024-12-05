package constraints;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;

public class HardConstraintsEval {
    
    public boolean validate(Assignment newAssignment) {
        /** 
        System.out.println("\n========== VALIDATING ASSIGNMENT ==========");
        System.out.println("Validating assignment: " + newAssignment.getTask().getIdentifier() + 
                          " -> Slot[" + newAssignment.getSlot().getDay() + " " + 
                          newAssignment.getSlot().getStartTime() + "]");
        */
        if (!maxConstraint(newAssignment.getSlot())) {
            /** 
            System.err.println("Failed: Max constraint violation");
            System.err.println("Current count: " + newAssignment.getSlot().getCurrentCount() + 
                             ", Max allowed: " + newAssignment.getSlot().getMax());
            */
            return false;
        }
        
        if (!noOverlappingPracticesAndGames(newAssignment)) {
            /** 
            System.err.println("Failed: Overlapping practices and games constraint violation");
            System.err.println("Task: " + newAssignment.getTask().getIdentifier() + 
                             " (isGame=" + newAssignment.getTask().getIsGame() + ")");
            */
            return false;
        }
        
        if (!eveningDivisionConstraint(newAssignment)) {
            /** 
            System.err.println("Failed: Evening division constraint violation");
            System.err.println("Division: " + newAssignment.getTask().getDivision() + 
                             ", Time: " + newAssignment.getSlot().getSlotStartTime());
                             */
            return false;
        }

        if (!nonOverlappingTimeForCertainLevels(newAssignment.getSlot())) {
            /** 
            System.err.println("Failed: Non-overlapping time for certain levels constraint violation");
            System.err.println("U15-19 count in slot: " + newAssignment.getSlot().getU1519());
            */
            return false;
            
        }
    
        if (!noGamesOnTuesdayMeeting(newAssignment)) {
            /** 
            System.err.println("Failed: Game scheduled on Tuesday at 11 constraint violation");
            System.err.println("Day: " + newAssignment.getSlot().getDay() + 
                             ", Time: " + newAssignment.getSlot().getSlotStartTime() + 
                             ", IsGame: " + newAssignment.getTask().getIsGame());
             */
            return false;
        }

        if (!specialPracticeBookingConstraint(newAssignment)) {
            /**
            System.err.println("Failed: Special practice booking constraint violation");
            System.err.println("IsSpecialPractice: " + newAssignment.getTask().isSpecialPractice() + 
                             ", Day: " + newAssignment.getSlot().getDay() + 
                             ", Time: " + newAssignment.getSlot().getSlotStartTime());
             */
            return false;
        }
        
        if (!specialGamePracticeBookingConstraint(newAssignment)) {
            /** 
            System.err.println("Failed: Special game practice booking constraint violation");
            System.err.println("Task ID: " + newAssignment.getTask().getIdentifier());
            */
            return false;
        }

        if (!notCompatibleConstraint(newAssignment)) {
            /** 
            System.err.println("Failed: Not compatible constraint violation");
            System.err.println("Task: " + newAssignment.getTask().getIdentifier());
            */
            return false;
        }

        //System.out.println("✓ All constraints passed successfully!");
        return true;
    }

    private boolean maxConstraint(Slot slot) {
        //System.out.println("\n[Checking Max Constraint]");
        //System.out.println("Current count: " + slot.getCurrentCount());
        //System.out.println("Maximum allowed: " + slot.getMax());
        boolean result = slot.getCurrentCount() <= slot.getMax();
        //System.out.println("Result: " + (result ? "PASS" : "FAIL"));
        return result;
    }

    private boolean noOverlappingPracticesAndGames(Assignment newAssignment) {
        /** 
        System.out.println("\n[Checking Overlapping Practices/Games]");
        System.out.println("New task: " + newAssignment.getTask().getIdentifier() + 
                          " (isGame=" + newAssignment.getTask().getIsGame() + ")");
        */
        
        Task newTask = newAssignment.getTask();
        Slot slot = newAssignment.getSlot();
        String newId = newTask.getIdentifier();
        
        //System.out.println("Checking against " + slot.getAssignedTasks().size() + " existing tasks");
        
        for (Task existingTask : slot.getAssignedTasks()) {
            //System.out.println("Comparing with: " + existingTask.getIdentifier() + 
            //                 " (isGame=" + existingTask.getIsGame() + ")");
            
            if (newTask.getIsGame() != existingTask.getIsGame()) {
                String existingId = existingTask.getIdentifier();
                if (newTask.getIsGame()) {
                    if (newId.equals(existingId.substring(0, existingId.length() - 6))) {
                        //System.out.println("Result: FAIL - Game/practice overlap found");
                        return false;
                    }
                } else {
                    if (existingId.equals(newId.substring(0, newId.length() - 6))) {
                        //System.out.println("Result: FAIL - Practice/game overlap found");
                        return false;
                    }
                }
            }
        }
        //System.out.println("Result: PASS");
        return true;
    }
    
    private boolean eveningDivisionConstraint(Assignment assignment) {
        //System.out.println("\n[Checking Evening Division Constraint]");
        //System.out.println("Division: " + assignment.getTask().getDivision());
        //System.out.println("Time: " + assignment.getSlot().getSlotStartTime());
        
        if (assignment.getTask().getDivision().startsWith("DIV 9")) {
            boolean result = assignment.getSlot().getSlotStartTime() == 18.0;
            //System.out.println("Result: " + (result ? "PASS" : "FAIL"));
            return result;
        }
        //System.out.println("Result: PASS (not DIV 9)");
        return true;
    }

    private boolean nonOverlappingTimeForCertainLevels(Slot slot) {
        //System.out.println("\n[Checking U15-19 Overlap Constraint]");
        //System.out.println("Current U15-19 count in slot: " + slot.getU1519());
        boolean result = slot.getU1519() <= 1;
        //System.out.println("Result: " + (result ? "PASS" : "FAIL"));
        return result;
    }

    private boolean notCompatibleConstraint(Assignment newAssignment) {
        //System.out.println("\n[Checking Not Compatible Constraint]");
        //System.out.println("New task: " + newAssignment.getTask().getIdentifier());
        
        Task newTask = newAssignment.getTask();
        Slot slot = newAssignment.getSlot();
        
        //System.out.println("Checking against " + slot.getAssignedTasks().size() + " existing tasks");
        
        for (Task existingTask : slot.getAssignedTasks()) {
            //System.out.println("Comparing with: " + existingTask.getIdentifier());
            if (!newTask.equals(existingTask) && newTask.isNotCompatibleWith(existingTask)) {
                //System.out.println("Result: FAIL - Found incompatible task");
                return false;
            }
        }
        //System.out.println("Result: PASS");
        return true;
    }

    private boolean specialPracticeBookingConstraint(Assignment assignment) {
        //System.out.println("\n[Checking Special Practice Booking Constraint]");
        //System.out.println("Is special practice: " + assignment.getTask().isSpecialPractice());
        //System.out.println("Day: " + assignment.getSlot().getDay());
        //System.out.println("Time: " + assignment.getSlot().getSlotStartTime());
        
        if (assignment.getTask().isSpecialPractice()) {
            Slot slot = assignment.getSlot();
            boolean result = slot.getDay().matches("TU|TH") && slot.getSlotStartTime() == 18.0;
            //System.out.println("Result: " + (result ? "PASS" : "FAIL"));
            return result;
        }
        //System.out.println("Result: PASS (not special practice)");
        return true;
    }

    private boolean specialGamePracticeBookingConstraint(Assignment newAssignment) {
        /* 
        System.out.println("\n[Checking Special Game Practice Booking Constraint]");
        System.out.println("Task ID: " + newAssignment.getTask().getIdentifier());
        */
        
        Task newTask = newAssignment.getTask();
        String newTaskId = newTask.getIdentifier();
        String newTeamType = "";
        
        if (newTaskId.startsWith("CMSA U12T1S")) newTeamType = "U12T1S";
        else if (newTaskId.startsWith("CMSA U12T1")) newTeamType = "U12T1";
        else if (newTaskId.startsWith("CMSA U13T1S")) newTeamType = "U13T1S";
        else if (newTaskId.startsWith("CMSA U13T1")) newTeamType = "U13T1";
        
        //System.out.println("New team type: " + newTeamType);
        
        if (!newTeamType.isEmpty()) {
            //System.out.println("Checking special team constraints...");
            for (Task existingTask : newAssignment.getSlot().getAssignedTasks()) {
                //System.out.println("Comparing with: " + existingTask.getIdentifier());
                String existingId = existingTask.getIdentifier();
                String existingTeamType = "";
                
                if (existingId.startsWith("CMSA U12T1S")) existingTeamType = "U12T1S";
                else if (existingId.startsWith("CMSA U12T1")) existingTeamType = "U12T1";
                else if (existingId.startsWith("CMSA U13T1S")) existingTeamType = "U13T1S";
                else if (existingId.startsWith("CMSA U13T1")) existingTeamType = "U13T1";
                
                //System.out.println("Existing team type: " + existingTeamType);
                
                if ((newTeamType.equals("U12T1") && existingTeamType.equals("U12T1S")) ||
                    (newTeamType.equals("U12T1S") && existingTeamType.equals("U12T1")) ||
                    (newTeamType.equals("U13T1") && existingTeamType.equals("U13T1S")) ||
                    (newTeamType.equals("U13T1S") && existingTeamType.equals("U13T1"))) {
                    //System.out.println("Result: FAIL - Incompatible special team types");
                    return false;
                }
            }
        }
        //System.out.println("Result: PASS");
        return true;
    }

    private boolean noGamesOnTuesdayMeeting(Assignment assignment) {
        /* 
        System.out.println("\n[Checking Tuesday Meeting Constraint]");
        System.out.println("Is game: " + assignment.getTask().getIsGame());
        System.out.println("Day: " + assignment.getSlot().getDay());
        System.out.println("Time: " + assignment.getSlot().getSlotStartTime());
        */

        if (assignment.getTask().getIsGame()) {
            Slot slot = assignment.getSlot();
            boolean result = !(slot.getDay().equals("TU") && slot.getSlotStartTime() == 11.0);
            //System.out.println("Result: " + (result ? "PASS" : "FAIL"));
            return result;
        }
        //System.out.println("Result: PASS (not a game)");
        return true;
    }
}