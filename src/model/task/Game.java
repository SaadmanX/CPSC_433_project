package model.task;
public class Game extends Task{

    public Game(String identifier) {
        super(identifier, true);
        //makes it clear here of the date all of them are avialble for
        //so it can be easier to be looped through
        //and no need for hard constraints model
    }
}