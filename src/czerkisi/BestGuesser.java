package czerkisi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BestGuesser {
    private List<String> allWords = new ArrayList<>();
    private List<String> validWords = new ArrayList<>();

    public BestGuesser(List<String> allWords){
        this.allWords.addAll(allWords);
        validWords.addAll(allWords);
    }

    public void addConditions(Collection<WordCondition> condition){
        condition.forEach(test -> validWords = validWords.stream()
                .filter(test::testCondition)
                .collect(Collectors.toList()));
    }

    public List<String> getValidWords(){
        return validWords;
    }
}
