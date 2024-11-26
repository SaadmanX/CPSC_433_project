package model.slots;

public class GameSlot extends Slot {
    
    public GameSlot(String day, String startTime, int gameMax, int gameMin) {
        super(day, startTime, gameMax, gameMin, true); 
    }
}
