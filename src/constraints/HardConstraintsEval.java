package constraints;

import java.util.List;

import model.Assignment;
import model.slots.Slot;
import model.task.Task;

//TODO: FIX TO REFLECT ALL DEEP COPIES
public class HardConstraintsEval {

    List<Task> allTasks;
    List<Slot> allSlots;

    public HardConstraintsEval(List<Task> allTasks, List<Slot> allSlots) {
        this.allTasks = allTasks;
        this.allSlots = allSlots;
    }

    public boolean validate(Assignment newAssignment) {

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

        if (!nonOverlappingTimeForCertainLevels(newAssignment.getSlot())) {
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

    // not the same slot. if new assignment is a game, look at the practice slots, and vice versa
    private boolean noOverlappingPracticesAndGames(Assignment newAssignment) {    
        Task newTask = newAssignment.getTask();
        Slot slot = newAssignment.getSlot();
        // System.out.println("=========================");
        // System.out.println(allSlots.size());
        // System.out.println(slot);
        Slot overlapslot = new Slot(slot);

        for (Slot s : allSlots) {
            if (!s.equals(slot)) {
                // System.out.println("line 85");
                if (s.getDay().equals(slot.getDay())) {
                    // System.out.println("line 87");
                    if (s.getStartTime().equals(slot.getStartTime())) {
                        // System.out.println("line 89");
                        if (s.forGame() != slot.forGame()) {
                            // System.out.println("line 91");
                            overlapslot = s;
                        }
                    }
                }
            }
        }

        if (overlapslot.equals(slot)) {
            System.out.println("overlap slot same as assigned slot");
            return true;
        }

        // System.out.println(overlapslot);

        String newId = newTask.getIdentifier(); 
        for (Task existingTask : overlapslot.getAssignedTasks()) {
            if (newTask.getIsGame() != existingTask.getIsGame()) {
                // System.out.println("line 109");
                String existingId = existingTask.getIdentifier();
                // System.out.println("new task: " + newId + " existing task: " + existingId);
                if (newTask.getIsGame()) {
                    // System.out.println("line 113");
                    if (newId.equals(existingId.substring(0, existingId.length() - 7))) {
                        // System.out.println("line 115");
                        return false;
                    }
                } else {
                    // System.out.println("line 119");
                    if (existingId.equals(newId.substring(0, newId.length() - 7))) {
                        // System.out.println("line 121");
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private boolean eveningDivisionConstraint(Assignment assignment) {    
        int div = Integer.parseInt(assignment.getTask().getDivision());
        if (div >= 9) {
            boolean result = assignment.getSlot().getSlotStartTime() >= 18.0;
            return result;
        }
        return true;
    }

    private boolean nonOverlappingTimeForCertainLevels(Slot slot) {
        boolean result = slot.getU1519() <= 1;
        return result;
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