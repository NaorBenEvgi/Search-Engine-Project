package Indexing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Indexer {

    private HashMap<String,StringBuilder> postingLines;
    private int postingFilesCounter;

    public Indexer(){
        postingFilesCounter = 0;
        postingLines = new HashMap<>();
    }


    /**
     * Iterates over a dictionary that contains all the terms in a given document, merges each term's posting file lines into
     * one StringBuilder, and saves it in the posting lines HashMap
     * @param documentDictionary the HashMap of all the terms in the document
     * @param doc the given document
     */
    public void collectTermPostingLines(HashMap<String,Term> documentDictionary, Article doc){
        for(String term : documentDictionary.keySet()){
            if(postingLines.containsKey(term)){
                postingLines.get(term).append(documentDictionary.get(term).getPostingLineInDoc(doc));
            }else{
                postingLines.put(term,new StringBuilder(documentDictionary.get(term).getPostingLineInDoc(doc)));
            }
        }
    }


    /**
     *
     * @param path the path of the directory in which the temporary file will be saved
     */
    public void createTemporaryPosting(String path){
        ArrayList<String> sortedTerms = new ArrayList<>(postingLines.keySet());
        Collections.sort(sortedTerms);
        StringBuilder temporaryPostingLinesBuilder = new StringBuilder();
        for(int i=0; i<sortedTerms.size(); i++){
            temporaryPostingLinesBuilder.append(sortedTerms.get(i)).append("|" + postingLines.get(i).toString()+"\n");
            postingLines.remove(sortedTerms.get(i));
        }
        String pathToTemporaryFile = path + "\\tempPostingFile" + postingFilesCounter;
        writePostingLinesToTempFile(pathToTemporaryFile,temporaryPostingLinesBuilder.toString());
        postingFilesCounter++;
    }


    /**
     * Given a path and content that contains the posting lines, the function writes the content into a file it creates and saves it in the given path
     * @param path the given path to save the file in
     * @param content the content of the file
     */
    private void writePostingLinesToTempFile(String path, String content){
        File file = new File(path);
        BufferedWriter postingLinesWriter = null;
        try{
            postingLinesWriter = new BufferedWriter(new FileWriter(file));
            postingLinesWriter.write(content);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                postingLinesWriter.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
