package Model;

import Model.Elements.*;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class ParameterManager extends Observable implements Serializable {

    private Simulation model;
    private Map<String, Map<String, Float>> Parameters_map;
    private Map<String, Boolean> changed_types = new HashMap<>();
    public ParameterManager(Simulation model){
        this.model = model;
        Parameters_map = new HashMap<>();
        fetchModelParameters();
        fetchElementParameters();
    }

    public void fetchModelParameters(){
        Map<String, Float> modelMap = model.getParameters();
        Parameters_map.put("Model", modelMap);
    }

    public void fetchElementParameters(){
        Map<String, Float> grassMap = new Grass(0,0, this).getParameters();
        Parameters_map.put("Grass", grassMap);
        Map<String, Float> houseMap = new House(0,0, this).getParameters();
        Parameters_map.put("House", houseMap);
        Map<String, Float> roadMap = new Road(0,0,this).getParameters();
        Parameters_map.put("Road", roadMap);
        Map<String, Float> treeMap = new Tree(0,0,this).getParameters();
        Parameters_map.put("Tree", treeMap);
        Map<String, Float> waterMap = new Water(0,0,this).getParameters();
        Parameters_map.put("Water", waterMap);
    }

    public void changeParameter(String type, String parameter, Float value){
        Parameters_map.get(type).put(parameter,value);
        changed_types.put(type, true);

        Map.Entry<String, Float> sub_entry = new AbstractMap.SimpleImmutableEntry<>(parameter, value);
        Map.Entry<String, Map.Entry<String, Float>> changed_Parameter = new AbstractMap.SimpleImmutableEntry<>(type, sub_entry);

        setChanged();
        notifyObservers(changed_Parameter);
    }

    public boolean isChanged(String type){
        if(changed_types.get(type) != null && changed_types.get(type)/* == true (implicit)*/){
            return true;
        }
        return false;
    }

    public Map<String, Float> getParameterSet(String type){
        return Parameters_map.get(type);
    }

    public int getWidth(){
        return Parameters_map.get("Model").get("Width").intValue();
    }

    public int getHeight(){
        return Parameters_map.get("Model").get("Height").intValue();
    }
    //TODO! Add elements as observers and find some way to update them  DONE
    //TODO! Ensure that new elements are set according to ParameterManager if ParameterManager exists DONE
    //TODO! Fetch values from simulation    DONE
    //TODO! Pass ParameterManager to ControlPanel so that it can send updates to this
    //TODO! Turn float into Integer
    //TODO! Consider separating Fuel into FUel and Starting_fuel
    //TODO! Fix reset to return to seed!
}
