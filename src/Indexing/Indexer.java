package Indexing;

import javafx.util.Pair;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class is responsible for creating posting files and a dictionary for the documents in the corpus.
 */
public class Indexer {

    private HashMap<String,StringBuilder> postingLines;
    private int postingFilesCounter;
    private SortedMap<String,String[]> finalDictionary;
    private String BY_VERTICLE_BAR = "\\|";
    private HashMap<Integer,String[]> documentDetails;
    public Indexer(){
        postingFilesCounter = 0;
        postingLines = new HashMap<>();
        finalDictionary = new TreeMap<>();
        documentDetails = new HashMap<>();
    }


    /**
     * Iterates over a dictionary that contains all the terms in a given document, merges each term's posting file lines into
     * one StringBuilder, and saves it in the posting lines HashMap
     * @param documentDictionary the HashMap of all the terms in the document
     * @param doc the given document
     */
    public void collectTermPostingLines(HashMap<String, Term> documentDictionary, Article doc){
        int maxTF = 0, docLength  = 0;
        for(String term : documentDictionary.keySet()){
            int termFrequency = documentDictionary.get(term).getTermFrequency(doc);
            docLength += termFrequency; // summing the amount of terms in the document in order to compute its length
            if(termFrequency > maxTF){
                maxTF = termFrequency;
            }
            if(postingLines.containsKey(term)){
                String correctTerm = removeDuplicatesTermsIndexer(documentDictionary.get(term).getTerm());
                String currentTermInDic = postingLines.get(term).toString().substring(0,postingLines.get(term).toString().indexOf("|"));
                if(!correctTerm.equals(currentTermInDic)){
                    String tempPL = postingLines.get(term).toString();
                    tempPL.replace(currentTermInDic,correctTerm);
                    StringBuilder postingLineAfterReplaceTermAndConcat = new StringBuilder(tempPL).append(documentDictionary.get(term).getPostingLineInDoc(doc));
                    postingLines.put(term, postingLineAfterReplaceTermAndConcat);
                }else{
                    postingLines.get(term).append(documentDictionary.get(term).getPostingLineInDoc(doc));
                }
            }else{
                postingLines.put(term, new StringBuilder(documentDictionary.get(term).getTerm()).append("|").append(documentDictionary.get(term).getPostingLineInDoc(doc)));
            }
        }
        String[] details = new String[4];
        details[0] = doc.getDocId();
        details[1] = String.valueOf(maxTF);
        details[2] = String.valueOf(documentDictionary.size());
        details[3] = String.valueOf(docLength);
        documentDetails.put(doc.getDocNum(),details);
    }


    /**
     *
     * @param word
     * @return
     */
    private String removeDuplicatesTermsIndexer(String word){
        if(Character.isDigit(word.charAt(0))){
            return word;
        }
        String wordInLower = word.toLowerCase();
        String postingLine = postingLines.get(wordInLower).toString();
        String termInPostingLines = postingLines.get(wordInLower).substring(0,postingLine.indexOf("|"));


        if(Character.isLowerCase(word.charAt(0))) {
            return word;
        }
        return termInPostingLines;

    }

    /**
     * Writes the terms that are currently stored in the class's HashMap of posting lines into a temporary posting file,
     * and removes them from the HashMap.
     * @param path the path of the directory in which the temporary file will be saved
     */
    public void createTemporaryPosting(String path){
        ArrayList<String> sortedTerms = new ArrayList<>(postingLines.keySet());
        sortedTerms.sort(String.CASE_INSENSITIVE_ORDER);
        StringBuilder temporaryPostingLinesBuilder = new StringBuilder();
        //prepares the lines to be written in the file, and removes them from the HashMap
        for(String term: sortedTerms){
            temporaryPostingLinesBuilder
                    .append(postingLines.get(term).toString())
                    .append("\n");
        }
        postingLines = new HashMap<>(); // Clear the posting Lines (More effective then clear because the garbage collector will free the memory)
        String pathToTemporaryFile = Paths.get(path, String.valueOf(postingFilesCounter)).toString() + ".txt";
        writePostingLinesToTempFile(pathToTemporaryFile,temporaryPostingLinesBuilder.toString());
        postingFilesCounter++;
    }



