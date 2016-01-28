package com.tomandfelix.stapp2.persistency;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tom on 6/10/2014.
 * The class that corresponds to the logs in the database
 */
public class DBLog {
    protected String action;
    protected Date datetime;
    protected double data;
    private SimpleDateFormat formatForDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat formatForMilliseconds = new SimpleDateFormat("SSS");
    private SimpleDateFormat reconstructFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public DBLog(String action, Date datetime, double data) {
        this.action = action;
        this.datetime = datetime;
        if(action.equals(DatabaseHelper.LOG_CONNECT) || action.equals(DatabaseHelper.LOG_DISCONNECT)|| action.equals(DatabaseHelper.LOG_START_DAY)) {
            this.data = -1;
        } else {
            this.data = data;
        }
    }

    public DBLog(JSONObject obj) throws JSONException, ParseException {
        this.action = obj.getString("action");
        this.datetime = reconstructFormat.parse(obj.getString("datetime") + "." + String.format("%03d", obj.getInt("milliseconds")));
        this.data = obj.isNull("data") ? -1 : obj.getDouble("data");
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return action + " " + DatabaseHelper.dateToString(datetime) + " " + data;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("action", action);
        result.put("datetime", formatForDateTime.format(datetime));
        result.put("milliseconds", Integer.parseInt(formatForMilliseconds.format(datetime)));
        result.put("data", data);
        return result;
    }
}
