package model;
public class Preference {
    private String day;
    private String time;
    private String identifier;
    private int value;

    public Preference(String day, String time, String identifier, int value) {
        this.day = day;
        this.time = time;
        this.identifier = identifier;
        this.value = value;
    }

    

    @Override
    public String toString() {
        return String.format("Preference[day=%s, time=%s, identifier=%s, value=%d]", day, time, identifier, value);
    }
}