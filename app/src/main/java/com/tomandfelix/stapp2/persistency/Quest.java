package com.tomandfelix.stapp2.persistency;

/**
 * Created by Flixse on 27/01/2015.
 */
public abstract class Quest {
    public enum Type{SOLO, CHALLENGE, COOP}
    protected int id;
    protected Type type;

    public Quest(int id, Type type){
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        String info = "";
        info += "id:" + id;
        return info;
    }
}
