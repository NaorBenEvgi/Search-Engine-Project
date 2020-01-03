package Searching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ranker {

    private HashMap<String,String[]> documentDetails;
    private HashMap<String,String> termsDF;
    private double averageDocumentLength;


    public Ranker(HashMap<String, String[]> finalDictionary, HashMap<String, String[]> documentDetails) {
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
        ArrayList<String> docs = new ArrayList<>(documentDetails.keySet());

        for(String doc : docs){
            lengthsSum += Integer.valueOf(documentDetails.get(doc)[3]);
        }
        averageDocumentLength = lengthsSum/corpusSize;
    }


    private void fillTermsDF(HashMap<String, String[]> finalDictionary){
        termsDF = new HashMap<>();
        ArrayList<String> terms = new ArrayList<>(documentDetails.keySet());

        for(String term : terms){
            termsDF.put(term,finalDictionary.get(term)[1]);
        }
    }

    public double rankByBM25(List<String> query, Integer docId, String pathToPostingFiles, boolean stem){
        int termFrequency, documentFrequency, documentLength;
        double numOfDocs = documentDetails.size(), idf, rank = 0, k = 1.2, b = 0.75, numerator, denominator;

        for(String term : query){
            termFrequency = Integer.valueOf(getTermFrequencyInDocument(term,docId,pathToPostingFiles,stem));
            documentFrequency = Integer.valueOf(termsDF.get(term));
            documentLength = Integer.valueOf(documentDetails.get(docId.toString())[3]);
            idf = log2(numOfDocs/documentFrequency);

            numerator = termFrequency*(k+1);
            denominator = termFrequency + k*(1-b+b*(documentLength/averageDocumentLength));

            rank += idf*(numerator/denominator);
        }

        return rank;
    }



    private String getTermFrequencyInDocument(String term, Integer docId, String pathToPostingFiles, boolean stem){
        char termInitial = term.charAt(0);
        File posting;
        if(Character.isLetter(termInitial)){
            if(stem){
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

            line = line.substring(line.indexOf(docId));
            return line.substring(line.indexOf("_")+1,line.indexOf("_")+2);

        } catch (Exception e){
           return "0";
        }
    }


    private double log2(double number){
        return (Math.log(number) / Math.log(2));
    }

    public HashMap<String, String[]> getDocumentDetails() {
        return documentDetails;
    }

    public void setDocumentDetails(HashMap<String, String[]> documentDetails) {
        this.documentDetails = documentDetails;
    }


    public void setFinalDictionary(HashMap<String, String[]> finalDictionary) {
        fillTermsDF(finalDictionary);
    }
}
