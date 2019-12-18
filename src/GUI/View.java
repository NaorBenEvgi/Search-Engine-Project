package GUI;


import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class View extends Canvas implements Observer {

    private ViewController viewController;
    public TextField corpusPathTextField;
    public TextField indexPathTextField;
    public Button corpusPathBrowser;
    public Button indexPathBrowser;
    public javafx.scene.control.CheckBox stemmingCheckbox;
    boolean stem;
    public Button runEngineButton;
    public Button loadDictionaryButton;
    public Button displayDictionaryButton;
    public Button clearIndexButton;
    public Label corpusPathLabel;
    public Label indexPathLabel;


    /**
     * Assigns the ViewController of the program with the given ViewController
     * @param vc
     */
    public void setViewController(ViewController vc){
        this.viewController = vc;
    }


    /**
     * Runs the indexing process of the engine, using the paths in the text boxes and the mark of the stemming checkbox
     */
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


    /**
     * Loads a dictionary from the dictionary file into the dictionary object in the program.
     */
    public void loadDictionary(){
        String indexPath = indexPathTextField.getText();
        viewController.loadDictionary(indexPath, stem);
    }

    /**
     * Opens a window with given title and text, in order to send a message to the user.
     * @param title the title of the message
     * @param alert the content of the message
     */
    private void displayAlert(String title, String alert){
        Alert displayedAlert = new Alert(Alert.AlertType.INFORMATION);
        displayedAlert.setTitle(title);
        displayedAlert.setContentText(alert);
        displayedAlert.setHeaderText("");
        DialogPane dialogPane = displayedAlert.getDialogPane();
        displayedAlert.show();
    }


    /**
     * Sets the stemming value according to whether the stemming checkbox is checked or not
     */
    public void checkStemming(){
        stem = stemmingCheckbox.isSelected();
    }


    /**
     * Pops a window that displays the dictionary.
     */
    public void displayDictionary(){

    }


    /**
     * Resets the memory and deletes the posting files and the dictionary.
     */
    public void reset(){
        viewController.reset();
    }


    /**
     * Enables the user to select the path of the corpus
     */
    public void browseCorpusPath(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose the directory of the corpus");
        File corpus = chooser.showDialog(null);


        /*File compressedMaze = chooser.showOpenDialog((Stage)mazeDisplayer.getScene().getWindow());
        if(compressedMaze != null){
            viewModel.loadMaze(compressedMaze);
            playSong("resources/music/EntranceOriginalSmurfSong.mp3");
            solveMazeButton.setSelected(false);
            hintButton.setDisable(false);
            mazeDisplayer.requestFocus();
            startedTime = System.currentTimeMillis();
            timeLabel.setText("");
        }*/
    }


    /**
     * Enables the user to select the path of the index
     */
    public void browseIndexPath(){

    }


    @Override
    public void update(Observable o, Object arg) {

    }
}
