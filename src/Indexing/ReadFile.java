package Indexing;

import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class ReadFile {

    private Document convertToValidXML(String input) {
        try{
            String xml = "<wrapper>" + input.replaceAll("”","\"") + "</wrapper>";
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e){
            return null;
        }

    }

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
//                    String date = eElement.getElementsByTagName("DATE1").item(0).getTextContent();
//                    String title = eElement.getElementsByTagName("TI").item(0).getTextContent();
                    try{
                        String content = eElement.getElementsByTagName("TEXT").item(0).getTextContent();
                        articles.add(new Article(docId, content));
                    } catch (Exception e) {
//                        System.out.println(docId + " has no text attribute");
                    }
                }
            } catch (Exception e) {
//                System.out.println(inputFile);
            }
        }
        return articles;
    }

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

    public void extractFilesFromFolder(File file, ArrayList<File> container){
        if(file.isDirectory()){
            File[] fileList = file.listFiles();
            if(fileList != null){
                for (File f : fileList) {
                    if(f.isDirectory()){
                        extractFilesFromFolder(f, container);
                    }
                    else {
                       container.add(f);
                    }
                }
            }
        }
    }

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
