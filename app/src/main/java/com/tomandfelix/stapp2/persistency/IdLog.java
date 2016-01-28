package com.tomandfelix.stapp2.persistency;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by Tom on 11/03/2015.
 */
public class IdLog extends DBLog {
    protected int id;

    public IdLog(int id, String action, Date datetime, double data) {
        super(action, datetime, data);
        this.id = id;
    }

    public IdLog(int id, DBLog dblog) {
        super(dblog.getAction(), dblog.datetime, dblog.data);
        this.id = id;
    }

    public IdLog(JSONObject obj) throws JSONException, ParseException {
        super(obj);
        this.id = obj.getInt("log_id");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id + " " + super.toString();
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject result = super.toJSONObject();
        result.put("id", id);
        return result;
    }
}
