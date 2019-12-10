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
        String t = "Hello World how are you there";
        System.out.println(t.getBytes().length);
      /*  a.add("435");
        a.add(t);
        a.add("baba");
        a.add("BABA");
        a.add("abba");
        Collections.sort(a,String.CASE_INSENSITIVE_ORDER);
        //a.sort(String::compareTo);
        for(String b : a){
            System.out.println(b.codePoints()
                    .map(cp -> cp<=0x7ff? cp<=0x7f? 1: 2: cp<=0xffff? 3: 4)
                    .sum());
        }*/


    }

}

