package Indexing;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {


    public Main() {

    }

    public static void main(String[] args) {
       ReadFile rf = new ReadFile();
        ArrayList<Article> docs = rf.readFiles("corpus");
        Indexer indexer = new Indexer();
       for(Article article: docs){
            HashMap<String, Term> terms = (new Parse().parse(article,false));
            indexer.collectTermPostingLines(terms, article);
            indexer.createTemporaryPosting(Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles").toString()).toString());
        }
       /* indexer.mergePostingFiles(Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles","0.txt").toString()).toString()
        ,Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles","1.txt").toString()).toString(),
                Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles").toString()).toString());*/
       indexer.createTermsListByLetter(Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles","0.txt").toString()).toString()
               ,Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles","1.txt").toString()).toString(),
               Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles").toString()).toString(),false);



//        for(String term: terms.keySet()){
//            System.out.println(terms.get(term).getTerm());
//        }
//        System.out.println(terms.size());
    }



}

