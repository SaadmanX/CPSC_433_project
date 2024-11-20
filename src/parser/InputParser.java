package parser;
import java.io.*;
import java.util.*;

import model.Assignment;
import model.Constraint;
import model.GameSlot;
import model.PracticeSlot;
import model.Preference;

public class InputParser {
    private Map<String, List<String>> sections;

    public InputParser() {
        sections = new LinkedHashMap<>(); // Preserve order of sections
    }

    public void parseFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String currentSection = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // Skip blank lines

                // Check for section headers
                if (line.endsWith(":")) {
                    currentSection = line;
                    sections.putIfAbsent(currentSection, new ArrayList<>());
                } else if (currentSection != null) {
                    sections.get(currentSection).add(line);
                }
            }
        }
    }

    public String parseName() {
        List<String> lines = sections.get("Name:");
        return (lines != null && !lines.isEmpty()) ? lines.get(0).trim() : null;
    }

    public List<GameSlot> parseGameSlots() {
        List<GameSlot> gameSlots = new ArrayList<>();
        List<String> lines = sections.get("Game slots:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String day = parts[0].trim();
                    String startTime = parts[1].trim();
                    int gameMax = Integer.parseInt(parts[2].trim());
                    int gameMin = Integer.parseInt(parts[3].trim());
                    gameSlots.add(new GameSlot(day, startTime, gameMax, gameMin));
                }
            }
        }
        return gameSlots;
    }

    public List<PracticeSlot> parsePracticeSlots() {
        List<PracticeSlot> practiceSlots = new ArrayList<>();
        List<String> lines = sections.get("Practice slots:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String day = parts[0].trim();
                    String startTime = parts[1].trim();
                    int practiceMax = Integer.parseInt(parts[2].trim());
                    int practiceMin = Integer.parseInt(parts[3].trim());
                    practiceSlots.add(new PracticeSlot(day, startTime, practiceMax, practiceMin));
                }
            }
        }
        return practiceSlots;
    }

    public List<String> parseGames() {
        return sections.getOrDefault("Games:", new ArrayList<>());
    }

    public List<String> parsePractices() {
        return sections.getOrDefault("Practices:", new ArrayList<>());
    }

    public List<Constraint> parseNotCompatible() {
        List<Constraint> constraints = new ArrayList<>();
        List<String> lines = sections.get("Not compatible:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    constraints.add(new Constraint("NotCompatible", parts[0].trim(), parts[1].trim()));
                }
            }
        }
        return constraints;
    }

    public List<Constraint> parsePairs() {
        List<Constraint> constraints = new ArrayList<>();
        List<String> lines = sections.get("Pair:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    constraints.add(new Constraint("Pair", parts[0].trim(), parts[1].trim()));
                }
            }
        }
        return constraints;
    }

    public List<Preference> parsePreferences() {
        List<Preference> preferences = new ArrayList<>();
        List<String> lines = sections.get("Preferences:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String day = parts[0].trim();
                    String time = parts[1].trim();
                    String identifier = parts[2].trim();
                    int value = Integer.parseInt(parts[3].trim());
                    preferences.add(new Preference(day, time, identifier, value));
                }
            }
        }
        return preferences;
    }

    public List<Assignment> parsePartialAssignments() {
        List<Assignment> assignments = new ArrayList<>();
        List<String> lines = sections.get("Partial assignments:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    assignments.add(new Assignment(parts[0].trim(), parts[1].trim(), parts[2].trim()));
                }
            }
        }
        return assignments;
    }
}
