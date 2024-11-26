package constraints;

import java.util.List;

import model.Assignment;

public class HardConstraintsEval {
    
    //TODO:Eval Hard Constraints
    private boolean maxMin(Assignment assignment){
        if (assignment.getSlot().getMax() == 0) return false;
        return true;
    }

    
    public boolean validate(List<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            maxMin(assignment);

        }

        return true; 
    }
}
