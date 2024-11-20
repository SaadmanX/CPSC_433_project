package model;
public class GameSlot {
    private String day;
    private String startTime;
    private int gameMax;
    private int gameMin;

    public GameSlot(String day, String startTime, int gameMax, int gameMin) {
        this.day = day;
        this.startTime = startTime;
        this.gameMax = gameMax;
        this.gameMin = gameMin;
    }

    public String getDay(){
        return this.day;
    }

    public String getStartTime(){
        return this.startTime;
    }

    public int gameMax(){
        return this.gameMax;
    }

    public int gameMin(){
        return this.gameMin;
    }


    @Override
    public String toString() {
        return String.format("GameSlot[day=%s, startTime=%s, gameMax=%d, gameMin=%d]", day, startTime, gameMax, gameMin);
    }
}