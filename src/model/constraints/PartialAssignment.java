package model.constraints;

public class PartialAssignment extends Constraint {
    private String day;
    private String time;
    private String timeSlotId;
    private String taskId; // For Game/Practice

    public PartialAssignment(String identifier, String day, String time) {
        super("Partial");
        this.day = day;
        this.time = time;
        this.timeSlotId = day + ", " + time;
        this.taskId = identifier;
    }

    public String getTimeSlotId(){
        return this.timeSlotId;
    }
    
    public String getDay(){
        return this.day;
    }
    
    public String getTime(){
        return this.time;
    }

    public String getTaskIdentifier(){
        return this.taskId;
    }

    @Override
    public String toString() {
        return String.format("Partial Assignment:[Team =%s, Time: %s]", taskId, timeSlotId);
    }
}
