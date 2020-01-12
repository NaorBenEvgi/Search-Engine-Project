package Indexing;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class represents a term. It contains a string of the term itself,
 * and a data structure that contains all the documents in which the terms appeared, and in which positions in it.
 */
public class Term {

    private String term;
    private HashMap<Article, ArrayList<Integer>> termPositions;
    private boolean isEntity;

    public Term(String term) {
        this.term = term;
        termPositions = new HashMap<>();
        isEntity = false;
    }

    /**
     * Creates a posting line of the term, the documents it appeared in, the positions for each documents and the term's frequency for each document.
     * @param doc the document to create the posting line for
     * @return the posting line
     */
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

    /**
     * Getter for the term's posting line
     * @param doc the document for which to create the posting line
     * @return the posting line for the term in the document
     */
    public String getPostingLineInDoc(Article doc){
        return lineInPostingFile(doc);
    }


    /**
     * Adds the position of the term in the document to the data structure of the term.
     * @param doc the given document
     * @param position the position of the term in the document
     */
    public void addPositionInDoc(Article doc, int position) {
        ArrayList<Integer> positionsArray;
        if (!termPositions.containsKey(doc)) { // in case this is the first instance of this term in the document
            positionsArray = new ArrayList<>();
            positionsArray.add(new Integer(position));
            termPositions.put(doc, positionsArray);
        }
        else { // in case we have already seen this term in the document
            positionsArray = termPositions.get(doc);
            positionsArray.add(new Integer(position));
            termPositions.put(doc, positionsArray);
        }
    }

    /**
     * Returns the amount of times the term appeared in the document.
     * @param doc the given document
     * @return the amount of times the term appeared in the document
     */
    public int getTermFrequency(Article doc) {
        if (termPositions.containsKey(doc)) {
            return termPositions.get(doc).size();
        }
        return 0;
    }

    /**
     * Returns the term.
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * Assigns the term with the given string
     * @param term the given string
     */
    public void setTerm(String term) {
        this.term = term;
    }


    public void setEntity(){
        isEntity = true;
    }

    public boolean isEntity(){
        return isEntity;
    }

}