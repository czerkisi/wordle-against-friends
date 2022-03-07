package czerkisi;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class WordGenerator {
    private String selectedWord;
    private double difficulty;
    private List<String> allWords;
    private List<String> guessable;

    WordGenerator(){
        fillLists();
        selectedWord = guessable.get((int) (Math.random()*guessable.size() + 1));
    }

    private void fillLists() {
        try {
            File guessableWordsList = new File("C:\\Users\\czerkisi\\IdeaProjects\\WordleAgainstFriends\\src\\czerkisi\\InternalFiles\\guessableWords.txt");
            File allWordsList = new File("C:\\Users\\czerkisi\\IdeaProjects\\WordleAgainstFriends\\src\\czerkisi\\InternalFiles\\allWords.txt");

            allWords = Files.lines(allWordsList.toPath()).collect(Collectors.toList());
            guessable = Files.lines(guessableWordsList.toPath()).collect(Collectors.toList());
        } catch (IOException e){
            System.out.println("error reading file");
            System.out.println(e.getLocalizedMessage());
            System.out.println(e.getMessage());
        }
    }

    public boolean verifyWord(String word){
        return allWords.contains(word);
    }

    public String getSelectedWord(){
        return selectedWord;
    }

    public List<String> getAllWords(){
        return allWords;
    }

    public static boolean containsChar(String word, char character){
        return word.contains(character+"");
    }


}
