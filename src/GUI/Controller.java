package GUI;

import Indexing.*;
import Searching.Searcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class is a controller that runs the search engine behind the scenes, following the commands sent by the user-interface.
 */
public class Controller extends Observable{

    private Indexer indexer;
    private ReadFile corpusReader;
    private SortedMap<String,String[]> finalDictionary;
    private HashMap<String,String[]> documentDetails;
    private int corpusSize, numOfTerms;
    private static int singleQueryID = 100;

    public Controller() {
        indexer = new Indexer();
        corpusReader = new ReadFile();
        finalDictionary = new TreeMap<>();
        documentDetails = new HashMap<>();
        corpusSize = 0;
        numOfTerms = 0;
    }

    /**
     * Activates the indexing process of the search engine. The function gets the path to the corpus,
     * reads all the documents stored in it, and indexes the corpus using a dictionary and posting files.
     * @param corpusPath the path to the corpus
     * @param targetPath the path of the directory in which the dictionary and posting files will be stored
     * @param stem an indicator of whether the indexing process will include stemming or not
     */
    public void runEngine(String corpusPath, String targetPath, boolean stem){
        corpusReader = new ReadFile();
        Parse parser = new Parse(corpusPath);
        indexer = new Indexer();
        ArrayList<File> filesInCorpus = new ArrayList<>();
        File corpus = new File(corpusPath);

        //creates the directories in which the posting files will be saved
        String tempFilesFolder1 = Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles1").toString()).toString();
        String tempFilesFolder2 = Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles2").toString()).toString();
        File tempDirectory1 = new File(tempFilesFolder1);
        tempDirectory1.mkdir();
        File tempDirectory2 = new File(tempFilesFolder2);
        tempDirectory2.mkdir();

        corpusReader.extractFilesFromFolder(corpus,filesInCorpus);
        //computes a threshold that indicates the amount of files to accumulate before writing a posting files with the terms in them
        long threshold = (corpusReader.getCorpusSize()/100)/(corpusReader.getCorpusSize()/filesInCorpus.size());
        if(threshold < 1){
            threshold = 1;
        }
        int fileCounter = 0;
        int tempFolderCounter1, tempFolderCounter2;

        for(File file : filesInCorpus){
            ArrayList<Article> docsInFile = corpusReader.readOneFile(file.getPath());
            for(Article doc : docsInFile){
                HashMap<String,Term> termsInDoc = parser.parse(doc,stem);
                indexer.collectTermPostingLines(termsInDoc,doc);
            }
            fileCounter++;
            if(fileCounter == threshold){
                indexer.createTemporaryPosting(tempFilesFolder1);
                fileCounter = 0;
            }
        }

        if(fileCounter > 0){
            indexer.createTemporaryPosting(tempFilesFolder1);
        }

        //merges all the initial posting files into temporary merged posting files, before eventually splitting them into 27 final posting files
        tempFolderCounter1 = tempDirectory1.listFiles().length;
        tempFolderCounter2 = tempDirectory2.listFiles().length;
        while(true){
            if(tempFolderCounter1 != 2 && tempFolderCounter2 == 0){
                mergeFiles(tempFilesFolder1,tempFilesFolder2);
            }
            else if(tempFolderCounter1 == 0 && tempFolderCounter2 != 2){
                mergeFiles(tempFilesFolder2,tempFilesFolder1);
            }
            else if(tempFolderCounter1 == 2 && tempFolderCounter2 == 0){
                File[] lastFiles = tempDirectory1.listFiles();
                indexer.createTermsListByLetter(lastFiles[0].getPath(),lastFiles[1].getPath(),targetPath,stem);
                break;
            }
            else if(tempFolderCounter1 == 0 && tempFolderCounter2 == 2){
                File[] lastFiles = tempDirectory2.listFiles();
                indexer.createTermsListByLetter(lastFiles[0].getPath(),lastFiles[1].getPath(),targetPath,stem);
                break;
            }
            tempFolderCounter1 = tempDirectory1.listFiles().length;
            tempFolderCounter2 = tempDirectory2.listFiles().length;
        }
        deleteDirectoryWithFiles(tempFilesFolder1);
        deleteDirectoryWithFiles(tempFilesFolder2);
        numOfTerms = indexer.getDictionary().size();
        corpusSize = indexer.getDocumentDetails().size();
        indexer = new Indexer();

        //copies the stop words file to the index directory
        try{
            Path source = Paths.get(corpusPath).resolve("stop_words.txt");
            Path target = Paths.get(targetPath).resolve("stop_words.txt");
            Files.copy(source,target);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Merges all the temporary posting files stored in the directories.
     * @param sourceFolderPath the path of the directory that contains the temporary posting files
     * @param destinationFolderPath the path of the directory that will store the merged posting files
     */
    private void mergeFiles(String sourceFolderPath, String destinationFolderPath){
        ArrayList<File> filesInSourceFolder = new ArrayList<>();
        File sourceFolder = new File(sourceFolderPath);
        corpusReader.extractFilesFromFolder(sourceFolder,filesInSourceFolder);

        try {
            if (filesInSourceFolder.size() % 2 != 0) { //in case there is an odd number of posting files, one is moved to the other temporary folder and the rest are merged
                String fileName = filesInSourceFolder.get(0).getName();
                Path newFilePath = Paths.get(destinationFolderPath).resolve(fileName);
                Files.move(Paths.get(filesInSourceFolder.get(0).getPath()), newFilePath);
                filesInSourceFolder.remove(0);
            }
            int arraySize = filesInSourceFolder.size();
            for(int i=0; i<arraySize; i+=2){
                File file1 = filesInSourceFolder.get(i), file2 = filesInSourceFolder.get(i+1);
                indexer.mergePostingFiles(file1.getPath(),file2.getPath(),destinationFolderPath);
            }
            deleteDirectoryFiles(sourceFolderPath);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Deletes a directory that has files in it.
     * @param directoryPath the path of the directory
     * @return true if the deletion was completed, false if not
     */
    private boolean deleteDirectoryWithFiles(String directoryPath){
        File directory = new File(directoryPath);
        if(directory.exists()){
            File[] files = directory.listFiles();
            for(File file : files){
                if(file.isDirectory()){
                    deleteDirectoryWithFiles(file.getPath());
                }
                file.delete();
            }
            return directory.delete();
        }
        return false;
    }

    /**
     * Deletes all the files in a given directory.
     * @param directoryPath the path to the directory
     * @return true if the deletion was completed, false if not
     */
    private boolean deleteDirectoryFiles(String directoryPath){
        File directory = new File(directoryPath);
        if(directory.exists()){
            File[] files = directory.listFiles();
            for(File file : files){
                file.delete();
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the amount of documents in the corpus that were indexed
     * @return the amount of documents in the corpus that were indexed
     */
    public int getAmountOfIndexedDocs(){
        return corpusSize;
    }

    /**
     * Returns the amount of unique terms that were indexed in the dictionary
     * @return the amount of unique terms that were indexed in the dictionary
     */
    public int getAmountOfUniqueTerms(){
        return numOfTerms;
    }


    /**
     * Deletes all the posting files and dictionary from the index directory
     * @param path the path of the index
     */
    public boolean deleteIndexes(String path){
        boolean ans = false;
        File[] filesInPath = new File(path).listFiles();
        for (File file : filesInPath) {
            if(file.isDirectory() && file.getName().contains("index")){
                deleteDirectoryWithFiles(file.getPath());
                ans = true;
            }
        }
        finalDictionary = new TreeMap<>();
        documentDetails = new HashMap<>();
        indexer = new Indexer();
        corpusReader = new ReadFile();
        corpusSize = 0;
        numOfTerms = 0;

        return ans;
    }


    /**
     * Reads the indexed file of the dictionary, and loads it into the HashMap
     * @param targetPath the path of the indexed files
     * @param stem an indicator of whether the terms have gone through stemming in the indexing process
     * @throws Exception in case the dictionary doesn't exist
     */
    public void loadDictionary(String targetPath, boolean stem) throws Exception{
        finalDictionary = new TreeMap<>();
        String innerTargetPath;
        ArrayList<File> filesInDirectory = new ArrayList<>();
        File dictionaryFile = null;
        BufferedReader dictionaryReader;
        if(stem){ //computes the path of the directory in which the wanted dictionary is stored
            innerTargetPath = Paths.get(targetPath).resolve("indexStem").toString();
        }
        else{
            innerTargetPath = Paths.get(targetPath).resolve("index").toString();
        }

        File innerDirectory = new File(innerTargetPath);
        corpusReader.extractFilesFromFolder(innerDirectory,filesInDirectory);
        for(File file : filesInDirectory){ //searches for the dictionary file
            if(file.getName().contains("finalDictionary")){
                dictionaryFile = file;
                break;
            }
        }

        //reads the dictionary file and fills the final dictionary HashMap with the content
        if(dictionaryFile == null){
            throw new NullPointerException();
        }
        dictionaryReader = new BufferedReader(new FileReader(dictionaryFile));
        String line, term;
        while((line = dictionaryReader.readLine()) != null){
            String[] termDetails = new String[2];
            String[] lineComponents = line.split("_");
            term = lineComponents[0];
            termDetails[0] = lineComponents[1];
            termDetails[1] = lineComponents[2];
            //termDetails[2] = lineComponents[3];  this is the size of the posting line - might want to remove this
            finalDictionary.put(term,termDetails);
        }
        numOfTerms = filesInDirectory.size();
        dictionaryReader.close();
    }


    // ----------------------------------------------------------------- PART B ADDITIONS----------------------------------------------------------------------------------

    /**
     * Reads the indexed file of the documents' details, and loads it into the HashMap
     * @param targetPath the path of the indexed files
     * @param stem an indicator of whether the terms have gone through stemming in the indexing process
     * @throws Exception in case the file doesn't exist
     */
    public void loadDocumentDetails(String targetPath, boolean stem) throws Exception{
        documentDetails = new HashMap<>();
        String innerTargetPath;
        ArrayList<File> filesInDirectory = new ArrayList<>();
        File documentDetailsFile = null;
        BufferedReader documentDetailsReader;
        if(stem){ //computes the path of the directory in which the wanted dictionary is stored
            innerTargetPath = Paths.get(targetPath).resolve("indexStem").toString();
        }
        else{
            innerTargetPath = Paths.get(targetPath).resolve("index").toString();
        }

        File innerDirectory = new File(innerTargetPath);
        corpusReader.extractFilesFromFolder(innerDirectory,filesInDirectory);
        for(File file : filesInDirectory){ //searches for the dictionary file
            if(file.getName().contains("documentDetails")){
                documentDetailsFile = file;
                break;
            }
        }

        //reads the dictionary file and fills the final dictionary HashMap with the content
        if(documentDetailsFile == null){
            throw new NullPointerException();
        }
        documentDetailsReader = new BufferedReader(new FileReader(documentDetailsFile));
        String line, docID;
        while((line = documentDetailsReader.readLine()) != null){
            String[] detailsAboutDocs = new String[4];
            String[] lineComponents = line.split("_");
            docID = lineComponents[0];
            detailsAboutDocs[0] = lineComponents[1];
            detailsAboutDocs[1] = lineComponents[2];
            detailsAboutDocs[2] = lineComponents[3];
            detailsAboutDocs[3] = lineComponents[4];
            documentDetails.put(docID,detailsAboutDocs);
        }
        /*corpusSize = documentDetails.size();*/
        documentDetailsReader.close();
    }


    /**
     * Runs a single query or a multiple queries that are stored in a file, and returns the 50 most relevant documents for each query, and the similarity rank of each one.
     * @param query the query or a path to a file of queries
     * @param targetPath the path to the directory of the indexed files
     * @param stem indicates whether the terms in the documents have gone through stemming or not
     * @return the 50 most relevant documents for each query, and the similarity rank of each one
     */
    public HashMap<String,HashMap<String,Double>> runQuery(String query, String targetPath, boolean stem, boolean semanticTreatment) {
        Parse parser = new Parse(targetPath);
        Searcher searcher = new Searcher(finalDictionary, documentDetails, targetPath);
        HashMap<String,Double> retrievedDocs;
        if (new File(query).exists()) {
            HashMap<String, ArrayList<String>> rawQueries = readQueryFile(query); //queries as they appear in the file
            HashMap<String, ArrayList<String>> parsedQueries = new HashMap<>(); //queries after parsing and stemming
            ArrayList<String> queryIDs = new ArrayList<>(rawQueries.keySet());
            for (String queryID : queryIDs) {
                parsedQueries.put(queryID, parser.parseQuery(rawQueries.get(queryID), stem));
            }
            return searcher.runMultipleQueries(parsedQueries, stem, semanticTreatment);
        } else {
            ArrayList<String> queryWords = new ArrayList<>(Arrays.asList(query.split(" ")));
            queryWords = parser.parseQuery(queryWords, stem);
            retrievedDocs = searcher.runSingleQuery(queryWords, stem, semanticTreatment);

            HashMap<String,HashMap<String,Double>> queryResult = new HashMap<>();
            queryResult.put(String.valueOf(singleQueryID),retrievedDocs);
            singleQueryID++;
            return queryResult;
        }
    }


    /**
     * Reads a file of queries and extracts the queries and their IDs into a HashMap.
     * @param queryFilePath the path to the file of queries
     * @return a HashMap of the queries' IDs and the list of words in each query
     */
    private HashMap<String,ArrayList<String>> readQueryFile(String queryFilePath){
        HashMap<String,ArrayList<String>> queries = new HashMap<>();
        BufferedReader reader;
        String line, queryID, query;
        try{
            reader = new BufferedReader(new FileReader(queryFilePath));
            while((line = reader.readLine()) != null){
                if(line.startsWith("<num>")){
                    queryID = line.substring(line.indexOf("<num> Number: ")+14);
                    line = reader.readLine();
                    query = line.substring(line.indexOf("<title> "));
                    ArrayList<String> queryWords = new ArrayList<>(Arrays.asList(query.split(" ")));
                    queries.put(queryID,queryWords);
                }
            }
            reader.close();
            return queries;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}



