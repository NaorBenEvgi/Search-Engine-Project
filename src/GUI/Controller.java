package GUI;

import Indexing.*;
import Searching.Ranker;
import Searching.Searcher;

import java.io.*;
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
    private HashMap<String,ArrayList<String>> fiveEntitiesPerDoc;
    private int corpusSize, numOfTerms;
    private static int singleQueryID = 100;
    private HashMap<String,ArrayList<String>> resultsForEachQuery;

    public Controller() {
        indexer = new Indexer();
        corpusReader = new ReadFile();
        finalDictionary = new TreeMap<>();
        documentDetails = new HashMap<>();
        corpusSize = 0;
        numOfTerms = 0;
        resultsForEachQuery = new HashMap<>();
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
                ArrayList<Term> entitiesInDoc = parser.getTermEntitiesPerDoc(); 
                addDocEntitiesToFile(entitiesInDoc,doc,targetPath,stem);
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
            if(!new File(target.toString()).exists())
                Files.copy(source,target);
        } catch(IOException e){ }

        String entitiesDocPath = "entities";
        Path innerTargetPath;
        if(stem){
            entitiesDocPath += "Stem";
            innerTargetPath = Paths.get(targetPath).resolve("indexStem");
        }
        else{
            innerTargetPath = Paths.get(targetPath).resolve("index");
        }
        entitiesDocPath += ".txt";

        try {
            Files.move(Paths.get(targetPath).resolve(entitiesDocPath), Paths.get(innerTargetPath.toString()).resolve(entitiesDocPath));
        }catch (IOException e){
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

            fiveEntitiesPerDoc = searcher.getFiveEntitiesPerDoc();
            HashMap<String,HashMap<String,Double>> multipleQueriesResults = searcher.runMultipleQueries(parsedQueries, stem, semanticTreatment);
            addResults(multipleQueriesResults);
            return multipleQueriesResults;
        } else {
            ArrayList<String> queryWords = new ArrayList<>(Arrays.asList(query.split(" ")));
            queryWords = parser.parseQuery(queryWords, stem);
            retrievedDocs = searcher.runSingleQuery(queryWords, stem, semanticTreatment);

            HashMap<String,HashMap<String,Double>> queryResult = new HashMap<>();
            queryResult.put(String.valueOf(singleQueryID),retrievedDocs);
            addResults(queryResult);
            singleQueryID++;

            fiveEntitiesPerDoc = searcher.getFiveEntitiesPerDoc();
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
                    queryID = line.substring(line.indexOf("<num> Number: ")+14).trim();
                    line = reader.readLine();
                    query = line.substring(line.indexOf("<title> ")+8);
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

    /**
     * Collects the five most common entities in a document to the entities HashMap that is being created during the indexing process.
     * In case one or more of these five cannot be determined as entities during the accumulation, all of the possible entities in the document will be collected.
     * @param entitiesInDoc a list of the possible entities in the document
     * @param doc the document
     * @param targetPath the path to the index folder
     * @param stem indicates whether the terms in the documents have gone through stemming or not
     */
    private void addDocEntitiesToFile(ArrayList<Term> entitiesInDoc, Article doc, String targetPath, boolean stem){
        HashMap<String,Double> sortedEntities = new HashMap<>();
        for(Term entity : entitiesInDoc){ //puts each entity with its frequency in the document
            sortedEntities.put(entity.getTerm(),(double)(entity.getTermFrequency(doc)));
        }
        sortedEntities = Ranker.sortByValue(sortedEntities); //sorts the HashMap by the frequencies
        ArrayList<String> entities = new ArrayList<>(sortedEntities.keySet());
        StringBuilder fiveMostCommon = new StringBuilder().append(doc.getDocId()).append(":");
        boolean collectedFive = false;
        int counter = 0;
        for(String entity : entities){
            if(sortedEntities.get(entity) > 1){ //checks if the entity is determined as an entity compared to the whole corpus
                fiveMostCommon.append(entity).append(",");
            }
            else{
                break;
            }
            counter++;
            if(counter == 5){ //in case five entities were collected
                collectedFive = true;
                break;
            }
        }
        if(!collectedFive){ //in case one or more of the most common five entities cannot be determined as an entity yet
            fiveMostCommon = new StringBuilder().append(doc.getDocId()).append(":");
            for(String entity : entities){
                fiveMostCommon.append(entity).append(",");
            }
        }

        if(fiveMostCommon.charAt(fiveMostCommon.length()-1) == ',')
            writeEntitiesToFile(fiveMostCommon.substring(0,fiveMostCommon.length()-1),targetPath,stem);
        else
            writeEntitiesToFile(fiveMostCommon.toString(),targetPath,stem);
    }

    /**
     * Write the five most common entities in a document from the entities HashMap into a file that is being created during the indexing process.
     * @param entitiesLine the line to be written to the file (the document id and the entities found)
     * @param targetPath the path to the index folder
     * @param stem indicates whether the terms in the documents have gone through stemming or not
     */
    private void writeEntitiesToFile(String entitiesLine, String targetPath, boolean stem){
        Path pathToEntitiesFile;
        if(stem) {
            pathToEntitiesFile = Paths.get(targetPath).resolve("entitiesStem.txt");
        }
        else {
            pathToEntitiesFile = Paths.get(targetPath).resolve("entities.txt");
        }

        BufferedWriter entitiesLineWriter = null;
        try{
            entitiesLineWriter = new BufferedWriter(new FileWriter(pathToEntitiesFile.toString(),true));
            entitiesLineWriter.append(entitiesLine + "\n");
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (entitiesLineWriter != null) {
                    entitiesLineWriter.close();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the data structure that stores the five most common entities in the retrieved docs of the queries
     * @return the data structure that stores the five most common entities in the retrieved docs of the queries
     */
    public HashMap<String,ArrayList<String>> getFiveEntitiesPerDoc(){
        return fiveEntitiesPerDoc;
    }

    /**
     * Adds the retrieved documents' ids and the matching queries' ids into a temporary HashMap, until the user clicks the save button.
     * @param queryResults a HashMap that stores the queries and the retrieved documents for each one
     */
    public void addResults(HashMap<String,HashMap<String,Double>> queryResults){
        ArrayList<String> retrievedDocs;

        for(String queryID : queryResults.keySet()){
            HashMap<String,Double> docs = queryResults.get(queryID);
            docs = Ranker.sortByValue(docs);
            retrievedDocs = new ArrayList<>(docs.keySet());
            resultsForEachQuery.put(queryID,retrievedDocs);
        }
    }


    /**
     * Saves the results of one or more queries in a text file.
     * @param resultsFile the file chosen by the user
     */
    public void saveQueryResults(File resultsFile){
        BufferedWriter resultsWriter;
        try{
            resultsWriter = new BufferedWriter(new FileWriter(resultsFile));
            StringBuilder content = new StringBuilder();
            ArrayList<String> queryIDs = new ArrayList<>(resultsForEachQuery.keySet());
            ArrayList<Integer> sortedQueryIDsInt = new ArrayList<>();
            for(String id : queryIDs){
                sortedQueryIDsInt.add(Integer.valueOf(id));
            }
            Collections.sort(sortedQueryIDsInt);
            for(Integer queryID : sortedQueryIDsInt){
                ArrayList<String> docs = resultsForEachQuery.get(queryID.toString());
                for(int i=0; i<docs.size(); i++){
                    content.append(queryID.toString()).append(" 0 ").append(docs.get(i).trim()).append(" 0 0.0 a\n");
                }
            }
            resultsWriter.write(content.toString());
            resultsWriter.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        resultsForEachQuery = new HashMap<>();
    }

}



