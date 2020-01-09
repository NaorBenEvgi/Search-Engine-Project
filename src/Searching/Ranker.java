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



    private double rankByPosition(){




        return 0;
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



    protected void rank(ArrayList<String> queryPostingLines, ArrayList<String> query){
        HashMap<String,HashMap<String,Integer>> queryWordsTFPerDoc = computeTFForQueryWords(queryPostingLines);
        ArrayList<String> retrievedDocuments = new ArrayList<>(queryWordsTFPerDoc.keySet());
        HashMap<String,Double> rankedDocs = new HashMap<>();

        for(String doc : retrievedDocuments){
            double rank = rankByBM25(query,doc,queryWordsTFPerDoc.get(doc));
            rankedDocs.put(doc,rank);
        }
        rankedDocs = sortByValue(rankedDocs);

        //TODO: return the highest ranked 50 docs
    }




    /**
     * Taken from here: https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
     * @param rankedDocs
     * @return
     */
    public HashMap<String, Double> sortByValue(HashMap<String, Double> rankedDocs)
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


    public void setFinalDictionary(SortedMap<String, String[]> finalDictionary) {
        fillTermsDF(finalDictionary);
    }
}
