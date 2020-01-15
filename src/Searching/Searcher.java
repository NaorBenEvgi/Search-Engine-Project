package Searching;

import com.medallia.word2vec.Word2VecModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

public class Searcher {

    private static Ranker ranker;
    private String targetPath;
    private SortedMap<String,String[]> finalDictionary;
    private HashMap<String,ArrayList<String>> fiveEntitiesPerDoc;

    /**
     * The Searcher constructor. The Object gets the final dictionary, the document details file and the path to the indexed files.
     * @param finalDictionary the final dictionary
     * @param documentDetails the document details file
     * @param targetPath the path to the indexed files
     */
    public Searcher(SortedMap<String, String[]> finalDictionary, HashMap<String, String[]> documentDetails, String targetPath){
        ranker = new Ranker(finalDictionary,documentDetails);
        this.targetPath = targetPath;
        this.finalDictionary = finalDictionary;
        this.fiveEntitiesPerDoc = new HashMap<>();
    }


    /**
     * Runs a query and returns the 50 most relevant documents
     * @param inputQuery the query
     * @param stem indicates whether the indexing process included stemming
     * @return the 50 most relevant documents and their ranks
     */
    public HashMap<String,Double> runSingleQuery(ArrayList<String> inputQuery, boolean stem, boolean semanticTreatment){
        ArrayList<String> postingLinesForQuery = new ArrayList<>();
        ArrayList<String> query = inputQuery;
        BufferedReader postingFilesReader;
        if(semanticTreatment){
            query = expandQuery(inputQuery);
        }
        try {
            query.sort(String.CASE_INSENSITIVE_ORDER);
            String lastPostingFile = "", currentPostingFile, line;
            postingFilesReader = null;
            for (String word : query) {
                currentPostingFile = getPathByWord(word, stem);
                if (!lastPostingFile.equals(currentPostingFile)) {
                    postingFilesReader = new BufferedReader(new FileReader(currentPostingFile));
                }
                while ((line = postingFilesReader.readLine()) != null) {
                    if (line.substring(0,line.indexOf("|")).equalsIgnoreCase(word)) {
                        postingLinesForQuery.add(line);
                        break;
                    }
                }
                lastPostingFile = currentPostingFile;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        HashMap<String,Double> mostRelevantDocs = ranker.rank(postingLinesForQuery, query);
        fillFiveEntitiesPerDoc(mostRelevantDocs,stem);

        return mostRelevantDocs;
    }


    /**
     * Runs multiple queries from a query file, and returns the 50 most relevant documents for each document
     * @param queries the queries that are written in a file
     * @param stem indicates whether the indexing process included stemming
     * @return the 50 most relevant documents for each document and their ranks
     */
    public HashMap<String,HashMap<String,Double>> runMultipleQueries(HashMap<String,ArrayList<String>> queries, boolean stem, boolean semanticTreatment){
        HashMap<String,HashMap<String,Double>> resultsForAllQueries = new HashMap<>();
        ArrayList<String> queriesIDs = new ArrayList<>(queries.keySet());
        for(String queryID : queriesIDs){
            HashMap<String,Double> queryResults = runSingleQuery(queries.get(queryID),stem, semanticTreatment);
            resultsForAllQueries.put(queryID,queryResults);
        }
        return resultsForAllQueries;
    }


    /**
     * Returns the posting file in which the word is indexed.
     * @param word the word to find the posting file for
     * @param stem indicates whether stemming has been done in the indexing process
     * @return the posting file in which the word is indexed
     */
    private String getPathByWord(String word, boolean stem){
        char queryWordInitial = Character.toUpperCase(word.charAt(0));
        String fileName, directoryName = "index";
        if(!Character.isLetter(queryWordInitial)){
            fileName = "numPostingFile";
        }
        else {
            fileName = queryWordInitial + "PostingFile";
        }
        if(stem){
            fileName += "Stem";
            directoryName += "Stem";
        }

        return Paths.get(targetPath).resolve(directoryName).resolve(fileName + ".txt").toString();
    }

    /**
     * Fills the data structure that stores the five most common entities in the retrieved docs of the queries
     * @param mostRelevantDocs the retrieved docs of the queries
     * @param stem indicates whether the indexing process included stemming
     */
    private void fillFiveEntitiesPerDoc(HashMap<String,Double> mostRelevantDocs, boolean stem){
        ArrayList<String> docs = new ArrayList<>(mostRelevantDocs.keySet());
        ArrayList<String> entities;
        Path entitiesFilePath;
        if(stem) {
            entitiesFilePath = Paths.get(targetPath).resolve("indexStem");
            entitiesFilePath = Paths.get(entitiesFilePath.toString()).resolve("entitiesStem.txt");
        }
        else{
            entitiesFilePath = Paths.get(targetPath).resolve("index");
            entitiesFilePath = Paths.get(entitiesFilePath.toString()).resolve("entities.txt");
        }

        BufferedReader entitiesFileReader = null;
        try {
            for (String doc : docs) {
                entities = new ArrayList<>();
                entitiesFileReader = new BufferedReader(new FileReader(entitiesFilePath.toString()));
                String line;
                while((line = entitiesFileReader.readLine()) != null){
                    if(line.startsWith(doc)){
                        String[] entitiesInDoc = line.substring(line.indexOf("|")+1).split(",");
                        for(int i=0; i<entitiesInDoc.length; i++){
                            if(finalDictionary.containsKey(entitiesInDoc[i])){
                                entities.add(entitiesInDoc[i]);
                                if(entities.size() == 5){
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
                fiveEntitiesPerDoc.put(doc,entities);
            }
            entitiesFileReader.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Returns the data structure that stores the five most common entities in the retrieved docs of the queries
     * @return the data structure that stores the five most common entities in the retrieved docs of the queries
     */
    public HashMap<String,ArrayList<String>> getFiveEntitiesPerDoc(){
        return fiveEntitiesPerDoc;
    }

    private ArrayList<String> expandQuery(ArrayList<String> query){
        ArrayList<String> expandedQuery = new ArrayList<>();
        try {
            //Word2VecModel model = Word2VecModel.fromTextFile((new File("C:\\Users\\Naor\\Desktop\\לימודים\\שנה ג\\סמסטר א\\אחזור\\עבודה\\semanticJar\\word2vec.c.output.model.txt")));
            Word2VecModel model = Word2VecModel.fromBinFile((new File("resources/corpusVector150K.bin")));
            com.medallia.word2vec.Searcher semanticSearcher = model.forSearch();
            int numOfResultInList = 2;
            for(String queryWord : query) {
                List<com.medallia.word2vec.Searcher.Match> matches;
                try {
                    matches = semanticSearcher.getMatches(queryWord.toLowerCase(), numOfResultInList);
                }catch (com.medallia.word2vec.Searcher.UnknownWordException e){
                    expandedQuery.add(queryWord);
                    continue;
                }
                for (com.medallia.word2vec.Searcher.Match match : matches) {
                    match.match();
                    String[] splitMatch = match.toString().split(" ");
                    expandedQuery.add(splitMatch[0]);
                }
            }
        } catch (IOException e) {

        }
        return expandedQuery;
    }


}
