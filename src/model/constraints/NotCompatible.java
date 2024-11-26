package model.constraints;

public class NotCompatible extends Constraint {

    private String team1Id;
    private String team2Id;

    public NotCompatible(String team1Id, String team2Id){
        super("NotCompatible");
        this.team1Id = team1Id;
        this.team2Id = team2Id;
    }

    public String getTeam1Id(){
        return this.team1Id;
    }

    public String getTeam2Id(){
        return this.team2Id;
    }

    @Override
    public String toString() {
        return String.format("Not Compatible[Team 1=%s, Team 2=%s]", team1Id, team2Id);
    }
}
