package model;

import java.util.Comparator;

import model.task.Task;

public class AssignmentComparator implements Comparator<Assignment>{

    @Override
    public int compare(Assignment assignment1, Assignment assignment2) {
        Task t1 = assignment1.getTask();

        Task t2 = assignment2.getTask();

        return t1.getIdentifier().compareTo(t2.getIdentifier());
    }
    
}
