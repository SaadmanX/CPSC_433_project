package model.slots;

import java.util.ArrayList;
import java.util.List;

import model.task.Task;

public class Slot {
    private String day;
    private String startTime;
    private double slotStartTime;    // start time + time frame
    private String identifier;
    private int max;
    private int min;
    private boolean forGame;
    private int currentCount = 0;
    private List<Task> taskList = new ArrayList<>();
    private int u1519 = 0;

    public Slot(String day, String startTime, int max, int min, boolean isGame) {
        this.day = day;
        this.startTime = startTime;
        this.identifier = day + ", " + startTime;
        this.max = max;
        this.min = min;
        this.forGame = isGame;
    }

    public Slot(Slot anotherSlot){
        this.day = anotherSlot.day;
        // this.currentCount = anotherSlot.currentCount;
        this.startTime = anotherSlot.startTime;
        this.slotStartTime = anotherSlot.slotStartTime;
        this.identifier = anotherSlot.identifier;
        this.max = anotherSlot.max;
        this.min = anotherSlot.min;
        this.forGame = anotherSlot.forGame;
        this.taskList = new ArrayList<>(anotherSlot.taskList);
        this.u1519 = anotherSlot.u1519;    
    }

    public boolean forGame(){
        return this.forGame;
    }

    public String getDay() {
        return this.day;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public double getSlotStartTime() {
        return this.slotStartTime;
    }
    
    public int getMax() {
        return this.max;
    }

    // public void setCurrentCount(int count){
    //     this.currentCount = count;
    // }

    public void addAssignedTask(Task task){
        taskList.add(task);
    }

    public List<Task> getAssignedTasks(){
        return this.taskList;
    }


    public void addU1519() {
        this.u1519 += 1;
    }

    public void delU1519() {
        this.u1519 -= 1;
    }

    public int getU1519() {
        return this.u1519;
    }

    public int getCurrentCount(){
        return taskList.size();
        // return this.currentCount;
    }

    public int getMin() {
        return this.min;
    }

    public String getId(){
        return this.identifier;
    }

    @Override
    public String toString() {
        return String.format("TimeSlot[day=%s, startTime=%s, max=%d, min=%d, curr=%d, isGame=%s]", day, startTime, max, min, getCurrentCount(), forGame);
    }
}
