package model;

import java.util.*;

import model.slots.*;
import model.task.Practice;
import model.task.Task;

public class SearchState {
    private List<Assignment> assignments; // Maps game/practice to slot
    private List<Task> remainingTasks;
    private List<GameSlot> availableGamesSlots;
    private List<PracticeSlot> availablePracticesSlots;
    private int penalty;

    public SearchState(List<Assignment> assignments, List<Task> remaningTasks, List<GameSlot> availableGamesSlots, 
                       List<PracticeSlot> availablePracticesSlots, int penalty) {
        this.assignments = assignments;
        this.remainingTasks = remaningTasks;
        this.availableGamesSlots = availableGamesSlots;
        this.availablePracticesSlots = availablePracticesSlots;
        this.penalty = penalty;
    }

    public void setAssignments(List<Assignment> assignments){
        this.assignments = assignments;
    }

    public void setRemainingTask(List<Task> tasks){
        this.remainingTasks = tasks;
    }

    public void setAvailableGamesSlot(List<GameSlot> gameSlots){
        this.availableGamesSlots = gameSlots;
    }

    public void setRemainingGamesSlots(Slot slot){
        for (Iterator<GameSlot> iterator = availableGamesSlots.iterator(); iterator.hasNext(); ) {
            GameSlot cur = iterator.next();
            if (cur.getId().equals(slot.getId())) {
                cur.setMax(slot.getMax() - 1);
                cur.setMin(slot.getMin() - 1);
                break;
            }
        }
    }

    public void setRemainingPracticesSlots(Slot slot){
        for (Iterator<PracticeSlot> iterator = availablePracticesSlots.iterator(); iterator.hasNext(); ) {
            PracticeSlot cur = iterator.next();
            if (cur.getId().equals(slot.getId())) {
                cur.setMax(slot.getMax() - 1);
                cur.setMin(slot.getMin() - 1);
                break;
            }
        }
    }

    public void setAvailablePracticesSlot(List<PracticeSlot> practiceSlots){
        this.availablePracticesSlots = practiceSlots;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public List<Task> getRemaininngTask(){
        return remainingTasks;
    }

    public List<GameSlot> getAvailableGamesSlots() {
        return availableGamesSlots;
    }

    public List<PracticeSlot> getAvailablePracticesSlots() {
        return availablePracticesSlots;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }
}
