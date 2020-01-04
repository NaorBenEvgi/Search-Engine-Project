package GUI;

import Indexing.*;
import Searching.Searcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class is a controller that runs the search engine behind the scenes, following the commands sent by the user-interface.
 */
public class Controller extends Observable{

    private Indexer indexer;
    private ReadFile corpusReader;
    private SortedMap<String,String[]> finalDictionary;
    private HashMap<Integer,String[]> documentDetails;
    private int corpusSize, numOfTerms;

    public Controller() {
        indexer = new Indexer();
        corpusReader = new ReadFile();
        finalDictionary = new TreeMap<>();
        documentDetails = new HashMap<>();
        corpusSize = 0;
        numOfTerms = 0;
    }

    /**
     * Activates the indexing process of the search engine. The function gets the path to the corpus,
     * reads all the documents stored in it, and indexes the corpus using a dictionary and posting files.
     * @param corpusPath the path to the corpus
     * @param targetPath the path of the directory in which the dictionary and posting files will be stored
     * @param stem an indicator of whether the indexing process will include stemming or not
     */
    public void runEngine(String corpusPath, String targetPath, boolean stem){
        corpusReader = new ReadFile();
        Parse parser = new Parse(corpusPath);
        indexer = new Indexer();
        ArrayList<File> filesInCorpus = new ArrayList<>();
        File corpus = new File(corpusPath);

        //creates the directories in which the posting files will be saved
        String tempFilesFolder1 = Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles1").toString()).toString();
        String tempFilesFolder2 = Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles2").toString()).toString();
        File tempDirectory1 = new File(tempFilesFolder1);
        tempDirectory1.mkdir();
        File tempDirectory2 = new File(tempFilesFolder2);
        tempDirectory2.mkdir();

        corpusReader.extractFilesFromFolder(corpus,filesInCorpus);
        //computes a threshold that indicates the amount of files to accumulate before writing a posting files with the terms in them
        long threshold = (corpusReader.getCorpusSize()/100)/(corpusReader.getCorpusSize()/filesInCorpus.size());
        if(threshold < 1){
            threshold = 1;
        }
        int fileCounter = 0;
        int tempFolderCounter1, tempFolderCounter2;

        for(File file : filesInCorpus){
            ArrayList<Article> docsInFile = corpusReader.readOneFile(file.getPath());
            for(Article doc : docsInFile){
                HashMap<String,Term> termsInDoc = parser.parse(doc,stem);
                indexer.collectTermPostingLines(termsInDoc,doc);
            }
            fileCounter++;
            if(fileCounter == threshold){
                indexer.createTemporaryPosting(tempFilesFolder1);
                fileCounter = 0;
            }
        }

        if(fileCounter > 0){
            indexer.createTemporaryPosting(tempFilesFolder1);
        }

        //merges all the initial posting files into temporary merged posting files, before eventually splitting them into 27 final posting files
        tempFolderCounter1 = tempDirectory1.listFiles().length;
        tempFolderCounter2 = tempDirectory2.listFiles().length;
        while(true){
            if(tempFolderCounter1 != 2 && tempFolderCounter2 == 0){
                mergeFiles(tempFilesFolder1,tempFilesFolder2);
            }
            else if(tempFolderCounter1 == 0 && tempFolderCounter2 != 2){
                mergeFiles(tempFilesFolder2,tempFilesFolder1);
            }
            else if(tempFolderCounter1 == 2 && tempFolderCounter2 == 0){
                File[] lastFiles = tempDirectory1.listFiles();
                indexer.createTermsListByLetter(lastFiles[0].getPath(),lastFiles[1].getPath(),targetPath,stem);
                break;
            }
            else if(tempFolderCounter1 == 0 && tempFolderCounter2 == 2){
                File[] lastFiles = tempDirectory2.listFiles();
                indexer.createTermsListByLetter(lastFiles[0].getPath(),lastFiles[1].getPath(),targetPath,stem);
                break;
            }
            tempFolderCounter1 = tempDirectory1.listFiles().length;
            tempFolderCounter2 = tempDirectory2.listFiles().length;
        }
        deleteDirectoryWithFiles(tempFilesFolder1);
        deleteDirectoryWithFiles(tempFilesFolder2);
        numOfTerms = indexer.getDictionary().size();
        corpusSize = indexer.getDocumentDetails().size();
        indexer = new Indexer();
    }

