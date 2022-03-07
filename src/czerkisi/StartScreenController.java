package czerkisi;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ResourceBundle;

public class StartScreenController implements Initializable {
    @FXML
    private TextField portNumberField;
    private int portNumber;
    private Stage awaitingConnectionStage;
    private Stage mainStage;
    boolean hostGoesFirst;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hostGoesFirst = Math.random() < 0.5;

    }

    public void setMainStage(Stage stage){
        mainStage = stage;
    }

    @FXML
    private void createGame() {
        try {
            int portNumber = getPortNumber();

            displayAwaiting(portNumber);

            ServerSocket host = new ServerSocket(portNumber);
            Socket socket1 = host.accept();
            Socket socket2 = new Socket("localhost", portNumber+1);

            socket1.setSoTimeout(300);
            socket2.setSoTimeout(300);

            Stage gameStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("WordleHostGameScreen.fxml"));
            Parent root = loader.load();
            WordleHostController controller = loader.getController();
            controller.setGoesFirst(hostGoesFirst);
            controller.setOutput(socket1);
            controller.setInput(socket2);
            Scene scene = new Scene(root);
            gameStage.setWidth(700);
            gameStage.setHeight(600);
            gameStage.setScene(scene);
            gameStage.setTitle("Main");
            awaitingConnectionStage.hide();
            mainStage.hide();
            gameStage.show();
        } catch (NumberFormatException e){
            displayNFE();
        } catch (SocketTimeoutException e) {
            displayPortError(e.getMessage());
        } catch (IOException e){
            displayPortError("No Wordle Game Found on Port " + portNumber);
        } catch (IllegalArgumentException e){
            displayPortError("The port number cannot be blank");
        }
    }

    private void displayPortError(String header){
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("No Game Found");
        a.setHeaderText(header);
        a.setContentText("Enter a valid port number");
        a.show();
    }

    @FXML
    private void joinGame(){
        int portNumber = 0;
        try {
            portNumber = getPortNumber();

            Socket socket1 = new Socket("localhost", portNumber);
            ServerSocket host = new ServerSocket(portNumber+1);
            Socket socket2 = host.accept();

            socket1.setSoTimeout(300);
            socket2.setSoTimeout(300);

            Stage gameStage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("WordleClientGameScreen.fxml"));
            Parent root = loader.load();
            WordleClientController controller = loader.getController();
            controller.setGoesFirst(hostGoesFirst);
            controller.setOutput(socket1);
            controller.setInput(socket2);
            Scene scene = new Scene(root);
            gameStage.setWidth(700);
            gameStage.setHeight(600);
            gameStage.setScene(scene);
            gameStage.setTitle("Main");
            mainStage.hide();
            gameStage.show();
        } catch (NumberFormatException e){
            displayNFE();
        } catch (IOException e){
            displayPortError("No Wordle Game Found on Port " + portNumber);
        } catch (IllegalArgumentException e) {
            displayPortError("The port number cannot be blank");
        }

    }

    private int getPortNumber() throws IllegalArgumentException {
        if (portNumberField.getText().isEmpty()){
            throw new IllegalArgumentException("Port number cannot be blank");
        }
        return Integer.parseInt(portNumberField.getText());
    }

    private void displayNFE(){
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("Invalid Port Number");
        a.setContentText(portNumberField.getText() + " is not an acceptable number");
        a.show();
    }

    private void displayAwaiting(int portNumber) throws IOException{
        awaitingConnectionStage = new Stage();
        FXMLLoader awaitingLoader = new FXMLLoader();
        awaitingLoader.setLocation(getClass().getResource("AwaitingConnectionScreen.fxml"));
        Parent awaitingRoot = awaitingLoader.load();
        Scene awaitingScene = new Scene(awaitingRoot);
        awaitingConnectionStage.setScene(awaitingScene);
        awaitingConnectionStage.setTitle("Waiting for User to Join on Port " + portNumber + "...");
        awaitingConnectionStage.show();
    }


    @FXML
    private void cancelAwaitingConnection(){
        System.out.println("cancel");
    }
}
