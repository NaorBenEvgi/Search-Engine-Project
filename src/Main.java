import java.util.ArrayList;

public class Main {

    public Main() {

    }

    public static void main(String[] args) {
        ReadFile rf = new ReadFile();
        ArrayList<Article> docs = rf.readFiles("corpus");
        ArrayList<String> terms =new ArrayList<>();
        for(Article article: docs){
            terms.addAll(new Parse().parse(article));
        }
        for(String term: terms){
            System.out.println(term);
        }
    }
}

