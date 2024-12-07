package model;

import java.util.*;

import model.slots.*;
import model.task.Task;

public class SearchState {
    private List<Assignment> assignments; // Maps game/practice to slot
    private List<Task> remainingTasks;
    private List<Slot> availableSlots;
    private int penalty;
    

    public SearchState(List<Assignment> assignments, List<Task> remainingTasks, List<Slot> availableSlots, int penalty) {
        //create deep copy in order not to mutate/share state
        this.assignments = new ArrayList<>(assignments);
        this.remainingTasks = new ArrayList<>(remainingTasks);
        this.availableSlots = new ArrayList<>(availableSlots);
        this.penalty = penalty;
    }

    public SearchState(SearchState another){
        this.assignments = new ArrayList<>(another.assignments);
        this.remainingTasks = new ArrayList<>(another.remainingTasks);
        this.availableSlots = new ArrayList<>(another.availableSlots);
        this.penalty = another.penalty;
    }

    public void setAssignments(List<Assignment> assignments){
        this.assignments = assignments;
    }
    
    public void setRemainingTask(List<Task> tasks) {
        List<Task> orderedTasks = new ArrayList<>();
        
        // First add tasks with division >= 9
        for (Task task : tasks) {
            try {
                int divNum = Integer.parseInt(task.getDivision());
                if (divNum >= 9) {
                    orderedTasks.add(task);
                }
            } catch (NumberFormatException e) {
                continue;
            }
        }
        
        // Then add remaining tasks
        for (Task task : tasks) {
            try {
                int divNum = Integer.parseInt(task.getDivision());
                if (divNum < 9) {
                    orderedTasks.add(task);
                }
            } catch (NumberFormatException e) {
                // Add tasks with non-numeric divisions at the end
                orderedTasks.add(task);
            }
        }
        
        this.remainingTasks = orderedTasks;
    }
        
    public void removeTask(Task task) {
        remainingTasks.removeIf(task2 -> task2.getIdentifier().equals(task.getIdentifier()));
    }
    

    public void setRemainingSlots(List<Slot> slots){
        this.availableSlots = slots;
    }

    public void updateRemainingSlots(Slot slot) {   
        availableSlots.removeIf(s -> (s.forGame() == slot.forGame() && slot.getId().equals(s.getId())));

        //Add in the newest clone if the max hasn't reached
        if (slot.getMax() > slot.getCurrentCount()){
            //System.out.println("WAIT BUT THE CURRENT COUNT IS: " + slot.getCurrentCount());
            availableSlots.add(slot);
        }
    }
    
    
    public List<Assignment> getAssignments() {
        return assignments;
    }

    public SearchState clone() {
        List<Slot> clonedSlots = new ArrayList<>();
        for (Slot slot : this.availableSlots) {
            clonedSlots.add(slot.clone());
        }
    
        return new SearchState(
            new ArrayList<>(this.assignments),
            new ArrayList<>(this.remainingTasks),
            clonedSlots,
            this.penalty
        );
    }
    

    public List<Task> getRemainingTask(){
        return remainingTasks;
    }

    public List<Slot> getAvailableSlots(){
        return availableSlots;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }


    public void printState(){
        System.out.println("Assignment: ");
        for (int a = 0; a < assignments.size(); ++a){
            System.out.println(assignments.get(a));
        }
        System.out.println("Remaining Tasks: ");
        for (int b = 0; b < getRemainingTask().size(); b++){
            System.out.println(getRemainingTask().get(b).toString());
        }

        System.out.println("Remaining Slots:");

        for (int c = 0; c < getAvailableSlots().size(); c++){
            if (getAvailableSlots().get(c).forGame()){
                System.out.println("---For Game--- " + getAvailableSlots().get(c));
            } else {
                System.out.println("---For Practices--- " + getAvailableSlots().get(c));
            }
            
        }

        System.out.println("Penalty: " + penalty);
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        Task task = assignment.getTask();
        Slot slot = assignment.getSlot();
        
        task.setCurrentAssign(slot);
        slot.addAssignedTask(task);
        slot.setCurrentCount(slot.getCurrentCount() + 1);
    }
}
