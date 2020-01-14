package GUI;

import Indexing.ReadFile;
import Searching.Ranker;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class is responsible for managing the user interface of the search engine.
 */
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

    //partB additions
    public TextField queryTextField;
    public Label queryLabel;
    public Button queryFileBrowser;
    public Button runQueryButton;
    public javafx.scene.control.CheckBox semanticTreatmentCheckbox;
    public Button saveResultsButton;
    private boolean semanticTreatment, mouseClick = false;

    //TODO: need to add entities identification functionality


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
            viewController.loadDocumentDetails(indexPath,stem);
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
    public void displayDictionary(){ //FIXME: change this to reading from the dictionary in the memory, not in the disk.
        reader = new ReadFile();
        SortedMap<String,String> finalDictionary = new TreeMap<>();
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

    /**
     * Converts the dictionary from a HashMap to a table form, with the term in the left column and its frequency in the corpus on the right column.
     * @param map the dictionary to display
     * @return a table that contains the final dictionary
     */
    public static TableModel convertDictionaryToTable(Map<String,String> map) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "Term", "Total appearances in corpus" }, 0
        );
        for (Map.Entry<String,String> entry : map.entrySet()) {
            model.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }
        return model;
    }



    // ------------------------------------------------------------- PART B ADDITIONS --------------------------------------------------------------------------------
    /**
     * Runs a query in the engine, according to the words or the file in the query text field.
     */
    public void runQuery(){
        if(queryTextField.getText().isEmpty()){
            displayAlert("Wrong Input!","The query is missing! Please insert a query in the query text box");
        }
        else{
            String query = queryTextField.getText();
            HashMap<String,HashMap<String,Double>> retrievedDocs = viewController.runQuery(query,indexPathTextField.getText(),stem, semanticTreatment);
            ArrayList<String> queryIDs = new ArrayList<>(retrievedDocs.keySet());

           ArrayList<String[]> docsToDisplay = new ArrayList<>();
            for(String queryID : queryIDs) {
                HashMap<String,Double> sortedRetrievedDocs;
                sortedRetrievedDocs = Ranker.sortByValue(retrievedDocs.get(queryID));
                ArrayList<String> docs = new ArrayList<>(sortedRetrievedDocs.keySet());

                for (int i = 0; i < sortedRetrievedDocs.size(); i++) {
                    String[] values = new String[4];
                    values[0] = String.valueOf(i+1);
                    values[1] = queryID; //queryID
                    values[2] = docs.get(i); //docID
                    values[3] = String.valueOf(sortedRetrievedDocs.get(values[2])); //rank
                    docsToDisplay.add(values);
                }
            }

            JTable table=new JTable(convertQueryResultsToTable(docsToDisplay));
            JFrame frame=new JFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new JScrollPane(table));
            frame.setSize(600,800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // http://java-buddy.blogspot.com/2013/12/java-swing-jtable-and.html - taken from Muhsen and Evgeny
            table.getSelectionModel().addListSelectionListener(event -> {
                if(!mouseClick) {
                    Platform.runLater(() -> {
                        String entities = "";
                        ArrayList<String> entitiesPerDoc = viewController.getFiveEntitiesPerDoc().get((table.getValueAt(table.getSelectedRow(), 2)));
                        String content = "";
                        if(entitiesPerDoc.size() == 0){
                            content = "No entities to show!";
                        }
                        else {
                            for (int i = 0; i < entitiesPerDoc.size(); i++) {
                                entities += entitiesPerDoc.get(i) + "\n";
                            }
                            content = entities.substring(0, entities.length() - 2);
                        }
                        displayAlert("High Five (entities)!", content);
                        mouseClick = true;
                    });
                }
                else{
                    mouseClick = false;
                }
            });

        }

    }


    /**
     *
     * @param
     * @return
     */
    public static TableModel convertQueryResultsToTable(ArrayList<String[]> results) {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] { "#", "Query Number", "DocID", "Rank"}, 0);
        for (String[] entry : results) {
            model.addRow(new Object[] { entry[0], entry[1], entry[2], entry[3]});
        }
        return model;
    }


    /**
     * Saves the results of the engine to a query in a text file.
     */
    public void saveResults(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save results");
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(" ",".txt"));

        //opens the file saving window
        File resultsFile = chooser.showSaveDialog(new Stage());
        if(resultsFile != null){
            resultsFile = new File(resultsFile.getPath()+".txt");
            viewController.saveQueryResults(resultsFile);
        }
    }

    /**
     * Sets the semantic treatment value according to whether the matching checkbox is checked or not
     */
    public void checkSemanticTreatment(){
        semanticTreatment = semanticTreatmentCheckbox.isSelected();
    }


    /**
     * Enables the user to select the path of a file with queries to run
     */
    public void browseQueryPath(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose the file of the queries");
        queryTextField.setText(chooser.showOpenDialog(new Stage()).getPath());
    }






}

