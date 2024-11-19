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

    @Override
    public String toString() {
        return String.format("PracticeSlot[day=%s, startTime=%s, practiceMax=%d, practiceMin=%d]", day, startTime, practiceMax, practiceMin);
    }
}