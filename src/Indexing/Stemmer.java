package Indexing;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * This class is responsible for stemming terms that have gone through parsing. It uses an open code of Porter's algorithm.
 */
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

