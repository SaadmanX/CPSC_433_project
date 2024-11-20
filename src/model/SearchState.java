package model;

import java.util.*;

public class SearchState {
    private Map<String, String> assignments; // Maps game/practice to slot
    private List<String> remainingGames;
    private List<String> remainingPractices;
    private List<GameSlot> availableSlots;
    private int penalty;

    public SearchState(Map<String, String> assignments, List<String> remainingGames,
                       List<String> remainingPractices, List<GameSlot> availableSlots, int penalty) {
        this.assignments = assignments;
        this.remainingGames = remainingGames;
        this.remainingPractices = remainingPractices;
        this.availableSlots = availableSlots;
        this.penalty = penalty;
    }

    public Map<String, String> getAssignments() {
        return assignments;
    }

    public List<String> getRemainingGames() {
        return remainingGames;
    }

    public List<String> getRemainingPractices() {
        return remainingPractices;
    }

    public List<GameSlot> getAvailableSlots() {
        return availableSlots;
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
