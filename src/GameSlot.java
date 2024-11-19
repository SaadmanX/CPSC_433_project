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

    @Override
    public String toString() {
        return String.format("GameSlot[day=%s, startTime=%s, gameMax=%d, gameMin=%d]", day, startTime, gameMax, gameMin);
    }
}