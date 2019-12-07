package Indexing;
import org.tartarus.snowball.ext.PorterStemmer;

public class Stemmer {

    protected static PorterStemmer stemmer = new PorterStemmer();

    public static void setCurrent(String word){
        stemmer.setCurrent(word);
    }

    public static void stem(){
        stemmer.stem();
    }

    public static String getCurrent(){
        return stemmer.getCurrent();
    }

}
