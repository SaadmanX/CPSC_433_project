package search;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Assignment;
import model.SearchState;

public class OutputHandler {
    public static void writeToFile(SearchState state, String inputFileName) {
        if (state == null) return;
        
        String outputFileName = "output.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            writer.write(String.format("Eval-value: %d\n", state.getPenalty()));
            writer.write("Solution:\n");
            
            // Sort assignments for consistent output
            List<Assignment> sortedAssignments = new ArrayList<>(state.getAssignments());
            sortedAssignments.sort((a1, a2) -> {
                String id1 = a1.getTask().getIdentifier();
                String id2 = a2.getTask().getIdentifier();
                return id1.compareTo(id2);
            });
            
            for (Assignment assignment : sortedAssignments) {
                writer.write(String.format("%s: %s, %s\n",
                    assignment.getTask().getIdentifier(),
                    assignment.getSlot().getDay(),
                    assignment.getSlot().getStartTime()));
            }
        } catch (IOException e) {
            System.err.println("Error writing solution to file: " + e.getMessage());
        }
    }
}
