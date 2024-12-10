package model;

import java.util.*;

import model.slots.*;
import model.task.Task;

public class SearchState {
    private List<Assignment> assignments; // Maps game/practice to slot
    private List<Task> remainingTasks;
    private List<Slot> availableSlots; 
    private int penalty = 0;

    //These values are cumulative of all assignments in the SearchState
    private int minGameFillPenalty; //MAX value of min, deducted or updated with newest assignment
    private int prefPenalty; //Same thing, MAX VALUE of preferences, updated with newest assignment
    private int pairPenalty;
    private int secDiffPenalty;
    private int minPracticeFillPenalty; 
    
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
        this.pairPenalty = another.pairPenalty;
        this.prefPenalty = another.prefPenalty;
        this.minGameFillPenalty = another.minGameFillPenalty;
        this.minPracticeFillPenalty = another.minPracticeFillPenalty;
        this.secDiffPenalty = another.secDiffPenalty;
    }

    public void setAssignments(List<Assignment> assignments){
        this.assignments = assignments;
    }
    
    public void setRemainingTask(List<Task> tasks) {
        tasks.sort(new TaskComparator());;
        this.remainingTasks = tasks;
    }

    public boolean compareSearchState(SearchState anotherState){

        AssignmentComparator assignmentComparator = new AssignmentComparator();

        //Sort both assignment tasks
        this.assignments.sort(assignmentComparator);
        anotherState.assignments.sort(assignmentComparator);

        List<Assignment> otherAssignments = anotherState.getAssignments();
        if (otherAssignments.size() != getAssignments().size()){
            return false;
        } 

        for (int i = 0; i < getAssignments().size(); i++){
           if (!assignments.get(i).getTask().getIdentifier().equals(otherAssignments.get(i).getTask().getIdentifier()))return false;
           if (!assignments.get(i).getSlot().getId().equals(otherAssignments.get(i).getSlot().getId()))return false;
        }

        return true;
    }
        
    public void removeTask(Task task) {
        remainingTasks.removeIf(task2 -> task2.getIdentifier().equals(task.getIdentifier()));
    }
    

    public void setRemainingSlots(List<Slot> slots){
        this.availableSlots = slots;
    }
    
    public int countAssignableTasks(Slot slot) {
        return (int) remainingTasks.stream()
            .filter(task -> !task.isUnwantedSlot(slot) && task.getIsGame() == slot.forGame())
            .count();
    }

    public boolean isTaskAssigned(Task task) {
        return assignments.stream().anyMatch(assignment -> assignment.getTask().getIdentifier().equals(task.getIdentifier()));
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
    
        return new SearchState(this);
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

    public void updatePenalty(){
        this.penalty = pairPenalty + prefPenalty + minGameFillPenalty + minPracticeFillPenalty + secDiffPenalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    public void setMinGameFillPenalty(int penalty){
        this.minGameFillPenalty = penalty;
    }

    public void setMinPracticeFillPenalty(int penaly){
        this.minPracticeFillPenalty = penaly;
    }

    public int getMinPracticeFillPenalty(){
        return this.minPracticeFillPenalty;
    }

    public void setPrefPenalty(int penalty){
        this.prefPenalty = penalty;
    }

    public void setPairPenalty(int penalty){
        this.pairPenalty = penalty;
    }

    public void setSecDiffPenalty(int penalty){
        this.secDiffPenalty = penalty;
    }

    public int getMinGameFillPenalty(){
        return minGameFillPenalty;
    }

    public int getPrefPenalty(){
        return prefPenalty;
    }

    public int getPairPenalty(){
        return pairPenalty;
    }

    public int getSecDiffPenalty(){
        return secDiffPenalty;
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

        System.out.println("Pairing: " + pairPenalty);
        System.out.println("Penalty: " + penalty);
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        Task task = assignment.getTask();
        Slot slot = assignment.getSlot();
        
        task.setCurrentAssign(slot);
        slot.addAssignedTask(task);
        slot.setCurrentCount(slot.getCurrentCount() + 1);
        slot.addTaskToTierList(task);
    }
}