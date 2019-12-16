package GUI;

import Indexing.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;

public class Controller {

    private Indexer indexer;
    private ReadFile corpusReader;
    private SortedMap<String,String[]> finalDictionary;
    private HashMap<Integer,String[]> documentDetails;

    /**
     *
     * @param corpusPath
     * @param targetPath
     * @param stem
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
        long threshold = (corpusReader.getCorpusSize()/10)/(corpusReader.getCorpusSize()/filesInCorpus.size());
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
        finalDictionary = indexer.getDictionary();
        documentDetails = indexer.getDocumentDetails();

    }

    /**
     *
     * @param sourceFolderPath
     * @param destinationFolderPath
     */
    private void mergeFiles(String sourceFolderPath, String destinationFolderPath){
        ArrayList<File> filesInSourceFolder = new ArrayList<>();
        File sourceFolder = new File(sourceFolderPath);
        corpusReader.extractFilesFromFolder(sourceFolder,filesInSourceFolder);

        try {
            int startIndex = 0;
            if (filesInSourceFolder.size() % 2 != 0) {
                String fileName = filesInSourceFolder.get(0).getName();
                Path newFilePath = Paths.get(destinationFolderPath).resolve(fileName);
                Files.move(Paths.get(filesInSourceFolder.get(0).getPath()), newFilePath);
                filesInSourceFolder.remove(0);
               // startIndex = 1;
            }
            int arraySize = filesInSourceFolder.size();
            for(int i=0; i<arraySize; i+=2){
                File file1 = filesInSourceFolder.get(i), file2 = filesInSourceFolder.get(i+1);
                indexer.mergePostingFiles(file1.getPath(),file2.getPath(),destinationFolderPath);
/*                file1.delete();
                file2.delete();*/
            }
            deleteDirectoryFiles(sourceFolderPath);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param directoryPath
     * @return
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

    private boolean deleteDirectoryFiles(String directoryPath){
        File directory = new File(directoryPath);
        if(directory.exists()){
            File[] files = directory.listFiles();
            for(File file : files){
                file.delete();
/*                try{
                    Files.delete(file.toPath());
                }catch (Exception e){
                    e.printStackTrace();
                }*/
            }
            return true;
        }
        return false;
    }
    /**
     *
     * @return
     */
    public int getAmountOfIndexedDocs(){
        return documentDetails.size();
    }

    /**
     *
     * @return
     */
    public int getAmountOfUniqueTerms(){
        return finalDictionary.size();
    }
}



