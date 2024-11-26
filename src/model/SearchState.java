package model;

import java.util.*;

import model.*;
import model.slots.*;
import model.task.Game;
import model.task.Practice;

public class SearchState {
    private Map<String, String> assignments; // Maps game/practice to slot
    private List<Game> remainingGames;
    private List<Practice> remainingPractices;
    private List<GameSlot> availableGamesSlots;
    private List<PracticeSlot> availablePracticesSlots;
    private int penalty;

    public SearchState(Map<String, String> assignments, List<Game> remainingGames,
                       List<Practice> remainingPractices, List<GameSlot> availableGamesSlots, 
                       List<PracticeSlot> availablePracticesSlots, int penalty) {
        this.assignments = assignments;
        this.remainingGames = remainingGames;
        this.remainingPractices = remainingPractices;
        this.availableGamesSlots = availableGamesSlots;
        this.availablePracticesSlots = availablePracticesSlots;
        this.penalty = penalty;
    }

    public void setAssignments(Map<String, String> assignments){
        this.assignments = assignments;
    }

    public void setGames(List<Game> games){
        this.remainingGames = games;
    }

    public void setPractices(List<Practice> practices){
        this.remainingPractices = practices;
    }

    public void setAvailableGamesSlot(List<GameSlot> gameSlots){
        this.availableGamesSlots = gameSlots;
    }

    public void setAvailablePracticesSlot(List<PracticeSlot> practiceSlots){
        this.availablePracticesSlots = practiceSlots;
    }

    public Map<String, String> getAssignments() {
        return assignments;
    }

    public List<Game> getRemainingGames() {
        return remainingGames;
    }

    public List<Practice> getRemainingPractices() {
        return remainingPractices;
    }

    public List<GameSlot> getAvailableGamesSlots() {
        return availableGamesSlots;
    }

    public List<PracticeSlot> getAvailablePracticesSlots() {
        return availablePracticesSlots;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    @Override
    public String toString() {
        return String.format("State[assignments=%s, penalty=%d]", assignments, penalty);
    }
}
