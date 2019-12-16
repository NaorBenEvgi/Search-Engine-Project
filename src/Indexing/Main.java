package Indexing;

import GUI.Controller;

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

        Controller contri = new Controller();
        contri.runEngine("C:\\Users\\Naor\\Desktop\\לימודים\\שנה ג\\סמסטר א\\אחזור\\Search-Engine-Project\\search-engine\\corpus","C:\\Users\\Naor\\Desktop\\target",false);

//        for(String term: terms.keySet()){
//            System.out.println(terms.get(term).getTerm());
//        }
//        System.out.println(terms.size());
    }



}

