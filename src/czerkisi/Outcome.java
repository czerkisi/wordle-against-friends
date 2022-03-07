package czerkisi;

import java.io.Serializable;
import java.util.ArrayList;

public class Outcome implements Serializable {
    private ArrayList<Result> outcomes = new ArrayList<>();
    private ArrayList<WordCondition> conditions = new ArrayList<>();
    private final String entry;
    private boolean validWord = true;
    private final boolean hostOutcome;

    Outcome(String entry, boolean hostOutcome){
        this.entry = entry;
        this.hostOutcome = hostOutcome;
    }

    public boolean isHostOutcome() {
        return hostOutcome;
    }

    public String getEntry(){
        return entry;
    }

    public boolean isValidWord() {
        return validWord;
    }

    public void setValidWord(boolean validWord) {
        this.validWord = validWord;
    }

    public void addOutcome(Result outcome){
        outcomes.add(outcome);
    }

    public ArrayList<Result> getOutcomes(){
        return outcomes;
    }

    public void addCondition(WordCondition wordCondition){
        conditions.add(wordCondition);
    }

    public ArrayList<WordCondition> getConditions(){
        return conditions;
    }
}
