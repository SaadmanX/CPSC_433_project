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
        this.assignments = assignments;
        this.remainingTasks = remainingTasks;
        this.availableSlots = availableSlots;
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
        
    public void setRemainingSlots(List<Slot> slots){
        this.availableSlots = slots;
    }

    public void updateRemainingSlots(Slot slot) {
        List<Slot> slotsToUpdate = new ArrayList<>();
        
        for (Slot s: this.availableSlots) {
            if (s.getDay().equals(slot.getDay()) && s.getStartTime().equals(slot.getStartTime()) && s.forGame() == slot.forGame()) {
                s.setCurrentCount(s.getCurrentCount() + 1);
                if (s.getMax() == s.getCurrentCount()) {
                    slotsToUpdate.add(s);
                }
            }
        }
        
        availableSlots.removeAll(slotsToUpdate);
    }
    
    public List<Assignment> getAssignments() {
        return assignments;
    }

    public SearchState clone() {
        SearchState clonedState = new SearchState(this);
        
        return clonedState;
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
    }
}
