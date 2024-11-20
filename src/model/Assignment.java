package model;
public class Assignment {
    private String identifier;
    private String day;
    private String time;

    public Assignment(String identifier, String day, String time) {
        this.identifier = identifier;
        this.day = day;
        this.time = time;
    }


    @Override
    public String toString() {
        return String.format("Assignment[identifier=%s, day=%s, time=%s]", identifier, day, time);
    }
}
