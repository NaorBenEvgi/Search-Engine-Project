package GUI;

import Indexing.ReadFile;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;

public class View implements Observer {

    private ViewController viewController;
    public TextField corpusPathTextField;
    public TextField indexPathTextField;
    public Button corpusPathBrowser;
    public Button indexPathBrowser;
    public javafx.scene.control.CheckBox stemmingCheckbox;
    private boolean stem;
    public Button runEngineButton;
    public Button loadDictionaryButton;
    public Button displayDictionaryButton;
    public Button clearIndexButton;
    public Label corpusPathLabel;
    public Label indexPathLabel;
    private ReadFile reader;


    /**
     * Assigns the ViewController of the program with the given ViewController
     * @param vc the given ViewController
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
        if(corpusPath.equals("") || targetPath.equals("")){
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
        try{
            viewController.loadDictionary(indexPath, stem);
            displayAlert("The dictionary has been loaded successfully","");
        }
        catch (Exception e){
            displayAlert("The dictionary is missing","Please run the search-engine first!");
        }
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
        reader = new ReadFile();
        SortedMap<String,String> finalDictionary = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String innerTargetPath;
        ArrayList<File> filesInDirectory = new ArrayList<>();
        File dictionaryFile = null;
        BufferedReader dictionaryReader;
        if(stem){
            innerTargetPath = Paths.get(indexPathTextField.getText()).resolve("indexStem").toString();
        }
        else{
            innerTargetPath = Paths.get(indexPathTextField.getText()).resolve("index").toString();
        }

        File innerDirectory = new File(innerTargetPath);

        if(!innerDirectory.exists()){
            displayAlert("The dictionary does not exist","Please run the engine before any try again!");
            return;
        }
        reader.extractFilesFromFolder(innerDirectory,filesInDirectory);
        for(File file : filesInDirectory){
            if(file.getName().contains("finalDictionary")){
                dictionaryFile = file;
                break;
            }
        }

        try{
            dictionaryReader = new BufferedReader(new FileReader(dictionaryFile));
            String[] termDetails = new String[3];
            String line, term, totalTF;
            while((line = dictionaryReader.readLine()) != null){
                String[] lineComponents = line.split("_");
                term = lineComponents[0];
                totalTF = lineComponents[1];
                finalDictionary.put(term,totalTF);
            }
            dictionaryReader.close();
        } catch(Exception e){
            displayAlert("The dictionary does not exist","Please run the engine before any try again!");
        }

        JTable table=new JTable(convertDictionaryToTable(finalDictionary));
        JFrame frame=new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new JScrollPane(table));
        frame.setSize(600,800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    /**
     * Resets the memory and deletes the posting files and the dictionary.
     */
    public void reset(){
        if(viewController.reset(indexPathTextField.getText()))
            displayAlert("The memory was cleared successfully", "Hope to see you next time!");
        else
            displayAlert("No files were deleted","Couldn't find any files to delete");
    }


    /**
     * Enables the user to select the path of the corpus
     */
    public void browseCorpusPath(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose the directory of the corpus");
        File corpus = chooser.showDialog(new Stage());
        corpusPathTextField.setText(corpus.getPath());
    }


    /**
     * Enables the user to select the path of the index
     */
    public void browseIndexPath(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose the directory of the index files");
        File index = chooser.showDialog(new Stage());
        indexPathTextField.setText(index.getPath());
    }


    @Override
    public void update(Observable o, Object arg) { }

    public static TableModel convertDictionaryToTable(Map<String,String> map) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "Term", "Total appearances in corpus" }, 0
        );
        for (Map.Entry<String,String> entry : map.entrySet()) {
            model.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }
        return model;
    }
}

