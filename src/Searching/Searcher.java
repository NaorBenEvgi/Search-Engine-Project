package Searching;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;

public class Searcher {

    private static Ranker ranker;
    private String targetPath;

    /**
     * The Searcher constructor. The Object gets the final dictionary, the document details file and the path to the indexed files.
     * @param finalDictionary the final dictionary
     * @param documentDetails the document details file
     * @param targetPath the path to the indexed files
     */
    public Searcher(SortedMap<String, String[]> finalDictionary, HashMap<String, String[]> documentDetails, String targetPath){
        ranker = new Ranker(finalDictionary,documentDetails);
        this.targetPath = targetPath;
    }


    /**
     * Runs a query and returns the 50 most relevant documents
     * @param query the query
     * @param stem indicates whether the indexing process included stemming
     * @return the 50 most relevant documents and their ranks
     */
    public HashMap<String,Double> runSingleQuery(ArrayList<String> query, boolean stem){
        ArrayList<String> postingLinesForQuery = new ArrayList<>();
        BufferedReader postingFilesReader;

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

         return ranker.rank(postingLinesForQuery, query);
    }


    /**
     * Runs multiple queries from a query file, and returns the 50 most relevant documents for each document
     * @param queries the queries that are written in a file
     * @param stem indicates whether the indexing process included stemming
     * @return the 50 most relevant documents for each document and their ranks
     */
    public HashMap<String,HashMap<String,Double>> runMultipleQueries(HashMap<String,ArrayList<String>> queries, boolean stem){
        HashMap<String,HashMap<String,Double>> resultsForAllQueries = new HashMap<>();
        ArrayList<String> queriesIDs = new ArrayList<>(queries.keySet());
        for(String queryID : queriesIDs){
            HashMap<String,Double> queryResults = runSingleQuery(queries.get(queryID),stem);
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

}
