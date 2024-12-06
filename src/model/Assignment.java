package model;

import model.slots.Slot;
import model.task.Task;

public class Assignment {
    private Task task;
    private Slot slot;

    public Assignment(Task task, Slot slot) {
        this.task = task;
        this.slot = slot;
        if (task.getLevel().matches("U1[5-9]..")) {
            slot.addU1519();
        }
    }

    public Task getTask(){
        return this.task;
    }

    public Slot getSlot(){
        return this.slot;
    }

    @Override
    public String toString() {
        return String.format("Assignment[Task=%s, day=%s, time=%s]", task.getIdentifier(), slot.getDay(), slot.getStartTime());
    }
}
