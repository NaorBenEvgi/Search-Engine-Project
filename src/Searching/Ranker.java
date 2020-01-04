package Searching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;


public class Ranker {

    private HashMap<Integer,String[]> documentDetails;
    private HashMap<String,String> termsDF;
    private double averageDocumentLength;


    public Ranker(SortedMap<String, String[]> finalDictionary, HashMap<Integer, String[]> documentDetails) {
        this.documentDetails = documentDetails;
        fillTermsDF(finalDictionary);
        computeAverageDocumentLength();
    }


    /**
     * Computes the average length of a document in the corpus.
     */
    private void computeAverageDocumentLength(){
        double corpusSize = documentDetails.size();
        double lengthsSum = 0;
        ArrayList<Integer> docs = new ArrayList<>(documentDetails.keySet());

        for(Integer doc : docs){
            lengthsSum += Integer.valueOf(documentDetails.get(doc)[3]);
        }
        averageDocumentLength = lengthsSum/corpusSize;
    }


    private void fillTermsDF(SortedMap<String, String[]> finalDictionary){
        termsDF = new HashMap<>();
        ArrayList<String> terms = new ArrayList<>(finalDictionary.keySet());

        for(String term : terms){
            termsDF.put(term,finalDictionary.get(term)[1]);
        }
    }


    public double rankByBM25(List<String> query, Integer docId, String pathToPostingFiles, boolean stem){
        int termFrequency, documentFrequency;
        double numOfDocs = documentDetails.size(), idf, rank = 0, k = 1.2, b = 0.75, numerator, denominator;
        int documentLength = Integer.valueOf(documentDetails.get(docId.toString())[3]);

        for(String term : query){
            termFrequency = Integer.valueOf(getTermFrequencyInDocument(term,docId,pathToPostingFiles,stem));
            documentFrequency = Integer.valueOf(termsDF.get(term));
            //TODO: check if this is the right computation for idf
            idf = Math.log10(numOfDocs/documentFrequency);

            numerator = termFrequency*(k+1);
            denominator = termFrequency + k*(1-b+b*(documentLength/averageDocumentLength));

            rank += idf*(numerator/denominator);
        }

        return rank;
    }


    //TODO: improve efficiency?
    private String getTermFrequencyInDocument(String term, Integer docId, String pathToPostingFiles, boolean stem){
        char termInitial = term.toUpperCase().charAt(0);
        File posting;
        if(Character.isLetter(termInitial)){
            if(stem){
                //TODO: fix the path - need to find the right inner directory before approaching the file
                posting = new File(Paths.get(pathToPostingFiles).resolve(termInitial + "PostingFileStem").toString());
            }
            else{
                posting = new File(Paths.get(pathToPostingFiles).resolve(termInitial + "PostingFile").toString());
            }
        }
        else{
            if(stem){
                posting = new File(Paths.get(pathToPostingFiles).resolve("NumPostingFileStem").toString());
            }
            else{
                posting = new File(Paths.get(pathToPostingFiles).resolve("NumPostingFile").toString());
            }
        }

        BufferedReader reader;
        String line;
        try{
            reader = new BufferedReader(new FileReader(posting));
            line = reader.readLine();
            while(!line.startsWith(term)){
                line = reader.readLine();
            }

            reader.close();
            if(!line.contains("_" + docId + ":") && !line.contains("|" + docId + ":"))
                return "0";

            line = line.substring(line.indexOf(docId + ":"));
            line = line.substring(line.indexOf("_")+1);
            return line.substring(0,line.indexOf("_"));

        } catch (Exception e){
           return "0";
        }
    }


    // might be unnecessary
    /*
    private double log2(double number){
        return (Math.log(number) / Math.log(2));
    }*/


    public HashMap<Integer, String[]> getDocumentDetails() {
        return documentDetails;
    }

    public void setDocumentDetails(HashMap<Integer, String[]> documentDetails) {
        this.documentDetails = documentDetails;
    }


    public void setFinalDictionary(SortedMap<String, String[]> finalDictionary) {
        fillTermsDF(finalDictionary);
    }
}
