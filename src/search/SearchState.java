package search;

import java.util.Map;

import model.GameSlot;

import java.util.List;

public class SearchState {
    private Map<String, String> assignments;
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

    // Getters, setters, and methods to manipulate state
}

