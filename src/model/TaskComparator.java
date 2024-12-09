package model;

import java.util.Comparator;

import model.task.Task;

public class TaskComparator implements Comparator<Task> {

    @Override
    public int compare(Task t1, Task t2) {

        // Prioritize tasks with division starting with 9
        boolean t1StartsWith9 = t1.getDivision().startsWith("9");
        boolean t2StartsWith9 = t2.getDivision().startsWith("9");

        if (t1StartsWith9 && !t2StartsWith9) {
            return -1; // t1 comes first
        } else if (!t1StartsWith9 && t2StartsWith9) {
            return 1; // t2 comes first
        }

        // Prioritize by the most unwanted slots
        int t1UnwantedSlotsCount = t1.getUnwantedSlots().size();
        int t2UnwantedSlotsCount = t2.getUnwantedSlots().size();

        if (t1UnwantedSlotsCount != t2UnwantedSlotsCount) {
            return Integer.compare(t2UnwantedSlotsCount, t1UnwantedSlotsCount); // More unwanted slots come first
        }

        // Prioritize by the most not compatible slots
        int t1NotCompatibleCount = t1.getNotCompatibleCount();
        int t2NotCompatibleCount = t2.getNotCompatibleCount();

        if (t1NotCompatibleCount != t2NotCompatibleCount) {
            return Integer.compare(t2NotCompatibleCount, t1NotCompatibleCount); //More notCompatible first
        }

        // Prioritize by preferences
        int t1PrefCount = t1.getSumPreferences();
        int t2PrefCount = t2.getSumPreferences();

        if (t1PrefCount != t2PrefCount) {
            return Integer.compare(t2PrefCount, t1PrefCount);   //More pref first
        }

        int t1PairingCount = t1.getPairs().size();
        int t2PairingCount = t2.getPairs().size();

        return Integer.compare(t2PairingCount, t1PairingCount); // More pairing first
    }
}
