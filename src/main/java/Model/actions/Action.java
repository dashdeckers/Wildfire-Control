package Model.actions;

import Model.Elements.*;

public abstract class Action {
    private int energyCost;
    private Element cell;
    private Agent agent;

    public abstract void excecuteAction();

    public Action(int energyCost, Element cell, Agent agent) {
        this.energyCost = energyCost;
        this.cell=cell;
        this.agent=agent;
    }

    public boolean checkAction(){
        if(agent.getParameters().get("Energy Level")>=energyCost){
            return true;
        } else {
            return false;
        }
    }
}
