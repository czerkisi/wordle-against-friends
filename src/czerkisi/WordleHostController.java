package czerkisi;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

public class WordleHostController implements Initializable {
    private static final int NUMBER_OF_ROWS = 6;
    private static final int NUMBER_OF_COLUMNS = 5;
    private static final int INSETS = -5;
    private static final int CORNER_RADIUS = 5;
    private static final int TIME_TO_RESPOND = 45;
    private static final long TIME_UPDATE_PERIOD = 1000;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    @FXML
    private GridPane userGrid;
    @FXML
    private GridPane opponentGrid;
    @FXML
    private VBox mainBox;
    @FXML
    private Label statusLabel;
    @FXML
    private Label timeLabel;

    private Timeline timeline;
    private int timeRemaining;
    private final Stage remainingWordsStage = new Stage();
    private final TextArea remainingWordsField = new TextArea();
    private final Scene scene = new Scene(remainingWordsField);
    private final Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    private final ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
    private final ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

    private int columnNum;
    private int rowNum;
    private boolean myTurn;
    private boolean redemptionChance;
    private boolean continueGame;
    private boolean wentFirst;
    private WordGenerator generator;
    private BestGuesser guesser;
    private Alert a;
    private URL defaultURL;
    private ResourceBundle defaultResBundle;
    private static final ArrayList<Integer> USER_LETTERS_ORDER = new ArrayList<>(Arrays. asList(13, 1, 2, 3, 4, 5, 16, 19, 20, 15, 6, 17, 22, 21, 14, 7, 18, 23, 24, 12, 8, 9, 10, 11, 0, 25, 26, 27, 28, 29, 30));
    private static final ArrayList<Integer> OPPONENT_LETTERS_ORDER = new ArrayList<>(Arrays. asList(13, 1, 2, 3, 4, 5, 16, 19, 20, 15, 6, 17, 22, 21, 147, 18, 23, 24, 12, 8, 9, 10, 11, 0, 25, 26, 27, 28, 29));
    private int opponentIndex;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        defaultURL = url;
        defaultResBundle = resourceBundle;
        columnNum = 0;
        rowNum = 0;
        opponentIndex = 0;
        generator = new WordGenerator();
        System.out.println(generator.getSelectedWord());
        guesser = new BestGuesser(generator.getAllWords());
        continueGame = true;
        redemptionChance = false;

        resetLetters();

        setRemainingWordsField();

