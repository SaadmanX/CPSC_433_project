package parser;
import java.io.*;
import java.util.*;

import model.constraints.Constraint;
import model.constraints.NotCompatible;
import model.constraints.Pair;
import model.constraints.PartialAssignment;
import model.constraints.Preference;
import model.constraints.Unwanted;
import model.slots.Slot;
import model.task.Game;
import model.task.Practice;
import model.task.Task;

public class InputParser {
    private Map<String, List<String>> sections;
    private ArrayList<Task> allTasks = new ArrayList<>();
    private ArrayList<Slot> allSlots = new ArrayList<>();

    public ArrayList<Task> getAllTasks() {
        return allTasks;
    }

    public ArrayList<Slot> getAllSlots() {
        return allSlots;
    }

    public InputParser() {
        sections = new LinkedHashMap<>(); // Preserve order of sections
    }    

    public void parseFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String currentSection = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

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

    public List<Slot> parseGameSlots() {
        List<Slot> gameSlots = new ArrayList<>();
        List<String> lines = sections.get("Game slots:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                if (parts.length == 4) {
                    String day = parts[0].trim();
                    String startTime = parts[1].trim();
                    int gameMax = Integer.parseInt(parts[2].trim());
                    int gameMin = Integer.parseInt(parts[3].trim());

                    if (!(day.equals("TU") && startTime.equals("11:00"))) {
                        Slot gs = new Slot(day, startTime, gameMax, gameMin, true);
                        gameSlots.add(gs);
                        allSlots.add(gs);
                    }
                }
            }
        }
        return gameSlots;
    }

    public List<Slot> parsePracticeSlots() {
        List<Slot> practiceSlots = new ArrayList<>();
        List<String> lines = sections.get("Practice slots:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                if (parts.length == 4) {
                    String day = parts[0].trim();
                    String startTime = parts[1].trim();
                    int practiceMax = Integer.parseInt(parts[2].trim());
                    int practiceMin = Integer.parseInt(parts[3].trim());
                    Slot ps = new Slot(day, startTime, practiceMax, practiceMin, false);
                    practiceSlots.add(ps);
                    allSlots.add(ps);
                }
            }
        }
        return practiceSlots;
    }

    public List<Game> parseGames() {
        List<Game> games = new ArrayList<>();
        List<String> lines = sections.get("Games:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                String identifier = parts[0].trim().replaceAll("\\s+", " ");
                Game newGame = new Game(identifier);
                games.add(newGame);
                allTasks.add(newGame);
            }
        }
        return games;
    }

    public List<Practice> parsePractices() {
        List<Practice> practices = new ArrayList<>();
        List<String> lines = sections.get("Practices:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                String identifier = parts[0].trim().replaceAll("\\s+", " ");
                Practice newP = new Practice(identifier);
                practices.add(newP);
                allTasks.add(newP);
            }
        }
        return practices;
    }

    public List<Constraint> parseNotCompatible() {
        List<Constraint> constraints = new ArrayList<>();
        List<String> lines = sections.get("Not compatible:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                String id1 = parts[0].trim().replaceAll("\\s+", " ");
                String id2 = parts[1].trim().replaceAll("\\s+", " ");
                constraints.add(new NotCompatible(id1, id2));
            }
        }
        return constraints;
    }

    public List<Pair> parsePairs() {
        List<Pair> constraints = new ArrayList<>();
        List<String> lines = sections.get("Pair:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                String id1 = parts[0].trim().replaceAll("\\s+", " ");
                String id2 = parts[1].trim().replaceAll("\\s+", " ");
                constraints.add(new Pair(id1, id2));
            }
        }
        return constraints;
    }

    public List<Preference> parsePreferences() {
        List<Preference> constraints = new ArrayList<>();
        List<String> lines = sections.get("Preferences:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                if (parts.length == 4) {
                    String day = parts[0].trim();
                    String time = parts[1].trim();
                    String identifier = parts[2].trim().replaceAll("\\s+", " ");
                    int value = Integer.parseInt(parts[3].trim());
                    constraints.add(new Preference(day, time, identifier, value));
                }
            }
        }
        return constraints;
    }

    public List<Unwanted> parseUnwanted() {
        List<Unwanted> constraints = new ArrayList<>();
        List<String> lines = sections.get("Unwanted:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                if (parts.length == 3) {
                    String identifier = parts[0].trim().replaceAll("\\s+", " ");
                    String day = parts[1].trim();
                    String time = parts[2].trim();
                    constraints.add(new Unwanted(identifier, day, time));
                }
            }
        }
        return constraints;
    }

    public List<PartialAssignment> parsePartialAssignments() {
        List<PartialAssignment> constraints = new ArrayList<>();
        List<String> lines = sections.get("Partial assignments:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                if (parts.length == 3) {
                    String identifier = parts[0].trim().replaceAll("\\s+", " ");
                    String day = parts[1].trim();
                    String time = parts[2].trim();
                    constraints.add(new PartialAssignment(identifier, day, time));
                }
            }
        }
        return constraints;
    }
}