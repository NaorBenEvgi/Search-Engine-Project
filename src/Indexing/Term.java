package Indexing;

import java.util.ArrayList;
import java.util.HashMap;

public class Term {

    private String term;
    private HashMap<Article, ArrayList<Integer>> termPositions;

    public Term(String term) {
        this.term = term;
        termPositions = new HashMap<>();
    }

    //will be used in the indexer
    private String lineInPostingFile(Article doc) {
        StringBuilder line = new StringBuilder();
        if (termPositions.containsKey(doc)) {
            line.append(doc.getDocNum()).append(":");
            ArrayList<Integer> positionsForCurrentDoc = termPositions.get(doc);
            int positionsForCurrentDocSize = positionsForCurrentDoc.size();
            if(positionsForCurrentDocSize > 0){
                line.append(positionsForCurrentDoc.get(0));
                for (int i = 1; i < positionsForCurrentDocSize; i++) {
                    line.append(",");
                    line.append(positionsForCurrentDoc.get(i));
                }
                line.append("_").append(positionsForCurrentDocSize).append("_");
            }
        }
        return line.toString();
    }


    public String getPostingLineInDoc(Article doc){
        return lineInPostingFile(doc);
    }


    public void addPositionInDoc(Article doc, int position) {
        ArrayList<Integer> positionsArray;
        if (!termPositions.containsKey(doc)) { // in case this is the first instance of this term in the document
            positionsArray = new ArrayList<>();
            positionsArray.add(new Integer(position));
            termPositions.put(doc, positionsArray);
        } else { // in case we have already seen this term in the document
            positionsArray = termPositions.get(doc);
            positionsArray.add(new Integer(position));
            termPositions.put(doc, positionsArray);
        }
    }


    public int getTermFrequency(Article doc) {
        if (termPositions.containsKey(doc)) {
            return termPositions.get(doc).size();
        }
        return 0;
    }


    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

}