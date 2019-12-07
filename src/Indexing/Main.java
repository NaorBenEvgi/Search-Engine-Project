package Indexing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
        String content = "Naor is a really big BITCH";
        File file = new File("C:\\Users\\royj1\\Desktop\\University\\הנדסת מערכות מידע\\שנה ג\\סמסטר ה\\אחזור מידע\\פרויקט\\Test.txt");
        try {
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

