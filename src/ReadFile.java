import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
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
        ArrayList<Article> docs = new ArrayList<>();
        Document xml = convertToValidXML(inputFile);
        if(xml == null){
            return docs;
        }
        NodeList nList = xml.getElementsByTagName("DOC");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            try {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String docId = eElement.getElementsByTagName("DOCNO").item(0).getTextContent();
//                    String date = eElement.getElementsByTagName("DATE1").item(0).getTextContent();
//                    String title = eElement.getElementsByTagName("TI").item(0).getTextContent();
                    try{
                        String content = eElement.getElementsByTagName("TEXT").item(0).getTextContent();
                        docs.add(new Article(docId, content));
                    } catch (Exception e) {
//                        System.out.println(docId + " has no text attribute");
                    }
                }
            } catch (Exception e) {
//                System.out.println(inputFile);
            }
        }
        return docs;
    }

    public ArrayList<Article> readOneFile(String filePath){
        ArrayList<Article> docs = new ArrayList<>();
        try {
            String inputFile = new String ( Files.readAllBytes( Paths.get(filePath) ) );
            docs = extractArticlesFromFile(inputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return docs;
    }

    public void extractFilesFromFolder(File file, ArrayList<File> files){
        if(file.isDirectory()){
            File[] fileList = file.listFiles();
            if(fileList != null){
                for (File f : fileList) {
                    if(f.isDirectory()){
                        extractFilesFromFolder(f, files);
                    }
                    else {
                       files.add(f);
                    }
                }
            }
        }
    }

    public ArrayList<Article> readFiles(String pathToDocsFolder){
        ArrayList<Article> docs = new ArrayList<>();
        File folder = new File(pathToDocsFolder);
        ArrayList<File> files = new ArrayList<>();
        extractFilesFromFolder(folder, files);
        for (File file : files) {
            docs.addAll(readOneFile(file.getPath()));
        }
        return docs;
    }
}
