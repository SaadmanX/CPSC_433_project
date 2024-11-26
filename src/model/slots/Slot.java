package model.slots;

public abstract class Slot {
    //This is for day and time
    private String day;
    private String startTime;
    private String identifier;
    private int max;
    private int min;

    public Slot(String day, String startTime, int max, int min) {
        this.day = day;
        this.startTime = startTime;
        this.identifier = day + ", " + startTime;
        this.max = max;
        this.min = min;
    }

    public String getDay() {
        return this.day;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public int getMax() {
        return this.max;
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
