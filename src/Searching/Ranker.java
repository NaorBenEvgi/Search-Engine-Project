package Searching;

import java.util.*;

/**
 * This class is responsible for ranking the retrieved documents of a query, and send only the highest ranked fifty documents.
 */
public class Ranker {

    private HashMap<String,String[]> documentDetails;
    private HashMap<String,String> termsDF;
    private double averageDocumentLength;

    /**
     * The Ranker's constructor. The object gets the final dictionary and the document details file, and computes the average length of a document in the corpus,
     * and fills the termsDF data structure.
     * @param finalDictionary the dictionary
     * @param documentDetails the document details file
     */
    public Ranker(SortedMap<String, String[]> finalDictionary, HashMap<String, String[]> documentDetails) {
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

    /**
     * Fills the data structure that contains the terms and their DF with the data from the final dictionary
     * @param finalDictionary the dictionary
     */
    private void fillTermsDF(SortedMap<String, String[]> finalDictionary){
        termsDF = new HashMap<>();
        ArrayList<String> terms = new ArrayList<>(finalDictionary.keySet());

        for(String term : terms){
            termsDF.put(term,finalDictionary.get(term)[1]);
        }
    }

    /**
     * Computes the similarity between a query and a document according to BM25.
     * @param query the query
     * @param docId the ID of the document
     * @param queryWordsTFPerDoc a data structure that contains the terms in the query and their frequencies in the documents they appear in
     * @return a rank by BM25 indice
     */
    private double rankByBM25(List<String> query, String docId, HashMap<String,Integer> queryWordsTFPerDoc){
        int termFrequency, documentFrequency;
        double numOfDocs = documentDetails.size(), idf, rank = 0, k = 1.2, b = 0.75, numerator, denominator;
        int documentLength = Integer.valueOf(documentDetails.get(docId)[3]);

        for(String term : query){
            if(queryWordsTFPerDoc.containsKey(term.toUpperCase())) {
                term = term.toUpperCase();
            }
            else if(queryWordsTFPerDoc.containsKey(term.toLowerCase())){
                term = term.toLowerCase();
            }
            termFrequency = queryWordsTFPerDoc.getOrDefault(term,0);

            if(termFrequency != 0) {
                documentFrequency = Integer.valueOf(termsDF.get(term));
                idf = log2(numOfDocs / documentFrequency);

                numerator = termFrequency * (k + 1);
                denominator = termFrequency + k * (1 - b + b * (documentLength / averageDocumentLength));

                rank += idf * (numerator / denominator);
            }
        }
        return rank;
    }


    /**
     * Computes the similarity between the query and a document according to the positions of the terms in the query in the document.
     * @param query the query
     * @param docId the ID of the document
     * @param queryPostingLines a list of the query's terms' lines from each matching posting file
     * @return the computed rank
     */
    private double rankByPosition(List<String> query, String docId, ArrayList<String> queryPostingLines){
        double rank = 0, sum = 0;
        boolean isEntity;
        ArrayList<Integer> allPositions = new ArrayList<>();
        double documentLength = Double.valueOf(documentDetails.get(docId)[3]);
        for(String word : query){
            isEntity = false;
            for(String postingLine : queryPostingLines) {
                if (word.equalsIgnoreCase(postingLine.substring(0, postingLine.indexOf("|")))) { //checks if the posting line matches the current term
                    if(postingLine.contains("_" + docId + ":") || postingLine.contains("|" + docId + ":" )){ //checks if the term appears in the document
                        if(word.toUpperCase().equals(postingLine.substring(0, postingLine.indexOf("|")))){ // an (or a part of) entity
                            isEntity = true;
                        }
                        if(postingLine.contains("_" + docId + ":")){
                            postingLine = postingLine.substring(postingLine.indexOf("_" + docId + ":") + docId.length() + 2);
                        }
                        else{ //in case the document is the first one in the term's posting line
                            postingLine = postingLine.substring(postingLine.indexOf("|" + docId + ":") + docId.length() + 2);
                        }
                        postingLine = postingLine.substring(0,postingLine.indexOf("_")); //trims the line only to the positions
                        String[] positions = postingLine.split(",");
                        for(int i=0; i<positions.length;i++){ //computes the rank
                            sum += (1-(Double.valueOf(positions[i]))/documentLength);
                            if(isEntity)
                                rank++;
                            allPositions.add(Integer.valueOf(positions[i]));
                        }
                        rank += sum / positions.length;

                        sum = 0;
                    }
                    break;
                }
            }
        }
        allPositions.sort(Integer::compareTo);
        int adjacent = 0;
        for(int i=0; i<allPositions.size()-1; i++){
            if(allPositions.get(i) == allPositions.get(i+1)-1){
                adjacent++;
            }
        }

        return rank + rank*adjacent*1.5;
    }


    /**
     * Extracts the term and its frequency in each document to a HashMap
     * @param queryPostingLines a list with the posting lines of the terms
     * @return a HashMap with a document ID as a key, and a term and its frequency as a value
     */
    private HashMap<String,HashMap<String,Integer>> computeTFForQueryWords(ArrayList<String> queryPostingLines) {
        HashMap<String,HashMap<String,Integer>> queryWordsTF = new HashMap<>();
        HashMap<String,HashMap<String,Integer>> queryWordsTFPerDoc = new HashMap<>();
        String docID,term;
        Integer tf;
        HashSet<String> docIDs = new HashSet<>();
        for(String postingLine : queryPostingLines){
            HashMap<String,Integer> frequenciesInDocuments = new HashMap<>();
            term = postingLine.substring(0,postingLine.indexOf("|"));
            postingLine = postingLine.substring(postingLine.indexOf("|")+1);
            while(!postingLine.equals("")) {
                docID = postingLine.substring(0, postingLine.indexOf(":"));
                docIDs.add(docID);
                postingLine = postingLine.substring(postingLine.indexOf("_") + 1);
                tf = Integer.valueOf(postingLine.substring(0, postingLine.indexOf("_")));
                postingLine = postingLine.substring(postingLine.indexOf("_") + 1);
                frequenciesInDocuments.put(docID,tf);
            }
            queryWordsTF.put(term,frequenciesInDocuments);
        }

        ArrayList<String> allTermsInQuery = new ArrayList<>(queryWordsTF.keySet());
        for(String doc : docIDs){
            HashMap<String,Integer> tfsInDoc = new HashMap<>();
            for(String termInQuery : allTermsInQuery){
                HashMap<String,Integer> termsTFInDoc = queryWordsTF.get(termInQuery);
                if(termsTFInDoc.containsKey(doc)){
                    tfsInDoc.put(termInQuery,termsTFInDoc.get(doc));
                }
            }
            queryWordsTFPerDoc.put(doc,tfsInDoc);
        }
        return queryWordsTFPerDoc;
    }


    /**
     * Ranks the similarity between a query and the documents that contain the terms in the query, and returns the 50 highest ranked documents.
     * @param queryPostingLines a list with the posting lines of the terms in the query
     * @param query a list with the terms in the query
     * @return he 50 highest ranked documents with their ranks
     */
    protected HashMap<String,Double> rank(ArrayList<String> queryPostingLines, ArrayList<String> query){
        HashMap<String,HashMap<String,Integer>> queryWordsTFPerDoc = computeTFForQueryWords(queryPostingLines);
        ArrayList<String> retrievedDocuments = new ArrayList<>(queryWordsTFPerDoc.keySet());
        HashMap<String,Double> rankedDocs = new HashMap<>();

        for(String doc : retrievedDocuments){
            HashMap<String,Integer> docTFs = queryWordsTFPerDoc.get(doc);
            double rank = 0.3*rankByBM25(query,doc,docTFs) + 0.7*rankByPosition(query,doc,queryPostingLines);// - rankByCosSim(query,doc,docTFs);
            rankedDocs.put(doc,rank);
        }
        rankedDocs = sortByValue(rankedDocs);
        ArrayList<String> docsAfterSort = new ArrayList<>(rankedDocs.keySet());
        HashMap<String,Double> docsToRetrieve = new HashMap<>();
        int i=0;
        for(String doc: docsAfterSort){
            docsToRetrieve.put(documentDetails.get(doc)[0],rankedDocs.get(doc));
            i++;
            if(i == 50)
                break;
        }
        return docsToRetrieve;
    }



    /**
     * Sorts a HashMap according to the values in it.
     * This function is taken from this page: https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
     * @param rankedDocs The HashMap that is being sorted
     * @return the HashMap sorted by value
     */
    public static HashMap<String, Double> sortByValue(HashMap<String, Double> rankedDocs)
    {
        // Creates a list from the elements of the HashMap
        List<Map.Entry<String, Double> > list =
                new LinkedList<Map.Entry<String, Double> >(rankedDocs.entrySet());

        // Sorts the list
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // puts the data from the sorted list into the HashMap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    /**
     * Computes the log in base 2 of a number
     * @param number the number
     * @return the log in base 2 of a number
     */
    private double log2(double number){
        return (Math.log(number) / Math.log(2));
    }

}
