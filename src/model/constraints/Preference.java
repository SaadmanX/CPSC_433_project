package model.constraints;

public class Preference extends Constraint{
    private String day;
    private String time;
    private String timeSlotId;
    private String taskId; // For Game/Practice
    private int penalty;

    public Preference(String day, String time, String identifier, int penalty) {
        super("Preference");
        this.day = day;
        this.time = time;
        this.timeSlotId = day + ", " + time;
        this.taskId = identifier;
        this.penalty = penalty;
    }

    public int penalty(){
        return this.penalty;
    }

    public String getTimeSlotId(){
        return this.timeSlotId;
    }
    
    public String getDay(){
        return this.day;
    }
    
    public int getPenalty(){
        return this.penalty;
    }

    public String getTime(){
        return this.time;
    }

    public String getTaskIdentifier(){
        return this.taskId;
    }

    @Override
    public String toString() {
        return String.format("Preference:[Team =%s, Time: %s, Penalty: %d]", taskId, timeSlotId, penalty);
    }
}