    /**
     * Given a path and content that contains the posting lines, the function writes the content into a file it creates
     * and saves it in the given path
     * @param path the given path to save the file in
     * @param content the content of the file
     */
    private void writePostingLinesToTempFile(String path, String content){
        File file = new File(path);
        BufferedWriter postingLinesWriter = null;
        try{
            postingLinesWriter = new BufferedWriter(new FileWriter(file,true));
            postingLinesWriter.append(content);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (postingLinesWriter != null) {
                    postingLinesWriter.close();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Merges two temporary posting files into one sorted posting file.
     * @param firstFilePath the path to the first file
     * @param secondFilePath the path to the second file
     * @param targetPath the path in which the merged file will be saved
     */
    public void mergePostingFiles(String firstFilePath, String secondFilePath, String targetPath){
        String mergedPostingFilePath = targetPath + "\\tempPostingFile" + postingFilesCounter + ".txt";
        postingFilesCounter++;
        SortedMap<String,StringBuilder> mergedDictionary = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        BufferedReader postingFile1,postingFile2;
        try {
            //reads the first file and puts the terms and their lines in a HashMap
            postingFile1 = new BufferedReader(new FileReader(firstFilePath));
            ArrayList<String> linesInFile1 = new ArrayList<>();
            String tempLine;
            while((tempLine = postingFile1.readLine()) != null){
                linesInFile1.add(tempLine);
            }
            for (String item : linesInFile1) {
                Pair<String, StringBuilder> mapEntry = convertLineToTermAndPosting(item);
                mergedDictionary.put(mapEntry.getKey(), mapEntry.getValue());
            }

            //reads the second file and puts the terms and their lines in a HashMap
            postingFile2 = new BufferedReader(new FileReader(secondFilePath));
            ArrayList<String> linesInFile2 = new ArrayList<>();
            while((tempLine = postingFile2.readLine()) != null){
                linesInFile2.add(tempLine);
            }
            for (String value : linesInFile2) {
                Pair<String, StringBuilder> mapEntry = convertLineToTermAndPosting(value);
                String mapKey = mapEntry.getKey();
                StringBuilder mapValue = mapEntry.getValue();
                if(Character.isDigit(mapKey.charAt(0))){
                    if(mergedDictionary.containsKey(mapKey)){
                        mergedDictionary.get(mapKey).append(mapValue);
                    }
                    else{
                        mergedDictionary.put(mapKey, mapValue);
                    }
                    continue;
                }
                //merges terms that already appeared in the HashMap, and regularly adds the rest
                if (mergedDictionary.containsKey(mapKey)) {
                    mergedDictionary.get(mapKey).append(mapValue);
                    //Checks what kind of term we take - lower or upper case
                    if (Character.isLowerCase(mapKey.charAt(0))) { // the term we are checking is written with small letters
                        StringBuilder updatedPostingLine = mergedDictionary.get(mapKey);
                        mergedDictionary.remove(mapKey);
                        mergedDictionary.put(mapKey,updatedPostingLine);
                    } else {
                        continue;
                    }
                }
                else {
                    mergedDictionary.put(mapKey, mapValue);
                }
            }

            //creates the content (the posting lines) in a lexicographical order and writes it in a new file
            StringBuilder fileContent = new StringBuilder();
            for (String s : mergedDictionary.keySet()) {
                fileContent.append(s).append("|").append(mergedDictionary.get(s)).append("\n");
                if(fileContent.length() >= 100000000){
                    writePostingLinesToTempFile(mergedPostingFilePath,fileContent.toString());
                    fileContent = new StringBuilder();
                }
            }
            if(fileContent.length() > 0){
                writePostingLinesToTempFile(mergedPostingFilePath,fileContent.toString());
                fileContent = new StringBuilder();
            }
            postingFile1.close();
            postingFile2.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Converts a line from a posting file into a pair of the term and the rest of the line.
     * The term is the key in the pair, and the line (without the term in it) is the value (stored in a StringBuilder).
     * @param line a line from a temporary posting file, that contains the term and its details in the corpus
     * @return a pair of the term and the line
     */
    private Pair<String,StringBuilder> convertLineToTermAndPosting(String line){
        String[] termAndPosting = line.split(BY_VERTICLE_BAR); //[0] contains the term, [1] contains the rest of the posting line
        StringBuilder postingLine = new StringBuilder();
        postingLine.append(termAndPosting[1]);
        return new Pair<>(termAndPosting[0],postingLine);
    }


    /**
     * Iterates over two final temporary posting files and splits them in an ordered way into 27 final posting files, one for each letter
     * and one for numbers. All of these files will be saves in a given directory path, and there will be an indication of whether the terms
     * have gone through stemming operations or not.
     * @param firstFilePath the path of the first posting file
     * @param secondFilePath the path of the second posting file
     * @param targetPath the path of the directory in which the posting files will be saved
     * @param stem an indicator of whether the terms have gone through stemming or not
     */
    public void createTermsListByLetter(String firstFilePath, String secondFilePath, String targetPath, boolean stem){
        BufferedReader file1Reader, file2Reader;
        String term, lastLine1, lastLine2;
        String innerTargetPath;
        StringBuilder contentToFile = new StringBuilder();
        StringBuilder lineBuilder;
        HashMap<String,StringBuilder> sortedTerms = new HashMap<>();

        if(stem){
            innerTargetPath = Paths.get(targetPath).resolve("indexStem").toString();
        }
        else{
            innerTargetPath = Paths.get(targetPath).resolve("index").toString();
        }
        new File(innerTargetPath).mkdir();

        try {
            file1Reader = new BufferedReader(new FileReader(firstFilePath));
            file2Reader = new BufferedReader(new FileReader(secondFilePath));

//---------------------------------------------------- Numbers ------------------------------------------------------------
            lastLine1 = file1Reader.readLine();
            while(!(lastLine1.startsWith("a") || lastLine1.startsWith("A"))){
                lineBuilder = new StringBuilder();
                term = lastLine1.substring(0,lastLine1.indexOf("|"));
                lineBuilder.append(lastLine1);
                sortedTerms.put(term,lineBuilder);
                lastLine1 = file1Reader.readLine();
            }
            lastLine2 = file2Reader.readLine();
            while(!(lastLine2.startsWith("a") || lastLine2.startsWith("A"))){
                lineBuilder = new StringBuilder();
                term = lastLine2.substring(0,lastLine2.indexOf("|"));
                if(!concatenateTerms(sortedTerms,lastLine2)){
                    lineBuilder.append(lastLine2);
                    sortedTerms.put(term,lineBuilder);
                }
                lastLine2 = file2Reader.readLine();
            }
            //creates a sorted list with all the terms that start with a number and adds the frequent ones to the dictionary
            ArrayList<String> sortedTermsList = new ArrayList<>(sortedTerms.keySet());
            sortedTermsList.sort(String.CASE_INSENSITIVE_ORDER);
            for(String checkedNumTerm : sortedTermsList){
                if(!addTermToFinalDictionary(sortedTerms,checkedNumTerm,"NumPostingFile")){
                    //sortedTermsList.remove(checkedNumTerm); //the term is too less frequent so we'll filter it out
                }else{ //the term was added to the dictionary so we'll add it to the content to be written to the file
                    contentToFile.append(sortedTerms.get(checkedNumTerm).toString()).append("\n");
                }
            }
            sortedTerms.clear();
            sortedTermsList.clear();
            writeContentToLetterFile(contentToFile,"NumPostingFile", innerTargetPath, stem);
            contentToFile = new StringBuilder();

//---------------------------------------------------- Letters ------------------------------------------------------------
            char[] letters = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
            boolean startsCorrectly1,startsCorrectly2;
            for(int i=0; i<letters.length; i++){
                // appending the current lines in lastLine1 and lastLine2 since they already contain lines with a matching letter from each file
                startsCorrectly1 = true;
                startsCorrectly2 = true;

                //in case the reading of one or both of the files has finished, or if there is no match between the
                //letter in the array and the letter that any of the lines begins with
                if(lastLine1 == null || !(lastLine1.startsWith((""+letters[i]).toLowerCase()) || lastLine1.startsWith(""+letters[i]))){
                    startsCorrectly1 = false;
                }
                if(lastLine2 == null || !(lastLine2.startsWith((""+letters[i]).toLowerCase()) || lastLine2.startsWith(""+letters[i]))){
                    startsCorrectly2 = false;
                }

                if(!startsCorrectly1 && !startsCorrectly2)
                    continue;

                if(startsCorrectly1){
                    lineBuilder = new StringBuilder();
                    term = lastLine1.substring(0,lastLine1.indexOf("|"));
                    lineBuilder.append(lastLine1);
                    sortedTerms.put(term,lineBuilder);
                }
                if(startsCorrectly2){
                    term = lastLine2.substring(0,lastLine2.indexOf("|"));
                    if(!concatenateTerms(sortedTerms,lastLine2)){
                        lineBuilder = new StringBuilder();
                        lineBuilder.append(lastLine2);
                        sortedTerms.put(term,lineBuilder);
                    }
                }

                if(startsCorrectly1) {
                    while (lastLine1 != null && (lastLine1.startsWith(("" + letters[i]).toLowerCase()) || lastLine1.startsWith("" + letters[i]))) {
                        lineBuilder = new StringBuilder();
                        term = lastLine1.substring(0, lastLine1.indexOf("|"));
                        lineBuilder.append(lastLine1);
                        sortedTerms.put(term, lineBuilder);
                        lastLine1 = file1Reader.readLine();
                    }
                }
                if(startsCorrectly2) {
                    while (lastLine2 != null && (lastLine2.startsWith(("" + letters[i]).toLowerCase()) || lastLine2.startsWith("" + letters[i]))) {
                        lineBuilder = new StringBuilder();
                        term = lastLine2.substring(0, lastLine2.indexOf("|"));
                        if (!concatenateTerms(sortedTerms, lastLine2)) {
                            lineBuilder.append(lastLine2);
                            sortedTerms.put(term, lineBuilder);
                        }
                        lastLine2 = file2Reader.readLine();
                    }
                }

                sortedTermsList = new ArrayList<>(sortedTerms.keySet());
                sortedTermsList.sort(String.CASE_INSENSITIVE_ORDER);
                for(String checkedTerm : sortedTermsList){
                    if(addTermToFinalDictionary(sortedTerms,checkedTerm,letters[i] + "PostingFile")){
                        contentToFile.append(sortedTerms.get(checkedTerm).toString()).append("\n");
                    }
                }
                sortedTermsList.clear();
                sortedTerms.clear();
                writeContentToLetterFile(contentToFile,letters[i] + "PostingFile",innerTargetPath,stem);
                contentToFile = new StringBuilder();
            }
            file1Reader.close();
            file2Reader.close();

            extractDictionaryToFile(innerTargetPath,stem);
            extractDocumentDetailsToFile(innerTargetPath,stem);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Writes a given content to a file
     * @param content the given content
     * @param postingFileName the name of the posting file
     * @param targetPath the directory in which the file will be saved
     * @param stem determines if the content includes stemming
     */
    private void writeContentToLetterFile(StringBuilder content, String postingFileName, String targetPath, boolean stem){
        BufferedWriter fileWriter;
        if(stem)
            postingFileName += "Stem";
        try{
            fileWriter = new BufferedWriter(new FileWriter(targetPath + "/" + postingFileName + ".txt"));
            fileWriter.write(content.toString());
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * Extracts the term from the given posting line, and checks if the given HashMap of terms already contains it.
     * In case it does, the given posting line is concatenated into the posting line that is already stored in the HashMap, and returns true.
     * In case it does not exist, the function returns false.
     * @param terms the HashMap of terms
     * @param line the line that contains a possibly existing term
     * @return true if the term exists in the HashMap, false otherwise
     */
    private boolean concatenateTerms(HashMap<String,StringBuilder> terms,String line){
        String term = line.substring(0,line.indexOf("|"));
        StringBuilder lineBuilder;
        //------------------------- Numbers ---------------------------------
        if(Character.isDigit(term.charAt(0))){
            if(!terms.containsKey(term)){
                return false;
            }
            String postingLine = line.substring(line.indexOf("|")+1);
            lineBuilder = terms.get(term);
            lineBuilder.append(postingLine);
            terms.put(term,lineBuilder);
            return true;
        }

        //------------------------- Words -----------------------------------
        String termLower = term.toLowerCase(), termUpper = term.toUpperCase();
        if(!((terms.containsKey(termLower)) || terms.containsKey(termUpper))) {
            return false;
        }
        if(terms.containsKey(termLower)){
            lineBuilder = terms.get(termLower);
            String postingLine = line.substring(line.indexOf("|")+1);
            lineBuilder.append(postingLine);
            terms.put(termLower,lineBuilder);
        }else{
            lineBuilder = terms.get(termUpper);
            String postingLine = lineBuilder.toString().substring(line.indexOf("|")+1);
            lineBuilder = (new StringBuilder()).append(line).append(postingLine);
            terms.put(term,lineBuilder);
        }
        return true;
    }


    /**
     * Collects all the details in the corpus about a specific term and adds it to the final dictionary,
     * unless the term appears less than 10 times in the whole corpus
     * @param sortedTerms dictionary of the terms and their posting lines
     * @param term the checked term
     * @param postingFileName the name of the posting file it will be saved in
     * @return true if the term was added to the dictionary, false otherwise
     */
    private boolean addTermToFinalDictionary(HashMap<String,StringBuilder> sortedTerms, String term, String postingFileName){
        StringBuilder postingLineWithTerm = sortedTerms.get(term);
        String postingLine = postingLineWithTerm.substring(postingLineWithTerm.toString().indexOf("|")+1);
        String[] termDetails = new String[4];
        String[] tfSum = postingLine.split("_");
        int sumOfTfTerm = 0, dfTerm=0;

        //Check how many times the term appears in the corpus and in how many documents
        for(int i=1; i<tfSum.length; i+=2){
            sumOfTfTerm += Integer.parseInt(tfSum[i]);
            dfTerm++;
        }

        //If the term appears less than 10 times in the corpus we filter it out
       /* if(sumOfTfTerm < 3){
            return false;
        }*/
        //In case the term is common enough, we collect its details into the final dictionary
        termDetails[0] = String.valueOf(sumOfTfTerm); //how many times the term appears in the corpus
        termDetails[1] = String.valueOf(dfTerm); //how many documents the term appears in
  /*      termDetails[2] = postingFileName;*/
        termDetails[2] = String.valueOf(postingLineWithTerm.toString().getBytes().length); //the posting line size in memory
        finalDictionary.put(term,termDetails);

        return true;
    }

    /**
     * Returns the final dictionary that was created during the indexing.
     * @return the final dictionary
     */
    public SortedMap<String,String[]> getDictionary(){
        return this.finalDictionary;
    }

    /**
     * Returns the HashMap that contains all the docIds and the details about each document
     * @return the documents details HashMap
     */
    public HashMap<Integer,String[]> getDocumentDetails(){
        return documentDetails;
    }


    /**
     *
     * @param path
     * @param stem
     */
    private void extractDictionaryToFile(String path, boolean stem){
        ArrayList<String> terms = new ArrayList<>(finalDictionary.keySet());
        StringBuilder dictionaryContent = new StringBuilder();
        String totalTF, documentFrequency, postingFileName, sizeOfPostingLine;
        String[] termDetails;
        Path pathToFinalDictionary;
        if(stem)
            pathToFinalDictionary = Paths.get(path).resolve("finalDictionaryStem.txt");
        else
            pathToFinalDictionary = Paths.get(path).resolve("finalDictionary.txt");

        for(String term : terms){
            termDetails = finalDictionary.get(term);
            totalTF = termDetails[0];
            documentFrequency = termDetails[1];
           /* postingFileName = termDetails[2];*/
            sizeOfPostingLine = termDetails[2];
            dictionaryContent.append(term).append("_" + totalTF).append("_" + documentFrequency).append("_" + sizeOfPostingLine).append("\n");
            if(dictionaryContent.length() >= 100000000){
                writePostingLinesToTempFile(pathToFinalDictionary.toString(),dictionaryContent.toString());
                dictionaryContent = new StringBuilder();
            }
        }
        if(dictionaryContent.length() > 0){
            writePostingLinesToTempFile(pathToFinalDictionary.toString(),dictionaryContent.toString());
            dictionaryContent = new StringBuilder();
        }

    }


    /**
     *
     * @param path
     * @param stem
     */
    private void extractDocumentDetailsToFile(String path, boolean stem){
        ArrayList<Integer> docs = new ArrayList<>(documentDetails.keySet());
        StringBuilder documentDetailsContent = new StringBuilder();
        Path pathToDocumentDetails;
        String[] docDetails;
        String docName, maxTF, uniqueTerms, docLength;

        if(stem)
            pathToDocumentDetails = Paths.get(path).resolve("documentDetailsStem.txt");
        else
            pathToDocumentDetails = Paths.get(path).resolve("documentDetails.txt");

        for(Integer id : docs){
            docDetails = documentDetails.get(id);
            docName = docDetails[0];
            maxTF = docDetails[1];
            uniqueTerms = docDetails[2];
            docLength = docDetails[3];
            documentDetailsContent.append(id).append("_" + docName).append("_" + maxTF).append("_" + uniqueTerms).append("_" + docLength).append("\n");
            if(documentDetailsContent.length() >= 100000000){
                writePostingLinesToTempFile(pathToDocumentDetails.toString(),documentDetailsContent.toString());
                documentDetailsContent = new StringBuilder();
            }
        }
        if(documentDetailsContent.length() > 0){
            writePostingLinesToTempFile(pathToDocumentDetails.toString(),documentDetailsContent.toString());
            documentDetailsContent = new StringBuilder();
        }
    }
}
