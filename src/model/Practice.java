package model;
public class Practice {
    private String identifier;

    public Practice(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return String.format("Practice[identifier=%s]", identifier);
    }
}