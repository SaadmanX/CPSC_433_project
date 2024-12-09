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
    
    //For soft constraints
    public int maxMinGame = 0;
    public int maxMinPractice = 0;
    public int maxPairs = 0;
    public int maxPreferencesValue = 0;
    private boolean div9 = false;


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

                    //Commented this out because it can cause nullptr with large input
                    //if (!(day.equals("TU") && startTime.equals("11:00"))) {
                        Slot gs = new Slot(day, startTime, gameMax, gameMin, true);
                        gameSlots.add(gs);
                        allSlots.add(gs);
                        maxMinGame += gameMin;
                        //System.out.println(gs);
                    //}
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
                    maxMinPractice += practiceMin;
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

                // flag to check for div 9 presence. will be used to pull these up, as well as pull any evening slots up as well. if no evening slot is present, hard constraints will automatically take care of it later on. also, put the special practices at the very top as well. both of these are hard constraints and are at the top of the priority
                if (identifier.contains("DIV 9")) {
                    div9 = true;
                }

                Slot s = findSlotByDayAndTime("TU", "18:00", true);

                if (identifier.contains("CMSA U12T1") && !specialTasks.contains("CMSA U12T1S") && s != null) {
                    specialTasks.add("CMSA U12T1S");
                }

                if (identifier.contains("CMSA U13T1") && !specialTasks.contains("CMSA U13T1S") && s != null) {
                    specialTasks.add("CMSA U13T1S");
                }

                if (s != null) {
                    newGame.addUnwantedSlot(s);
                } 
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

        for (String s : specialTasks) {
            if (s.equals("CMSA U13T1S")) {
                Practice newP = new Practice("CMSA U13T1S");
                practices.add(newP);
                allTasks.add(newP);
            } 

            if (s.equals("CMSA U12T1S")) {
                Practice newP = new Practice("CMSA U12T1S");
                practices.add(newP);
                allTasks.add(newP);
            } 
        }

        for (Task p : practices) {
            if (p.getIdentifier().contains("CMSA U12T1 ") || p.getIdentifier().contains("CMSA U13T1 ")) {
                p.addUnwantedSlot(findSlotByDayAndTime("TU", "18:00", false));
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
            }
        }
    }

    
    public void parsePairs() {
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

                maxPairs += 1;

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

                    if (day.equals("TU") && time.equals("11:00") && task.getIsGame()) {
                        continue;
                    }

                    Slot slot = findSlotByDayAndTime(day, time, task.getIsGame());

                    if (slot == null) {
                        System.out.println("at inputparser, slot is null");
                        System.out.println(task);
                        System.out.println(line);
                    }

                    task.addPreference(slot, value);

                    maxPreferencesValue += value;
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
                }
            }
        }
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
        //return false;
        return constraints;
    }

    public void sortTasks() {
        if (allTasks.isEmpty()) return;
        
        // Create temporary lists to hold the sorted elements
        List<Task> sortedList = new ArrayList<>();
        List<Task> remainingTasks = new ArrayList<>(allTasks);
    
        // First handle special practices if there are any
        if (!specialTasks.isEmpty()) {
            // Find and move U12T1S and U13T1S practices to front
            Iterator<Task> specialIterator = remainingTasks.iterator();
            while (specialIterator.hasNext()) {
                Task task = specialIterator.next();
                if (task.getIdentifier().contains("CMSA U12T1S") || 
                    task.getIdentifier().contains("CMSA U13T1S")) {
                    sortedList.add(task);
                    specialIterator.remove();
                }
            }
        }
    
        // Then handle div9 tasks if div9 flag is true
        if (div9) {
            Iterator<Task> div9Iterator = remainingTasks.iterator();
            while (div9Iterator.hasNext()) {
                Task task = div9Iterator.next();
                if (task.getIdentifier().contains("DIV 9")) {
                    sortedList.add(task);
                    div9Iterator.remove();
                }
            }
        }
    
        // Add remaining tasks
        sortedList.addAll(remainingTasks);
    
        // Clear and refill allTasks with sorted list
        allTasks.clear();
        allTasks.addAll(sortedList);
    }

    public void sortSlots() {
        if (allSlots.isEmpty()) return;
        
        List<Slot> sortedList = new ArrayList<>();
        List<Slot> remainingSlots = new ArrayList<>(allSlots);
        
        // Handle special practice slots first if there are special tasks
        if (!specialTasks.isEmpty()) {
            Iterator<Slot> specialIterator = remainingSlots.iterator();
            while (specialIterator.hasNext()) {
                Slot slot = specialIterator.next();
                if (!slot.forGame() && // is a practice slot
                    slot.getDay().equals("TU") && 
                    slot.getStartTime().equals("18:00")) {
                    sortedList.add(slot);
                    specialIterator.remove();
                }
            }
        }
        
        // Handle evening slots for div9
        if (div9) {
            Iterator<Slot> div9Iterator = remainingSlots.iterator();
            while (div9Iterator.hasNext()) {
                Slot slot = div9Iterator.next();
                if (slot.getDay().equals("TU")) {
                    // Parse time to check if it's 18:00 or later
                    int hour = Integer.parseInt(slot.getStartTime().split(":")[0]);
                    if (hour >= 18) {
                        sortedList.add(slot);
                        div9Iterator.remove();
                    }
                }
            }
        }
        
        // Add remaining slots
        sortedList.addAll(remainingSlots);
        
        // Clear and refill allSlots with sorted list
        allSlots.clear();
        allSlots.addAll(sortedList);
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