package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("View.fxml"));
        Stage stage = new Stage();
        Controller controller = new Controller();
        ViewController viewController = new ViewController(controller);
        controller.addObserver(viewController);
        primaryStage.setTitle("Fake News Search Engine");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Scene scene = new Scene(fxmlLoader.load(getClass().getResource("View.fxml").openStream()),1000,600);
        primaryStage.setScene(scene);
        View view = fxmlLoader.getController();
        view.setViewController(viewController);
        viewController.addObserver(view);
        primaryStage.show();
    }





}
