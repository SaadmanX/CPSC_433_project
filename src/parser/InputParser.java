package parser;
import java.io.*;
import java.util.*;

import model.constraints.PartialAssignment;
import model.slots.Slot;
import model.task.Game;
import model.task.Practice;
import model.task.Task;

public class InputParser {
    private Map<String, List<String>> sections;
    private ArrayList<Task> allTasks = new ArrayList<>();
    private ArrayList<Slot> allSlots = new ArrayList<>();
    public List<String> specialTasks = new ArrayList<>();

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

                        //System.out.println(gs);
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
                    //System.out.println(ps);
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
                if (identifier.contains("CMSA U12T1") || identifier.contains("CMSA U13T1")){
                    specialTasks.add(newGame.getIdentifier());
                }
                //System.out.println(newGame);
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

                //System.out.println(newP);
            }
        }
        return practices;
    }

    public void parseNotCompatible() {
        List<String> lines = sections.get("Not compatible:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                String id1 = parts[0].trim().replaceAll("\\s+", " ");
                String id2 = parts[1].trim().replaceAll("\\s+", " ");

                Task t1 = findTaskByIdentifier(id1);
                Task t2 = findTaskByIdentifier(id2);
                t1.addNotCompatible(id2);
                t2.addNotCompatible(id1);

                //constraints.add(new NotCompatible(id1, id2));
                //System.out.println(id1 + ", " + id2);
                
            }
        }
    }

    
    public void parsePairs() {
        //List<Pair> constraints = new ArrayList<>();
        List<String> lines = sections.get("Pair:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                String id1 = parts[0].trim().replaceAll("\\s+", " ");
                String id2 = parts[1].trim().replaceAll("\\s+", " ");
                //constraints.add(new Pair(id1, id2));

                Task t1 = findTaskByIdentifier(id1);
                Task t2 = findTaskByIdentifier(id2);

                t1.addPair(t2);
                t2.addPair(t1);

                //System.out.println(id1 + ", " + id2);
            }
        }
        //return constraints;
    }

    public void parsePreferences() {
        //List<Preference> constraints = new ArrayList<>();
        List<String> lines = sections.get("Preferences:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                if (parts.length == 4) {
                    String day = parts[0].trim();
                    String time = parts[1].trim();
                    String identifier = parts[2].trim().replaceAll("\\s+", " ");
                    int value = Integer.parseInt(parts[3].trim());
                    //constraints.add(new Preference(day, time, identifier, value));

                    Task task = findTaskByIdentifier(identifier);
                    Slot slot = findSlotByDayAndTime(day, time, task.getIsGame());

                    task.addPreference(slot, value);

                    //System.out.println(task + ", " + slot);
                }
            }
        }
        //return constraints;
    }

    public void parseUnwanted() {
        //List<Unwanted> constraints = new ArrayList<>();
        List<String> lines = sections.get("Unwanted:");

        if (lines != null) {
            for (String line : lines) {
                String[] parts = line.split("\\s*,\\s*");
                if (parts.length == 3) {
                    String identifier = parts[0].trim().replaceAll("\\s+", " ");
                    String day = parts[1].trim();
                    String time = parts[2].trim();
                    //constraints.add(new Unwanted(identifier, day, time));

                    Task task = findTaskByIdentifier(identifier);
                    Slot slot = findSlotByDayAndTime(day, time, task.getIsGame());

                    task.addUnwantedSlot(slot);
                    //System.out.println(task + ", " + slot);
                }
            }
        }
        //return constraints;
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
                    //System.out.println(task + ", " + slot);
                }
            }
        }
        //return false;
        return constraints;
    }

    private Task findTaskByIdentifier(String identifier) {
        return allTasks.stream().filter(task -> task.getIdentifier().equals(identifier)).findFirst().orElse(null);
    }

    private Slot findSlotByDayAndTime(String day, String time, boolean isGame) {
        return allSlots.stream()
                .filter(slot -> slot.getDay().equals(day) && slot.getStartTime().equals(time) && slot.forGame() == isGame)
                .findFirst()
                .orElse(null);
    }
}