package Searching;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;

public class Searcher {

    private static Ranker ranker;
    private String targetPath; //the path is to the inner
    // private String corpusPath


    public Searcher(SortedMap<String, String[]> finalDictionary, HashMap<String, String[]> documentDetails, String targetPath){
        ranker = new Ranker(finalDictionary,documentDetails);
        this.targetPath = targetPath;
    }


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


    /*
        The idea is to iterate over the words in the query, and find the posting line of each one. The line will be put in an arrayList
        of all the lines, and after the iteration is over, the arrayList will be sent to the Ranker. The Ranker will be responsible
        for grading each document, according to all the parameters in the posting line, the document details and the final dictionary,
        and then send back a list of the 50 most relevant documents to the searcher.
     */











}
