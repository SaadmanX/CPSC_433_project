package constraints;

import model.SearchState;

public class HardConstraintsEval {

    public static boolean validate(SearchState state) {
        for (String name : state.getAssignments().keySet()) {
            String identifier = state.getAssignments().get(name);
            System.out.println("Identifier name: " + name + ", Value: " + identifier);
        }



        return true; 
    }
}
