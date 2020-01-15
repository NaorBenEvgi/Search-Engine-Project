package Indexing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This class gets a path to a corpus and reads all the files in it, and extracts all the documents in these files.
 */
public class ReadFile {

    private long corpusSize;
    private static final String DELETE_NULLS = "[\\000]*";


    public ReadFile(){
        corpusSize = 0;
    }

    /**
     * Getter for the size of the corpus in memory
     * @return the size of the corpus
     */
    public long getCorpusSize() {
        return corpusSize;
    }

    /**
     * Converts a text file into an XML format
     * @param input the content of the text file
     * @return the file in XML format
     */
    private Document convertToValidXML(String input) {
        try{
            String content = input.replaceAll(DELETE_NULLS, "").replaceAll("\\s*P=[0-9]*", "");
            String xml = "<wrapper>" + content.replaceAll("”","\"") + "</wrapper>";
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e){
            return null;
        }

    }

    /**
     * Extracts all the documents in a certain file into a list of Articles
     * @param inputFile the content of the given text file
     * @return a list of the documents in the file
     */
    private ArrayList<Article> extractArticlesFromFile(String inputFile) {
        ArrayList<Article> articles = new ArrayList<>();
        Document xml = convertToValidXML(inputFile);
        if(xml == null){
            return articles;
        }
        NodeList articleList = xml.getElementsByTagName("DOC");
        for (int i = 0; i < articleList.getLength(); i++) {
            try {
                Node article = articleList.item(i);
                if (article.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) article;
                    String docId = eElement.getElementsByTagName("DOCNO").item(0).getTextContent();

                    try{
                        String content = eElement.getElementsByTagName("TEXT").item(0).getTextContent();
                        articles.add(new Article(docId, content));
                    } catch (Exception e) { }
                }
            } catch (Exception e) { }
        }
        return articles;
    }

    /**
     * Reads a file and extracts all the documents from it
     * @param filePath the path to the file
     * @return a list of the documents in the file
     */
    public ArrayList<Article> readOneFile(String filePath){
        ArrayList<Article> articles = new ArrayList<>();
        try {
            String inputFile = new String ( Files.readAllBytes( Paths.get(filePath) ) );
            articles = extractArticlesFromFile(inputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articles;
    }

    /**
     * Collects all the files that are stored in a given directory or in its subdirectories
     * @param file the given directory
     * @param container the list in which all the files in the directory will be stored
     */
    public void extractFilesFromFolder(File file, ArrayList<File> container){
        if(file.isDirectory()){
            File[] fileList = file.listFiles();
            if(fileList != null){
                for (File f : fileList) {
                    if(f.isDirectory()){
                        extractFilesFromFolder(f, container);
                    }
                    else {
                        if(!f.getName().equals("stop_words.txt")) {
                            container.add(f);
                            corpusSize += f.length();
                        }
                    }
                }
            }
        }
    }

    /**
     * Reads all the files in a given directory, and collects all the documents in these files
     * @param pathToDocsFolder the path to the directory
     * @return a list of all the documents in all the files in the directory
     */
    public ArrayList<Article> readFiles(String pathToDocsFolder){
        File folder = new File(pathToDocsFolder);
        ArrayList<File> filesList = new ArrayList<>();
        extractFilesFromFolder(folder, filesList);

        ArrayList<Article> articles = new ArrayList<>();
        for (File file : filesList) {
            articles.addAll(readOneFile(file.getPath()));
        }
        return articles;
    }
}
