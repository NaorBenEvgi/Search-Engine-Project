package Indexing;

import javafx.util.Pair;

import java.io.*;
import java.nio.Buffer;
import java.util.*;

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

    /**
     *  Merges two temporary posting files into one sorted posting file.
     * @param firstFilePath the path to the first file
     * @param secondFilePath the path to the second file
     * @param targetPath the path in which the merged file will be saved
     */
    public void mergePostingFiles(String firstFilePath, String secondFilePath, String targetPath){
        String mergedPostingFilePath = targetPath + "\\tempPostingFile" + postingFilesCounter;
        postingFilesCounter++;
        SortedMap<String,StringBuilder> mergedDictionary = new TreeMap<>();
        BufferedReader postingFile1,postingFile2;
        try {
            postingFile1 = new BufferedReader(new FileReader(firstFilePath));
            String[] linesInFile1 = (String[])postingFile1.lines().toArray();
            for(int i=0; i<linesInFile1.length; i++){
                Pair<String,StringBuilder> mapEntry = convertLineToTermAndPosting(linesInFile1[i]);
                mergedDictionary.put(mapEntry.getKey(),mapEntry.getValue());
            }

            postingFile2 = new BufferedReader(new FileReader(secondFilePath));
            String[] linesInFile2 = (String[])postingFile2.lines().toArray();
            for(int i=0; i<linesInFile2.length; i++){
                Pair<String,StringBuilder> mapEntry = convertLineToTermAndPosting(linesInFile2[i]);
                if(mergedDictionary.containsKey(mapEntry.getKey())){
                    mergedDictionary.get(mapEntry.getKey()).append(mapEntry.getValue().toString().replace(mapEntry.getKey() + "|",""));
                } else{
                    mergedDictionary.put(mapEntry.getKey(),mapEntry.getValue());
                }
            }

            StringBuilder fileContent = new StringBuilder();
            Iterator<String> termsIterator = mergedDictionary.keySet().iterator();
            while(termsIterator.hasNext()){
                fileContent.append(mergedDictionary.get(termsIterator.next())).append("\n");
            }
            writePostingLinesToTempFile(mergedPostingFilePath,fileContent.toString());

        } catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     *
     * @param line
     * @return
     */
    private Pair<String,StringBuilder> convertLineToTermAndPosting(String line){
        String[] termAndPosting = line.split("|");
        StringBuilder postingLine = new StringBuilder();
        postingLine.append(termAndPosting[1]);
        Pair<String,StringBuilder> pair = new Pair<>(termAndPosting[0],postingLine);
        return pair;
    }



}
