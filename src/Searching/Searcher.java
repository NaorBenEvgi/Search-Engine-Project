package Searching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;

public class Searcher {

    private static Ranker ranker;
    // private String corpusPath
    // private String targetPath


    public Searcher(SortedMap<String, String[]> finalDictionary, HashMap<Integer, String[]> documentDetails){
        ranker = new Ranker(finalDictionary,documentDetails);
    }


    public void runSingleQuery(ArrayList<String> query){





    }


    public void runMultipleQueries(HashMap<String,ArrayList<String>> queries){

    }

}
