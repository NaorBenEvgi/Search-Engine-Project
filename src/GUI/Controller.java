package GUI;

import Indexing.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Controller {

    private Indexer indexer;
    private ReadFile corpusReader;
    private HashMap<String,String[]> finalDictionary;

    public void runEngine(String corpusPath, String targetPath, boolean stem){
        corpusReader = new ReadFile();
        Parse parser = new Parse();
        indexer = new Indexer();
        ArrayList<File> filesInCorpus = new ArrayList<>();
        File corpus = new File(corpusPath);
        String tempFilesFolder1 = Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles1").toString()).toString();
        String tempFilesFolder2 = Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles2").toString()).toString();
        corpusReader.extractFilesFromFolder(corpus,filesInCorpus);
        long threshold = (corpusReader.getCorpusSize()/10)/(corpusReader.getCorpusSize()/filesInCorpus.size());
        int fileCounter = 0;

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





    }


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
                startIndex = 1;
            }
            int arraySize = filesInSourceFolder.size();
            for(int i=startIndex; i<arraySize; i+=2){
                File file1 = filesInSourceFolder.get(i), file2 = filesInSourceFolder.get(i+1);
                indexer.mergePostingFiles(file1.getPath(),file2.getPath(),destinationFolderPath);
                file1.delete();
                file2.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}



