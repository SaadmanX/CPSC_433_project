package model;
public class Assignment {
    //I'll change this later to Task + Slot 

    private String identifier;
    private String day;
    private String time;

    public Assignment(String identifier, String day, String time) {
        this.identifier = identifier;
        this.day = day;
        this.time = time;
    }

    public String getDay(){
        return this.day;
    }

    public String getTime(){
        return this.time;
    }

    public String getId(){
        return this.identifier;
    }

    @Override
    public String toString() {
        return String.format("Assignment[identifier=%s, day=%s, time=%s]", identifier, day, time);
    }
}
