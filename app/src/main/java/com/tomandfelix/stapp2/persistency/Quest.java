package com.tomandfelix.stapp2.persistency;

/**
 * Created by Flixse on 27/01/2015.
 */
public abstract class Quest {
    public enum Type{SOLO, CHALLENGE, COOP}
    protected int id;
    protected String name;
    protected String description;
    protected Type type;

    public Quest(int id, String name, String description, Type type){
        this.id = id;
        this.description = description;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        String info = "";
        info += "id:" + id;
        info += (name == null ? "":" name:" + name);
        info += (description == null ? "":" description:" + description);
        return info;
    }
}
