package model.constraints;

public class Unwanted extends Constraint {
    private String day;
    private String time;
    private String timeSlotId;
    private String taskId; // For Game/Practice

    public Unwanted(String identifier, String day, String time) {
        super("Unwanted");
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
        return String.format("Unwanted:[Team =%s, Time: %s]", taskId, timeSlotId);
    }

}
