package Searching;

import java.util.*;


public class Ranker {

    private HashMap<String,String[]> documentDetails;
    private HashMap<String,String> termsDF;
    private double averageDocumentLength;


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


    private void fillTermsDF(SortedMap<String, String[]> finalDictionary){
        termsDF = new HashMap<>();
        ArrayList<String> terms = new ArrayList<>(finalDictionary.keySet());

        for(String term : terms){
            termsDF.put(term,finalDictionary.get(term)[1]);
        }
    }


    private double rankByBM25(List<String> query, String docId, HashMap<String,Integer> queryWordsTFPerDoc){
        int termFrequency, documentFrequency;
        double numOfDocs = documentDetails.size(), idf, rank = 0, k = 1.2, b = 0.75, numerator, denominator;
        int documentLength = Integer.valueOf(documentDetails.get(docId)[3]);

        for(String term : query){
            termFrequency = queryWordsTFPerDoc.getOrDefault(term,0);

            if(termFrequency != 0) {
                documentFrequency = Integer.valueOf(termsDF.get(term));
                //TODO: check if this is the right computation for idf
                idf = Math.log10(numOfDocs / documentFrequency);

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
        double rank = 0;
        for(String word : query){
            for(String postingLine : queryPostingLines) {
                if (word.equalsIgnoreCase(postingLine.substring(0, postingLine.indexOf("|")))) { //checks if the posting line matches the current term
                    if(postingLine.contains("_" + docId + ":") || postingLine.contains("|" + docId + ":" )){ //checks if the term appears in the document
                        if(postingLine.contains("_" + docId + ":")){
                            postingLine = postingLine.substring(postingLine.indexOf("_" + docId + ":") + docId.length() + 2);
                        }
                        else{ //in case the document is the first one in the term's posting line
                            postingLine = postingLine.substring(postingLine.indexOf("|" + docId + ":") + docId.length() + 2);
                        }
                        postingLine = postingLine.substring(0,postingLine.indexOf("_")); //trims the line only to the positions
                        double documentLength = Double.valueOf(documentDetails.get(docId)[3]);
                        String[] positions = postingLine.split(",");
                        for(int i=0; i<positions.length;i++){ //computes the rank
                            rank += (1-Double.valueOf(positions[i])/documentLength)/positions.length;
                        }
                    }
                    break;
                }
            }
        }
        return rank;
    }



    /**
     *
     * @param queryPostingLines
     * @return
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
     *
     * @param queryPostingLines
     * @param query
     * @return
     */
    protected HashMap<String,Double> rank(ArrayList<String> queryPostingLines, ArrayList<String> query){
        HashMap<String,HashMap<String,Integer>> queryWordsTFPerDoc = computeTFForQueryWords(queryPostingLines);
        ArrayList<String> retrievedDocuments = new ArrayList<>(queryWordsTFPerDoc.keySet());
        HashMap<String,Double> rankedDocs = new HashMap<>();

        for(String doc : retrievedDocuments){
            double rank = rankByBM25(query,doc,queryWordsTFPerDoc.get(doc)) + rankByPosition(query,doc,queryPostingLines);
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
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double> > list =
                new LinkedList<Map.Entry<String, Double> >(rankedDocs.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }


    // might be unnecessary
    /*
    private double log2(double number){
        return (Math.log(number) / Math.log(2));
    }*/


    public HashMap<String, String[]> getDocumentDetails() {
        return documentDetails;
    }

    public void setDocumentDetails(HashMap<String, String[]> documentDetails) {
        this.documentDetails = documentDetails;
    }

}
