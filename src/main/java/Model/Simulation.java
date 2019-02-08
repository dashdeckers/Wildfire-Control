package Model;

import Model.Elements.Element;
import Model.Elements.Tree;

import java.util.ArrayList;
import java.util.List;

public class Simulation {
    List<List<Element>> cells;
    public Simulation(){
        tree_grid(10,10);
    }

    public void start(){

    }

    public void stop(){

    }
    public void reset(){

    }

    public List<List<Element>> getAllCells() {
        return cells;
    }



    //Dummy function creating only tree tiles for testing GUI
    public void tree_grid(int x, int y){
        cells = new ArrayList<List<Element>>();
        for(int i = 0; i<x; i++){
            List<Element> row = new ArrayList<Element>();
            for(int j=0; j<y; j++){
                if(i== 5 && j == 5){
                    Element t = new Tree(i,j);
                    t.setBurning();
                    row.add(t);
                }else {
                    row.add(new Tree(i, j));
                }
            }
            cells.add(row);
        }
    }
}
