package Model;

import Model.Elements.*;

import java.io.Serializable;
import java.util.*;

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

    /**
     * Load the default values from the model
     */
    public void fetchModelParameters(){
        Map<String, Float> modelMap = model.getParameters();
        Parameters_map.put("Model", modelMap);
    }

    /**
     * Load the default values from the elements by creating a dummy element and getting it's parameters.
     * After this the elements can disappear
     */
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

    /**
     * When the parameter fields want to change a parameter they should call this function with the
     * type = recipient (Tree, Water, Model)
     * parameter = e.g. Width, Fire Intensity
     * value = the value to be assigned
     *
     * The function calls notify_Observer with a botched triple of STRING, STRING, FLOAt (changed_Parameter)
     * So that each listener can identify whether it's relevant for them and update their values accordingly.
     * @param type
     * @param parameter
     * @param value
     */
    public void changeParameter(String type, String parameter, Float value){
        Parameters_map.get(type).put(parameter,value);
        changed_types.put(type, true);

        Map.Entry<String, Float> sub_entry = new AbstractMap.SimpleImmutableEntry<>(parameter, value);
        Map.Entry<String, Map.Entry<String, Float>> changed_Parameter = new AbstractMap.SimpleImmutableEntry<>(type, sub_entry);

        setChanged();
        notifyObservers(changed_Parameter);
    }

    /**
     * This returns wheter a value has been changed from the original. The advantage of this is that when generating
     * the map with changed parameters you would get width*height*5 retrievals, but this list will prevent some or many
     * of these retrievals  if there is nothing to retrieve.
     * @param type
     * @return
     */
    public boolean isChanged(String type){
        if(changed_types.get(type) != null && changed_types.get(type)/* == true (implicit)*/){
            return true;
        }
        return false;
    }

    /**
     * Returns a map of all parameters and values for the relevant type. This is useful when creating a new Element
     * when one or more of it's parameters has been changed.
     * It is recommended to call isChanged(type) before this to prevent unecessary looping through the map.
     * @param type
     * @return
     */
    public Map<String, Float> getParameterSet(String type){
        return Parameters_map.get(type);
    }

    /**
     * Get width is defined so that Elements can easily access with&height, which are normally model values.
     * This is used for finding whetehr a neighbour is out of bounds.
     * @return
     */
    public int getWidth(){
        return Parameters_map.get("Model").get("Width").intValue();
    }

    /**
     * Get height is defined so that Elements can easily access with&height, which are normally model values.
     * This is used for finding whetehr a neighbour is out of bounds.
     * @return
     */
    public int getHeight(){
        return Parameters_map.get("Model").get("Height").intValue();
    }

    public float getParameter(String type, String parameter){
        if(Parameters_map == null){
            return -0.0f;
        }
        if(Parameters_map.get(type) == null){
            System.out.println("ERROR NO PARAMETERS FOR TYPE: " + type);
            return -0.0f;
        }
        return Parameters_map.get(type).get(parameter);
    }

    public Set<String> getTypes(){
        return Parameters_map.keySet();
    }
    //TODO! Turn float into Integer
    //TODO! Fix reset to return to seed
}
