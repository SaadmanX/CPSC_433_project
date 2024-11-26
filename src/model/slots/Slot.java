package model.slots;

public abstract class Slot {
    //TODO: Will need to convert startTime to int and timefrime, .0 and .5
    private String day;
    private String startTime;
    private String identifier;
    private int max;
    private int min;
    private boolean forGame;

    public Slot(String day, String startTime, int max, int min, boolean isGame) {
        this.day = day;
        this.startTime = startTime;
        this.identifier = day + ", " + startTime;
        this.max = max;
        this.min = min;
        this.forGame = isGame;
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

    public int getMax() {
        return this.max;
    }

    public void setMin(int min){
        this.min = min;
    }

    public void setMax(int max){
        this.max = max;
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
