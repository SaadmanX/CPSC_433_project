package model;

import java.util.*;

import model.slots.*;
import model.task.Task;

public class SearchState {
    private List<Assignment> assignments; // Maps game/practice to slot
    private List<Task> remainingTasks;
    private List<Slot> availableSlots;
    private SearchState parent;
    private int penalty;

    public SearchState(List<Assignment> assignments, List<Task> remaningTasks, List<Slot> availableSlots, int penalty) {
        this.assignments = assignments;
        this.remainingTasks = remaningTasks;
        this.availableSlots = availableSlots;
        this.penalty = penalty;
    }

    public void setAssignments(List<Assignment> assignments){
        this.assignments = assignments;
    }

    public SearchState getParent(){
        return parent;
    }

    public void setRemainingTask(List<Task> tasks){
        this.remainingTasks = tasks;
    }

    public void setRemainingSlots(List<Slot> slots){
        this.availableSlots = slots;
    }

    public void updateRemainingSlots(Slot slot){
        for (Iterator<Slot> iterator = availableSlots.iterator(); iterator.hasNext(); ) {
            Slot cur = iterator.next();
            if (cur.getId().equals(slot.getId()) && cur.forGame() == slot.forGame()) {
                cur.setMax(slot.getMax() - 1);
                cur.setMin(slot.getMin() - 1);
                if (cur.getMax() == 0){
                    availableSlots.remove(cur);
                }
                break;
            }
        }
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public SearchState clone() {
        SearchState clone = new SearchState(assignments, remainingTasks, availableSlots, penalty);
        clone.assignments = new ArrayList<>(assignments);
        clone.availableSlots = availableSlots;
        clone.remainingTasks = remainingTasks;
        clone.penalty = penalty;
        return clone;
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

    public void setParent(SearchState parent){
        this.parent = parent;
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
