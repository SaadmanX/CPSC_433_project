package model;
public class Constraint {
    private String type;
    private String item1;
    private String item2;

    public Constraint(String type, String item1, String item2) {
        this.type = type;
        this.item1 = item1;
        this.item2 = item2;
    }

    public String getType(){
        return this.type;
    }

    public String item1(){
        return this.item1;
    }

    public String item2(){
        return this.item2;
    }
 
    @Override
    public String toString() {
        return String.format("Constraint[type=%s, item1=%s, item2=%s]", type, item1, item2);
    }
}