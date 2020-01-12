package GUI;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * This class is responsible for connecting between the Controller and the View classes. The methods in this class are called automatically cy the View,
 * and call the matching methods in the Controller.
 */
public class ViewController extends Observable implements Observer {

    private Controller controller;


    public ViewController(Controller controller){
        this.controller = controller;
    }


    /**
     * Runs the engine by calling the method in the Controller.
     * @param corpusPath the path to the corpus
     * @param targetPath the path of the index files
     * @param stem indicates whether there has been stemming in the indexing process.
     */
    public void runEngine(String corpusPath, String targetPath, boolean stem){
        controller.runEngine(corpusPath,targetPath,stem);
    }

    /**
     * Returns the amount of documents that were indexed
     * @return the amount of documents that were indexed
     */
    public int getAmountOfIndexedDocs(){
        return controller.getAmountOfIndexedDocs();
    }

    /**
     * Returns the amount of unique terms that were indexed
     * @return the amount of unique terms that were indexed
     */
    public int getAmountOfUniqueTerms(){
        return controller.getAmountOfUniqueTerms();
    }

    /**
     * Reads the file of the final dictionary and fills the HashMap of it with the content
     * @param indexPath the path of the index
     * @param stem indicates whether the wanted dictionary is with or without stemming
     * @throws Exception in case the dictionary file does not exist
     */
    public void loadDictionary(String indexPath, boolean stem) throws Exception{
        controller.loadDictionary(indexPath,stem);
    }

    /**
     * Reads the file of the documents' details and fills the HashMap of it with the content
     * @param indexPath the path to the directory that contains the file
     * @param stem indicates whether the wanted dictionary is with or without stemming
     * @throws Exception in case the document details file doesn't exist
     */
    public void loadDocumentDetails(String indexPath, boolean stem) throws Exception{
        controller.loadDocumentDetails(indexPath,stem);
    }

    /**
     * Deletes all the posting files and dictionary and the directories they are stored in.
     * @param path the path of the index
     * @return true if the deletion was successful, false otherwise
     */
    public boolean reset(String path){
        return controller.deleteIndexes(path);
    }


    @Override
    public void update(Observable o, Object arg) {
        if(o == controller){
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Calls the runQuery method in the controller. The method runs the query and returns the 50 most relevant documents.
     * @param query the query
     * @param targetPath the path to the index
     * @param stem determines if the content includes stemming
     * @return the 50 most relevant documents to the query
     */
    public HashMap<String,HashMap<String,Double>> runQuery(String query, String targetPath, boolean stem){
        return controller.runQuery(query, targetPath, stem);
    }
}
