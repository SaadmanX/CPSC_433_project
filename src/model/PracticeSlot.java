package model;
public class PracticeSlot {
    private String day;
    private String startTime;
    private int practiceMax;
    private int practiceMin;

    public PracticeSlot(String day, String startTime, int practiceMax, int practiceMin) {
        this.day = day;
        this.startTime = startTime;
        this.practiceMax = practiceMax;
        this.practiceMin = practiceMin;
    }

    public String getDay(){
        return this.day;
    }

    public String getStartTime(){
        return this.startTime;
    }

    public int practiceMax(){
        return this.practiceMax;
    }

    public int practiceMin(){
        return this.practiceMin;
    }

    @Override
    public String toString() {
        return String.format("PracticeSlot[day=%s, startTime=%s, practiceMax=%d, practiceMin=%d]", day, startTime, practiceMax, practiceMin);
    }
}