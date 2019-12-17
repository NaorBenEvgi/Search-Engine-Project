package GUI;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class View implements Observer {

    private ViewController viewController;
    public TextField corpusPathTextField;
    public TextField indexPathTextField;
    public Button corpusPathBrowser;
    public Button indexPathBrowser;
    public Checkbox stemmingCheckbox;
    boolean stem;
    public Button runEngineButton;
    public Button loadDictionaryButton;
    public Button displayDictionaryButton;
    public Button clearIndexButton;
    public Label corpusPathLabel;
    public Label indexPathLabel;


    public void setViewController(ViewController vc){
        this.viewController = vc;
    }


    public void activateEngine(){
        String corpusPath = corpusPathTextField.getText();
        String targetPath = indexPathTextField.getText();
        if(corpusPath == null || targetPath == null){
            displayAlert("Wrong Input!","One or more of the paths is missing!");
            return;
        }

        long startTime = System.currentTimeMillis();
        viewController.runEngine(corpusPath,targetPath,stem);
        long finishedTime = (System.currentTimeMillis() - startTime)/1000;
        StringBuilder content = new StringBuilder();
        content.append("Amount of indexed files: ").append(viewController.getAmountOfIndexedDocs()).append("\n");
        content.append("Amount of unique terms: ").append(viewController.getAmountOfUniqueTerms()).append("\n");
        content.append("The process took ").append(finishedTime).append(" seconds");

        String title = "Indexing Details";
        displayAlert(title,content.toString());
    }



    public void loadDictionary(){
        String indexPath = indexPathTextField.getText();
        viewController.loadDictionary(indexPath, stem);
    }


    private void displayAlert(String title, String alert){
        Alert displayedAlert = new Alert(Alert.AlertType.INFORMATION);
        displayedAlert.setTitle(title);
        displayedAlert.setContentText(alert);
        displayedAlert.setHeaderText("");
        DialogPane dialogPane = displayedAlert.getDialogPane();
        displayedAlert.show();
    }



    @Override
    public void update(Observable o, Object arg) {

    }
}