        startReadLoop();
    }

    private void startReadLoop() {
        Timeline loop = new Timeline();
        loop.setCycleCount(Timeline.INDEFINITE);
        loop.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        (EventHandler<ActionEvent>) event -> {
                            try {
                                Object obj = in.readObject();
                                if (obj != null){
                                    Outcome outcome = (Outcome) obj;
                                    nextRow(outcome);
                                }
                            } catch (SocketTimeoutException e){
                                System.out.println("no object ");
                            } catch (IOException e) {
                                loop.stop();
                                endGame(true, true);
                            } catch (ClassNotFoundException e){
                                error("class not found");
                            }
                        }));
        loop.playFromStart();
    }

    public void setOutput(Socket socket) throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
    }

    public void setInput(Socket socket) throws IOException {
        System.out.println("in made");
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println(in);
    }

    private void setRemainingWordsField() {
        remainingWordsField.setEditable(false);
        remainingWordsField.setWrapText(true);
        remainingWordsStage.setHeight(400);
        remainingWordsStage.setWidth(400);
        remainingWordsStage.setTitle("Remaining words");
        remainingWordsStage.setScene(scene);
        scene.setOnKeyReleased(action -> {
            if (action.getText().equals("=")){
                updateRemainingWordVisibility();
            }
        });
    }

    public void setGoesFirst(boolean goesFirst){
        wentFirst = goesFirst;
        if (goesFirst){
            myTurn();
            statusLabel.setText("You Go First!");
        } else {
            yourTurn();
        }
    }

    public void nextRow(Outcome outcome) {
        String entry = outcome.getEntry();
        if (generator.verifyWord(entry)) {
            for (int i = 0; i < entry.length(); i++) {
                columnNum = i;
                char selectedChar = entry.charAt(i);
                char correctChar = generator.getSelectedWord().charAt(i);
                if (selectedChar == correctChar) {
                    outcome.addOutcome(Result.EXACT);
                    //the char at the specified index must match the char at that index of the correct word
                    //outcome.addCondition(word -> word.charAt(columnNum) == correctChar);
                } else if (WordGenerator.containsChar(generator.getSelectedWord(), selectedChar)) {
                    outcome.addOutcome(Result.CONTAINS);
                    //the word must contain the specified char
                    //outcome.addCondition(word -> WordGenerator.containsChar(word, selectedChar));
                    //the word cannot have the char at the specified index
                    //outcome.addCondition(word -> word.charAt(columnNum) != selectedChar);
                } else {
                    outcome.addOutcome(Result.MISS);
                    //the word does not contain the character
                    //outcome.addCondition(word -> !word.contains(selectedChar+""));
                }
            }
        } else {
            outcome.setValidWord(false);
            for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                outcome.addOutcome(Result.MISS);
            }
        }
        setResults(outcome);
        try {
            out.writeObject(outcome);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getLocalizedMessage());
            error("IO Exception nextRow " + e.getMessage() + e.getLocalizedMessage());
        }
    }

    private void setResults(Outcome outcome) {
        System.out.println(outcome.getOutcomes());
        System.out.println(outcome.getEntry());
        System.out.println(outcome.isValidWord());
        boolean allMatch = outcome.getOutcomes()
                                  .stream()
                                  .allMatch(result -> result.equals(Result.EXACT));
        if (redemptionChance && allMatch){
            error("tie");
            //tie
        } else if (redemptionChance){
            //other user wins
            endGame(!myTurn);
            redemptionChance = false;
        }
        if (!outcome.isValidWord()){
            if (myTurn && !outcome.getEntry().isEmpty()){
                invalidWord(outcome.getEntry());
            }
        } else {
            updateFromOutcome(outcome);
            if (myTurn) {
                guesser.addConditions(outcome.getConditions());
                yourTurn();
                if (checkToEndGame()){
                    endGame(false);
                } else {
                    advanceRow();
                }
            } else {
                myTurn();
            }
        }
    }

    private boolean checkToEndGame(){
        return (rowNum + 1 == NUMBER_OF_ROWS && !redemptionChance);
    }

    private void updateFromOutcome(Outcome outcome) {
        if (outcome.isHostOutcome()) {
            for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                columnNum = i;
                if (outcome.getOutcomes().get(i).equals(Result.EXACT)) {
                    getCurrentLabel().setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
                } else if (outcome.getOutcomes().get(i).equals(Result.CONTAINS)) {
                    getCurrentLabel().setBackground(new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
                }
            }
        } else {
            for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                Label temp = (Label) opponentGrid.getChildren().get(OPPONENT_LETTERS_ORDER.get(opponentIndex));
                if (outcome.getOutcomes().get(i).equals(Result.EXACT)) {
                    temp.setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
                } else if (outcome.getOutcomes().get(i).equals(Result.CONTAINS)) {
                    temp.setBackground(new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
                }
                opponentIndex++;
            }
            columnNum = 0;
        }
        boolean allCorrect = outcome.getOutcomes().stream().allMatch(result -> result.equals(Result.EXACT));
        if ((allCorrect && myTurn && wentFirst) || (allCorrect && !myTurn && !wentFirst)){
            System.out.println("redemption");
            redemption();
        } else if (allCorrect){
            endGame(myTurn);
        }
    }

    private void endGame(boolean win){
        endGame(win, false);
    }

    private void redemption() {
        redemptionChance = true;
        if (myTurn){
            yourTurn();
        } else {
            myTurn();
        }
        statusLabel.setText("Redemption Chance!");
        System.out.println("redemption chance");
    }

    private void advanceRow(){
        columnNum = 0;
        rowNum++;
    }

    private void resetRow(){
        for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            columnNum = i;
            getCurrentLabel().setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
            getCurrentLabel().setText("");
        }
    }

    private void endGame(boolean win, boolean otherDisconnected) {
        continueGame = false;
        if (timeline != null) {
            timeline.getKeyFrames().clear();
        }
        a = new Alert(Alert.AlertType.CONFIRMATION,
                "", yes, no);
        a.setContentText("Would you like to play again?");
        timeLabel.setText("Time remaining: " + TIME_TO_RESPOND + "s");
        if (win) {
            a.setTitle("You won");
            if (otherDisconnected) {
                a.setHeaderText("The other user disconnected");
            } else {
                a.setHeaderText("You guessed the word correctly!");
            }
        } else {
            a.setTitle("You ran out of guesses!");
            a.setHeaderText("The correct word was " + generator.getSelectedWord());
        }
        Platform.runLater(this::playAgain);
    }

    private void playAgain() {
        Optional<ButtonType> result = a.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            initialize(defaultURL, defaultResBundle);
        } else {
            System.exit(0);
        }
    }

    private void resetLetters() {
        for (int i = 0; i < NUMBER_OF_ROWS*NUMBER_OF_COLUMNS; i++) {
            Label temp = (Label) userGrid.getChildren().get(i);
            temp.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
            temp.setText("");
            temp = (Label) opponentGrid.getChildren().get(i);
            temp.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
            temp.setText("");
        }
        statusLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(2*CORNER_RADIUS), new Insets(INSETS))));
        timeLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(2*CORNER_RADIUS), new Insets(INSETS))));
    }

    private Label getCurrentLabel(){
        return (Label) userGrid.getChildren().get(USER_LETTERS_ORDER.get(NUMBER_OF_COLUMNS * rowNum+columnNum));
    }

    @FXML
    private void letter(KeyEvent keyEvent) {
        System.out.println(keyEvent.getText());
        String entry = keyEvent.getCode().toString();
        if (entry.length() == 1 && Character.isAlphabetic(entry.charAt(0))){
            getCurrentLabel().setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
            if (columnNum < NUMBER_OF_COLUMNS) {
                getCurrentLabel().setText(keyEvent.getText().toLowerCase());
                columnNum++;
            }
            if (columnNum == NUMBER_OF_COLUMNS){
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                    columnNum = i;
                    builder.append(getCurrentLabel().getText());
                }
                nextRow(new Outcome(builder.toString(), true));
            } else {
                getCurrentLabel().setBackground(new Background(new BackgroundFill(Color.GRAY, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
            }
        } else if (entry.equals("BACK_SPACE")){
            getCurrentLabel().setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
            if (columnNum == NUMBER_OF_COLUMNS-1){
                getCurrentLabel().setText("");
                columnNum--;
            } else if (columnNum != 0){
                columnNum--;
            }
            getCurrentLabel().setText("");
            getCurrentLabel().setBackground(new Background(new BackgroundFill(Color.GRAY, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
        } else if (entry.equals("EQUALS")){
            updateRemainingWordVisibility();
        }
    }

    private void updateRemainingWordVisibility(){
        if (remainingWordsStage.isShowing()) {
            remainingWordsStage.hide();
        } else {
            remainingWordsStage.show();
            updateValidWordsTextField();
        }
    }

    private void updateValidWordsTextField(){
        remainingWordsField.setText(guesser.getValidWords().toString());
        remainingWordsStage.setTitle("Remaining words (" + guesser.getValidWords().size() + ")");
    }

    private void myTurn(){
        restartTimer();
        myTurn = true;
        statusLabel.setText("Your Turn");
        mainBox.setOnKeyReleased(this::letter);
        mainBox.setOnKeyPressed(action -> {
            System.out.println("press");
        });
        mainBox.setOnMouseClicked(action -> {
            System.out.println("click");
        });
        getCurrentLabel().setBackground(new Background(new BackgroundFill(Color.GRAY, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
        userGrid.setVisible(true);
        opponentGrid.setVisible(true);
    }

    private void restartTimer() {
        timeLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(2*CORNER_RADIUS), new Insets(INSETS))));
        timeRemaining = TIME_TO_RESPOND;
        timeLabel.setText("Time Remaining: " + timeRemaining + "s");
        if (timeline != null){
            timeline.stop();
        }
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        (EventHandler<ActionEvent>) event -> {
                            timeRemaining--;
                            timeLabel.setText("Time Remaining: " + timeRemaining + "s");
                            if (timeRemaining == 0){
                                if (myTurn) {
                                    resetRow();
                                    nextRow(new Outcome("", true));
                                }
                            }
                            if (timeRemaining==10){
                                timeLabel.setBackground(new Background(new BackgroundFill(Color.RED, new CornerRadii(2*CORNER_RADIUS), new Insets(INSETS))));
                            }
                            if (!continueGame){
                                timeLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(2*CORNER_RADIUS), new Insets(INSETS))));
                                timeline.stop();
                            }
                        }));
        timeline.playFromStart();
    }

    private void yourTurn(){
        myTurn = false;
        restartTimer();
        mainBox.setOnKeyReleased(action -> {
            if (action.getText().equals("=")){
                updateRemainingWordVisibility();
            }
        });
        statusLabel.setText("Your Opponent's Turn");
    }


    private void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.show();
    }


    public void invalidWord(String entry){
        Alert invalidWord = new Alert(Alert.AlertType.ERROR);
        invalidWord.setTitle("Invalid Word");
        invalidWord.setHeaderText(entry + " is not a valid word");
        invalidWord.show();
        getCurrentLabel().setText("");
        getCurrentLabel().setBackground(new Background(new BackgroundFill(Color.GRAY, new CornerRadii(CORNER_RADIUS), new Insets(INSETS))));
    }


}
