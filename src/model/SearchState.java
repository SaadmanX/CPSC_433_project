package model;

import java.util.*;

import model.slots.*;
import model.task.Task;

public class SearchState {
    private List<Assignment> assignments; // Maps game/practice to slot
    private List<Task> remainingTasks;
    private List<Slot> availableSlots;
    private int penalty;

    public SearchState(List<Assignment> assignments, List<Task> remaningTasks, List<Slot> availableSlots, int penalty) {
        this.assignments = assignments;
        this.remainingTasks = remaningTasks;
        this.availableSlots = availableSlots;
        this.penalty = penalty;
    }

    public SearchState(SearchState another){
        this.assignments = another.assignments;
        this.remainingTasks = another.remainingTasks;
        this.availableSlots = another.availableSlots;
        this.penalty = another.penalty;
    }

    public void setAssignments(List<Assignment> assignments){
        this.assignments = assignments;
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
            //Ah, this needs to be clone as well in order to avoid concurrent update
            if (cur.getId().equals(slot.getId()) && cur.forGame() == slot.forGame()) {
                cur.setCurrentCount(cur.getCurrentCount() + 1);
                if (cur.getMax() == cur.getCurrentCount()){
                    iterator.remove();
                }
                break;
            }
        }
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

   
    public SearchState clone() {
        SearchState clonedState = new SearchState(this);
        clonedState.availableSlots = new ArrayList<>();
        for (Slot slot : this.availableSlots) {
            clonedState.availableSlots.add(new Slot(slot)); // Deep clone slots
        }
        
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
