package constraints;

import java.util.List;

import model.Assignment;

public class SoftConstraintsEval{
    //TODO: IMPLEMENT THIS

    List<Integer> multiplerList; //Order: minFill, pref, pair, secdiff
    List<Integer> penaltyList; //Order: gameMin, practiceMin, notPaird, section

    public SoftConstraintsEval(List<Integer> multiplierList, List<Integer> penaltyList){
        this.multiplerList = multiplierList;
        this.penaltyList = penaltyList;
    }

    public int calculatePenalty(List<Assignment> assignments) {
        int penalty = 0;
        
        return penalty;
    }
}
