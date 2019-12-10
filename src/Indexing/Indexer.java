package Indexing;

import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class Indexer {

    private HashMap<String,StringBuilder> postingLines;
    private int postingFilesCounter;
    private SortedMap<String,String[]> finalDictionary;

    public Indexer(){
        postingFilesCounter = 0;
        postingLines = new HashMap<>();
        finalDictionary = new TreeMap<>();
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
        Collections.sort(sortedTerms,String.CASE_INSENSITIVE_ORDER);
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
     * Merges two temporary posting files into one sorted posting file.
     * @param firstFilePath the path to the first file
     * @param secondFilePath the path to the second file
     * @param targetPath the path in which the merged file will be saved
     */
    public void mergePostingFiles(String firstFilePath, String secondFilePath, String targetPath){
        String mergedPostingFilePath = targetPath + "\\tempPostingFile" + postingFilesCounter;
        postingFilesCounter++;
        SortedMap<String,StringBuilder> mergedDictionary = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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


    /**
     *
     * @param firstFilePath
     * @param secondFilePath
     * @param targetPath
     */
    public void mergeAndSplitByLetter(String firstFilePath, String secondFilePath, String targetPath, boolean stem) {
        // create an 27 cells sized array, each cell contains list<String>
        // iterate through



            /*while((line = file2Reader.readLine()) != null){
                term = line.substring(0,line.indexOf("|"));
                sortedTerms.add(term);
            }
            sortedTerms.sort(String::compareTo);
            file1Reader = new BufferedReader(new FileReader(path1));
            file2Reader = new BufferedReader(new FileReader(path2));
            Iterator<String> sortedTermsIterator = sortedTerms.iterator();
            while(sortedTermsIterator.hasNext()){
                term = sortedTermsIterator.next();
                line1 = file1Reader.readLine();
                line2 = file2Reader.readLine();
                if(line1.startsWith(term)){
                    letterFileWriter.write(line1);
                    letterFileWriter.flush();
                    line1 = file1Reader.readLine();
                } else{
                    letterFileWriter.write(line2);
                    letterFileWriter.flush();
                    line2 = file1Reader.readLine();
                }*/


    }



    public void createTermsListByLetter(String firstFilePath, String secondFilePath, String targetPath, boolean stem) {
        BufferedReader file1Reader, file2Reader;
        boolean foundFirst = false;
        String term, line1, line2;
        StringBuilder contentToFile = new StringBuilder();
        HashMap<String,StringBuilder> sortedTerms = new HashMap<>();

        try {
            file1Reader = new BufferedReader(new FileReader(firstFilePath));
            file2Reader = new BufferedReader(new FileReader(secondFilePath));

//---------------------------------------------------- Numbers ------------------------------------------------------------

            while(!((line1 = file1Reader.readLine()).startsWith("a") || (line1 = file1Reader.readLine()).startsWith("A"))){
                StringBuilder lineBuilder = new StringBuilder();
                term = line1.substring(0,line1.indexOf("|"));
                if(!concatenateTerms(sortedTerms,term)){
                    lineBuilder.append(line1 + "\n");
                    sortedTerms.put(term,lineBuilder);
                }
            }
            while(!((line2 = file2Reader.readLine()).startsWith("a") || (line2 = file2Reader.readLine()).startsWith("A"))){
                StringBuilder lineBuilder = new StringBuilder();
                term = line2.substring(0,line2.indexOf("|"));
                if(!concatenateTerms(sortedTerms,term)){
                    lineBuilder.append(line2 + "\n");
                    sortedTerms.put(term,lineBuilder);
                }
            }
            //creates a sorted list with all the terms that start with a number and adds the frequent ones to the dictionary
            ArrayList<String> sortedTermsList = new ArrayList<>(sortedTerms.keySet());
            Collections.sort(sortedTermsList,String.CASE_INSENSITIVE_ORDER);
            for(String checkedNumTerm : sortedTermsList){
                if(!addTermToFinalDictionary(sortedTerms,checkedNumTerm,"NumPostingfile")){
                    sortedTermsList.remove(checkedNumTerm); //the term is too less frequent so we'll filter it out
                }else{ //the term was added to the dictionary so we'll add it to the content to be written to the file
                    contentToFile.append(sortedTerms.get(checkedNumTerm).toString() + "\n");
                }
            }
            sortedTerms.clear();
            writeContentToLetterFile(contentToFile,"NumPostingfile", targetPath, stem);
/*
            //iterates over the two files and fills a list with all the terms in them, and sorts the list after that
            while ((line = file1Reader.readLine()) != null) {
                if (line.startsWith("" + letter)) {
                    foundFirst = true;
                } else {
                    if (foundFirst) {
                        break;
                    } else {
                        continue;
                    }
                }

                term = line.substring(0, line.indexOf("|"));


                sortedTerms.add(term);

            }*/
        } catch (Exception e) {
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
            fileWriter = new BufferedWriter(new FileWriter(targetPath + "/" + postingFileName));
            fileWriter.write(content.toString());
            fileWriter.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     *
     * @param terms
     * @param line
     * @return
     */
    private boolean concatenateTerms(HashMap<String,StringBuilder> terms,String line){
        String term = line.substring(0,line.indexOf("|"));
        if(terms.containsKey(term)){
            StringBuilder lineBuilder = terms.get(term);
            String postingLine = line.substring(line.indexOf("|")+1);
            lineBuilder.append(postingLine);
            terms.put(term,lineBuilder);
            return true;
        }
        return false;
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
        if(sumOfTfTerm < 10){
            return false;
        }
        //In case the term is common enough, we collect its details into the final dictionary
        termDetails[0] = sumOfTfTerm + ""; //how many times the term appears in the corpus
        termDetails[1] = dfTerm + ""; //in how many documents the term appears
        termDetails[2] = postingFileName;
        termDetails[3] = postingLineWithTerm.toString().getBytes().length + ""; //the posting line size in memory
        finalDictionary.put(term,termDetails);

        return true;
    }
}
