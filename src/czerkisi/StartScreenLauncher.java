package czerkisi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartScreenLauncher extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("WordleStartScreen.fxml"));
        Parent root = loader.load();
        StartScreenController controller = loader.getController();
        controller.setMainStage(stage);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Main Screen");
        stage.show();
    }
}