    /**
     * Merges all the temporary posting files stored in the directories.
     * @param sourceFolderPath the path of the directory that contains the temporary posting files
     * @param destinationFolderPath the path of the directory that will store the merged posting files
     */
    private void mergeFiles(String sourceFolderPath, String destinationFolderPath){
        ArrayList<File> filesInSourceFolder = new ArrayList<>();
        File sourceFolder = new File(sourceFolderPath);
        corpusReader.extractFilesFromFolder(sourceFolder,filesInSourceFolder);

        try {
            if (filesInSourceFolder.size() % 2 != 0) { //in case there is an odd number of posting files, one is moved to the other temporary folder and the rest are merged
                String fileName = filesInSourceFolder.get(0).getName();
                Path newFilePath = Paths.get(destinationFolderPath).resolve(fileName);
                Files.move(Paths.get(filesInSourceFolder.get(0).getPath()), newFilePath);
                filesInSourceFolder.remove(0);
            }
            int arraySize = filesInSourceFolder.size();
            for(int i=0; i<arraySize; i+=2){
                File file1 = filesInSourceFolder.get(i), file2 = filesInSourceFolder.get(i+1);
                indexer.mergePostingFiles(file1.getPath(),file2.getPath(),destinationFolderPath);
            }
            deleteDirectoryFiles(sourceFolderPath);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Deletes a directory that has files in it.
     * @param directoryPath the path of the directory
     * @return true if the deletion was completed, false if not
     */
    private boolean deleteDirectoryWithFiles(String directoryPath){
        File directory = new File(directoryPath);
        if(directory.exists()){
            File[] files = directory.listFiles();
            for(File file : files){
                if(file.isDirectory()){
                    deleteDirectoryWithFiles(file.getPath());
                }
                file.delete();
            }
            return directory.delete();
        }
        return false;
    }

    /**
     * Deletes all the files in a given directory.
     * @param directoryPath the path to the directory
     * @return true if the deletion was completed, false if not
     */
    private boolean deleteDirectoryFiles(String directoryPath){
        File directory = new File(directoryPath);
        if(directory.exists()){
            File[] files = directory.listFiles();
            for(File file : files){
                file.delete();
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the amount of documents in the corpus that were indexed
     * @return the amount of documents in the corpus that were indexed
     */
    public int getAmountOfIndexedDocs(){
        return corpusSize;
    }

    /**
     * Returns the amount of unique terms that were indexed in the dictionary
     * @return the amount of unique terms that were indexed in the dictionary
     */
    public int getAmountOfUniqueTerms(){
        return numOfTerms;
    }


    /**
     * Deletes all the posting files and dictionary from the index directory
     * @param path the path of the index
     */
    public boolean deleteIndexes(String path){
        boolean ans = false;
        File[] filesInPath = new File(path).listFiles();
        for (File file : filesInPath) {
            if(file.isDirectory() && file.getName().contains("index")){
                deleteDirectoryWithFiles(file.getPath());
                ans = true;
            }
        }
        finalDictionary = new TreeMap<>();
        documentDetails = new HashMap<>();
        indexer = new Indexer();
        corpusReader = new ReadFile();
        corpusSize = 0;
        numOfTerms = 0;

        return ans;
    }


    /**
     * Reads the indexed file of the dictionary, and loads it into the HashMap
     * @param targetPath the path of the indexed files
     * @param stem an indicator of whether the terms have gone through stemming in the indexing process
     */
    public void loadDictionary(String targetPath, boolean stem) throws Exception{
        finalDictionary = new TreeMap<>();
        String innerTargetPath;
        ArrayList<File> filesInDirectory = new ArrayList<>();
        File dictionaryFile = null;
        BufferedReader dictionaryReader;
        if(stem){ //computes the path of the directory in which the wanted dictionary is stored
            innerTargetPath = Paths.get(targetPath).resolve("indexStem").toString();
        }
        else{
            innerTargetPath = Paths.get(targetPath).resolve("index").toString();
        }

        File innerDirectory = new File(innerTargetPath);
        corpusReader.extractFilesFromFolder(innerDirectory,filesInDirectory);
        for(File file : filesInDirectory){ //searches for the dictionary file
            if(file.getName().contains("finalDictionary")){
                dictionaryFile = file;
                break;
            }
        }

        //reads the dictionary file and fills the final dictionary HashMap with the content
        if(dictionaryFile == null){
            throw new NullPointerException();
        }
        dictionaryReader = new BufferedReader(new FileReader(dictionaryFile));
        String[] termDetails = new String[3];
        String line, term;
        while((line = dictionaryReader.readLine()) != null){
            String[] lineComponents = line.split("_");
            term = lineComponents[0];
            termDetails[0] = lineComponents[1];
            termDetails[1] = lineComponents[2];
            termDetails[2] = lineComponents[3];
            finalDictionary.put(term,termDetails);
        }
        numOfTerms = filesInDirectory.size();
        dictionaryReader.close();
    }

 //   ---------------------------------------------------------- PART B ADDITIONS-----------------------------------------------------------------------------------

    //TODO: change the functionality in searcher such that it accepts the targetPath and the corpusPath in the constructor / runQuery methods,
    // and send these parameters accordingly
    public void runQuery(String query, String corpusPath, String targetPath, boolean stem){
        Parse parser = new Parse(corpusPath);
        Searcher searcher = new Searcher(finalDictionary, documentDetails);

        if(isPath(query)){
            ArrayList<ArrayList<String>> rawQueries = readQueryFile(query); //queries as they appear in the file
            ArrayList<ArrayList<String>> parsedQueries = new ArrayList<>(); //queries after parsing and stemming
            for(ArrayList<String> queryToParse : rawQueries){
                parsedQueries.add(parser.parseQuery(queryToParse,stem));
            }
            searcher.runMultipleQueries(parsedQueries);
        }
        else{
            ArrayList<String> queryWords = new ArrayList<>(Arrays.asList(query.split(" ")));
            queryWords = parser.parseQuery(queryWords, stem);
            searcher.runSingleQuery(queryWords);
        }
    }


    /**
     * Determines whether a given string is a path or not
     * @param path the given string
     * @return true if the string is a path, false otherwise
     */
    private boolean isPath(String path){
        try{
            Paths.get(path);
            return true;
        } catch(InvalidPathException | NullPointerException e){
            return false;
        }
    }


    private ArrayList<ArrayList<String>> readQueryFile(String queryFilePath){

        return null;
    }
}



