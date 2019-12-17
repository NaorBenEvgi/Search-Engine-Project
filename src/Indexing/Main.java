package Indexing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Main {


    public Main() {

    }

    public static void main(String[] args) throws Exception{
      /* ReadFile rf = new ReadFile();
        ArrayList<Article> docs = rf.readFiles("corpus");
        Indexer indexer = new Indexer();
       for(Article article: docs){
            HashMap<String, Term> terms = (new Parse().parse(article,false));
            indexer.collectTermPostingLines(terms, article);
            indexer.createTemporaryPosting(Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles1").toString()).toString());
        }
        indexer.mergePostingFiles(Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles1","0.txt").toString()).toString()
        ,Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles1","1.txt").toString()).toString(),
                Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles1").toString()).toString());*/
   /*    indexer.createTermsListByLetter(Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles","0.txt").toString()).toString()
               ,Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles","1.txt").toString()).toString(),
               Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles").toString()).toString(),false);*/

        /*File file = new File("C:\\Users\\royj1\\Desktop\\a");
        Path path = Paths.get(file.getPath());
        System.out.println(path.resolve("aaa.txt").toString());*/

/*
        new File(Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles11").toString()).toString()).mkdir();
        Files.walk(Paths.get(System.getProperty("user.dir"), Paths.get("postingFiles11").toString())).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
*/
        /*long time = System.currentTimeMillis();
        Controller contri = new Controller();
        contri.runEngine("C:\\Users\\royj1\\IdeaProjects\\search-engine\\corpus","C:\\Users\\royj1\\Desktop\\a",false);
        System.out.println((System.currentTimeMillis() - time)/1000);*/


        File a = new File("C:\\Users\\royj1\\Desktop\\a\\b.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(a,true));
        try{
            bw.append("hi");
            bw.newLine();
            //bw.flush();
            bw.close();
        }catch(Exception e){
            System.err.println("fuck");
        }
    }



}

