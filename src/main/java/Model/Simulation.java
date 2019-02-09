package Model;

import Model.Elements.Element;
import Model.Elements.Tree;

import javax.swing.undo.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class Simulation extends Observable {
    List<List<Element>> cells;
    Map<String, Float> parameters;
    private Random rand;
    UndoManager undoManager;

    public Simulation(){
        parameters = new HashMap<String, Float>();
        undoManager = new UndoManager();
        rand = new Random();

        create_parameters();

        tree_grid( parameters.get("Width").intValue(), parameters.get("Height").intValue());
    }

    public void start(){
        System.out.println("Starting");
    }
    public void stop(){
        System.out.println("Stopping");
    }
    public void reset(){}
    public void regenerate(){
        tree_grid(parameters.get("Width").intValue(), parameters.get("Height").intValue());
    }
    public void stepBack(){
        if(undoManager.canUndo()){
            undoManager.undo();
        }
    }
    public void stepForward(){
        if(undoManager.canRedo()){
            undoManager.redo();
        }else{
            UndoableEdit undoableEdit = new AbstractUndoableEdit() {

                public void redo() throws CannotRedoException {
                    super.redo();
                }

                public void undo() throws CannotUndoException {
                    super.undo();
                }
            };
        }
    }

    public List<List<Element>> getAllCells() {
        return cells;
    }



    //Dummy function creating only tree tiles for testing GUI
    public void tree_grid(int x, int y){

        int fire_x = rand.nextInt(x);
        int fire_y = rand.nextInt(y);
        cells = new ArrayList<List<Element>>();
        for(int i = 0; i<x; i++){
            List<Element> row = new ArrayList<Element>();
            for(int j=0; j<y; j++){
                if(i== fire_x && j == fire_y){
                    System.out.println("Fire at " + i + "," + j);
                    Element t = new Tree(i,j);
                    t.setBurning();
                    row.add(t);
                }else {
                    row.add(new Tree(i, j));
                }
            }
            cells.add(row);
        }
        setChanged();
        notifyObservers(cells);
    }

    public void create_parameters(){
        parameters.put("Width", 20f);
        parameters.put("Height", 20f);
        parameters.put("Fire speed", 1f);
        parameters.put("Wind strength", 1f);
        parameters.put("Yadayada", 0f);
    }

    public void changeParameter(String s, float v){
        System.out.println("Setting " + s + " to " + v);
        parameters.put(s, v);
    }

    public Map<String, Float> getParameters(){
        return parameters;
    }
}
