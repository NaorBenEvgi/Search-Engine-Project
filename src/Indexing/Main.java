package Indexing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class Main {


    public Main() {

    }

    /*public static void main(String[] args) {
        ReadFile rf = new ReadFile();
        ArrayList<Article> docs = rf.readFiles("corpus");
        ArrayList<String> terms =new ArrayList<>();
        for(Article article: docs){
            terms.addAll(new Parse().parse(article));
        }
        for(String term: terms){
            System.out.println(term);
        }
    }*/


    public static void main(String[] args) {
        LinkedList<String> a = new LinkedList<>();
        a.add("435");
        a.add("ABBC");
        a.add("baba");
        a.add("BABA");
        a.add("abba");
        Collections.sort(a,String.CASE_INSENSITIVE_ORDER);
        //a.sort(String::compareTo);
        for(String b : a){
            System.out.println(b);
        }
    }

}

