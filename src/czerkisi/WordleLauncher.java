package czerkisi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class WordleLauncher extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(WordleLauncher.class.getResource("WordleHostGameScreen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setWidth(700);
            stage.setHeight(600);
            stage.setScene(scene);
            stage.setTitle("Main");
            WordleHostController controller = loader.getController();
            controller.setGoesFirst(true);
            /*controller.setOutput(new Socket());
            controller.setInput(new Socket());*/
            stage.show();
        } catch (NumberFormatException e) {
            //displayNFE();
        } catch (IOException e) {
            //displayPortError("No Wordle Game Found on Port " + portNumber);
        } catch (IllegalArgumentException e) {
            //displayPortError("The port number cannot be blank");
        }
    }
}
