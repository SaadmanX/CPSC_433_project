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
        this.slotStartTime = convertTimeToDouble(startTime);
    }

    public Slot(Slot anotherSlot){
        this.day = anotherSlot.day;
        this.startTime = anotherSlot.startTime;
        this.identifier = anotherSlot.identifier;
        this.max = anotherSlot.max;
        this.min = anotherSlot.min;
        this.forGame = anotherSlot.forGame;
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

    public void addU1519() {
        this.u1519 += 1;
    }

    public void delU1519() {
        this.u1519 -= 1;
    }

    public int getU1519() {
        return this.u1519;
    }

    // returns a double value such that "9:30" -> 9.5
    private double convertTimeToDouble(String timeStr) {
        return Double.parseDouble(timeStr.substring(0, timeStr.indexOf(':'))) + 
                (timeStr.charAt(timeStr.length() - 2) == '3' ? 0.5 : 0.0);
    }

    public double getSlotStartTime() {
        return this.slotStartTime;
    }
    
    public int getMax() {
        return this.max;
    }

    public void setCurrentCount(int count){
        this.currentCount = count;
    }

    public void addAssignedTask(Task task){
        taskList.add(task);
    }

    public List<Task> getAssignedTasks(){
        return this.taskList;
    }

    public int getCurrentCount(){
        return this.currentCount;
    }

    public int getMin() {
        return this.min;
    }

    public String getId(){
        return this.identifier;
    }

    @Override
    public String toString() {
        return String.format("TimeSlot[day=%s, startTime=%s, max=%d, min=%d]", day, startTime, max, min);
    }
}
