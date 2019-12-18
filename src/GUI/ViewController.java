package GUI;

import java.util.Observable;
import java.util.Observer;

public class ViewController extends Observable implements Observer {

    private Controller controller;


    public ViewController(Controller controller){
        this.controller = controller;
    }


    public void runEngine(String corpusPath, String targetPath, boolean stem){
        controller.runEngine(corpusPath,targetPath,stem);
    }


    public int getAmountOfIndexedDocs(){
        return controller.getAmountOfIndexedDocs();
    }


    public int getAmountOfUniqueTerms(){
        return controller.getAmountOfUniqueTerms();
    }


    public void loadDictionary(String indexPath, boolean stem) throws Exception{
        controller.loadDictionary(indexPath,stem);
    }


    public boolean reset(String path){
        return controller.deleteIndexes(path);
    }


    @Override
    public void update(Observable o, Object arg) {
        if(o == controller){
            setChanged();
            notifyObservers();
        }
    }
}
