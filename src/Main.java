import java.io.IOException;
import parser.InputParser;

public class Main {
    public static void main(String[] args) {
        String inputFileName = args[0];
        InputParser parser = new InputParser();

        try {
            parser.parseFile(inputFileName);
            //System.out.println("Name: " + parser.parseName());
            //System.out.println("\nGame Slots:");
            parser.parseGameSlots().forEach(System.out::println);
           // System.out.println("\nPractice Slots:");
            parser.parsePracticeSlots().forEach(System.out::println);
            //System.out.println("\nGames:");
            parser.parseGames().forEach(System.out::println);

            //System.out.println("\nPractices:");
            parser.parsePractices().forEach(System.out::println);

            //System.out.println("\nNot Compatible:");
            parser.parseNotCompatible().forEach(System.out::println);

            //System.out.println("\nPairs:");
            parser.parsePairs().forEach(System.out::println);

            //System.out.println("\nPreferences:");
            parser.parsePreferences().forEach(System.out::println);

            //System.out.println("\nPartial Assignments:");
            parser.parsePartialAssignments().forEach(System.out::println);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
