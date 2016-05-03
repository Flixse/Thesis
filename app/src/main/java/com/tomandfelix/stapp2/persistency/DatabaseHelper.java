package com.tomandfelix.stapp2.persistency;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.persistency.ChallengeStatus.Status;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * Created by Tom on 6/10/2014.
 * Works the local database for the application
 */
public class DatabaseHelper extends SQLiteOpenHelper{
    private static DatabaseHelper uniqueInstance = null;
    private static Context context;
    private static final int DATABASE_VERSION = 1;
    private static final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    private static int lastPendingUploadIndex = 0;
    SQLiteDatabase db;
    private static final String DATABASE_NAME = "data.db";
    private static final String TABLE_LOGS = "logs";
    private static final String TABLE_PROFILES = "profiles";
    private static final String TABLE_SETTINGS = "settings";
    private static final String TABLE_LC = "livechallenges";
    private static final String TABLE_OPPONENTSTATUS = "opponentstatus";
    private static final String KEY_ID = "id";
    private static final String KEY_ACTION = "action";
    private static final String KEY_DATETIME = "datetime";
    private static final String KEY_DATA = "data";
    private static final String KEY_FIRSTNAME = "firstname";
    private static final String KEY_LASTNAME = "lastname";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_MONEY = "money";
    private static final String KEY_EXPERIENCE = "experience";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_RANK = "rank";
    private static final String KEY_UPDATED = "lastUpdated";
    private static final String KEY_SETTING = "name";
    private static final String KEY_VALUE_INT = "intValue";
    private static final String KEY_VALUE_STRING = "stringValue";
    private static final String KEY_UNIQUE_ID = "unique_id";
    private static final String KEY_CHALLENGE_ID = "challenge_id";
    private static final String KEY_STATUS = "status";
    private static final String KEY_OPPONENT_ID = "opponent_id";
    private static final String KEY_STATUS_MESSAGE = "status_message";
    public static final String OWNER = "owner";
    public static final String NOTIF = "notification";
    public static final String TOKEN = "token";
    public static final String LAST_ENTERED_USERNAME  = "last_entered_username";
    public static final String UPLOAD3G = "upload_if_3g";
    public static final String LAST_UPLOADED_INDEX = "last_uploaded_index";
    public static final String UPLOAD_FREQUENCY = "upload_frequency";
    public static final String LAST_USED_SENSOR = "last_used_sensor";
    public static final String PROFILE_GRAPH = "profile_graph";
    public static final String PROFILE_OPEN_CHALLENGE = "profile_open_challenge";
    public static final String LOG_SIT = "sit";
    public static final String LOG_OVERTIME = "sit_overtime";
    public static final String LOG_STAND = "stand";
    public static final String LOG_START_DAY = "begin_day";
    public static final String LOG_STOP_DAY = "end_day";
    public static final String LOG_CONNECT = "sensor_connect";
    public static final String LOG_DISCONNECT = "sensor_disconnect";
    public static final String LOG_ACH_SCORE = "achieved_score";
    public static final String LOG_ACH_SCORE_PERC = "achieved_score_percent";

    public static DatabaseHelper getInstance() {
        if(uniqueInstance == null) {
            uniqueInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return uniqueInstance;
    }

    public static void init(Context context) {
        if(uniqueInstance == null) {
            uniqueInstance = new DatabaseHelper(context.getApplicationContext());
            DatabaseHelper.context = context;
        }
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("PRAGMA foreign_keys=ON;");
        db.execSQL("CREATE TABLE " + TABLE_LOGS + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_ACTION + " TEXT, " + KEY_DATETIME + " DATETIME, " + KEY_DATA + " DOUBLE)");
        db.execSQL("CREATE TABLE " + TABLE_PROFILES + " (" + KEY_ID + " INTEGER PRIMARY KEY NOT NULL UNIQUE, "
                + KEY_FIRSTNAME + " TEXT, " + KEY_LASTNAME + " TEXT, " + KEY_USERNAME + " TEXT, "
                + KEY_EMAIL + " TEXT, " + KEY_MONEY + " INT, " + KEY_EXPERIENCE + " INT, "
                + KEY_AVATAR + " TEXT, " + KEY_RANK + " INT, " + KEY_UPDATED + " DATETIME)");
        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + " (" + KEY_SETTING + " TEXT PRIMARY KEY NOT NULL UNIQUE, "
                + KEY_VALUE_INT + " INTEGER, " + KEY_VALUE_STRING + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_LC + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_UNIQUE_ID + " TEXT NOT NULL UNIQUE, " + KEY_CHALLENGE_ID + " INTEGER, "
                + KEY_STATUS + " TEXT, " + KEY_DATA + " TEXT, " + KEY_STATUS_MESSAGE + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_OPPONENTSTATUS + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_UNIQUE_ID + " TEXT, " + KEY_OPPONENT_ID + " INTEGER, " + KEY_STATUS + " TEXT, "
                + KEY_DATA + " TEXT, FOREIGN KEY(" + KEY_UNIQUE_ID + ") REFERENCES " + TABLE_LC + "(" + KEY_UNIQUE_ID + "))");


        ContentValues values = new ContentValues(2);
        values.put(KEY_SETTING, OWNER);
        values.put(KEY_VALUE_INT, -1);
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
        values.put(KEY_SETTING, NOTIF);
        values.put(KEY_VALUE_INT, 0);
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
        values.put(KEY_SETTING, TOKEN);
        values.put(KEY_VALUE_STRING, "");
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
        values.put(KEY_SETTING, LAST_ENTERED_USERNAME);
        values.put(KEY_VALUE_STRING, "");
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
        values.put(KEY_SETTING, UPLOAD3G);
        values.put(KEY_VALUE_INT, 0);
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
        values.put(KEY_SETTING, LAST_UPLOADED_INDEX);
        values.put(KEY_VALUE_INT, 0);
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
        values.put(KEY_SETTING, UPLOAD_FREQUENCY);
        values.put(KEY_VALUE_INT, 60000);
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
        values.put(KEY_SETTING, LAST_USED_SENSOR);
        values.put(KEY_VALUE_STRING, "");
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
        values.put(KEY_SETTING, PROFILE_GRAPH);
        values.put(KEY_VALUE_INT, "1");
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
        values.put(KEY_SETTING, PROFILE_OPEN_CHALLENGE);
        values.put(KEY_VALUE_INT, "1");
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
        db.insert(TABLE_SETTINGS, null, values);
        values.clear();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OPPONENTSTATUS);
        onCreate(db);
    }

    private int secondsAgo(Date input) {
        return Math.round(TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - input.getTime()));
    }

    public static String dateToString(Date date) {
        return df.format(date);
    }

    public static Date stringToDate(String dateString) {
        try {
            return df.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void truncateLogs() {
        db.execSQL("DELETE FROM " + TABLE_LOGS);
        db.execSQL("DELETE FROM sqlite_sequence where name='" + TABLE_LOGS + "'");
        setIntSetting(LAST_UPLOADED_INDEX, 0);
    }

    public void openDB() {
        db = getWritableDatabase();
    }

    //--------------------------------------------------------LOGS------------------------------------------------------------------------------

    public ArrayList<IdLog> getLogsToUpload() {
        Log.i("getLogsToUpload", "getting logs to upload");
        String query = "SELECT " + KEY_ID + ", " + KEY_ACTION + ", " + KEY_DATETIME + ", " + KEY_DATA +  " FROM " + TABLE_LOGS + " WHERE " + KEY_ID + " > " + getIntSetting(LAST_UPLOADED_INDEX) + " ORDER BY " + KEY_ID + " ASC LIMIT 1000";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<IdLog> result = null;
        if(cursor != null && cursor.getCount() > 0) {
            result = new ArrayList<>();
            while(cursor.moveToNext()) {
                result.add(new IdLog(cursor.getInt(0), cursor.getString(1), stringToDate(cursor.getString(2)), cursor.getDouble(3)));
            }
        }
        if(result != null) {
            lastPendingUploadIndex = result.get(result.size() - 1).getId();
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public void confirmUpload(int lastIndex) {
        if(lastPendingUploadIndex == lastIndex) {
            setIntSetting(LAST_UPLOADED_INDEX, lastIndex);
        }
        lastPendingUploadIndex = 0;
    }

    public void addLog(DBLog log) {
        Log.i("addLog", log.toString());
        ContentValues input = new ContentValues(3);
        input.put(KEY_ACTION, log.getAction());
        input.put(KEY_DATETIME, dateToString(log.getDatetime()));
        input.put(KEY_DATA, log.getData() == -1 ? null : log.getData());
        db.insert(TABLE_LOGS, null, input);
    }

    public void storeLogs(ArrayList<IdLog> logs) {
        if(logs != null) {
            ContentValues input = new ContentValues(4);
            int maxId = getIntSetting(LAST_UPLOADED_INDEX);
            Log.i("storeLog", "storing records " + logs.get(0).getId() + "->" + logs.get(logs.size() - 1).getId());
            for (IdLog log : logs) {
                maxId = Math.max(maxId, log.getId());
                input.put(KEY_ID, log.getId());
                input.put(KEY_ACTION, log.getAction());
                input.put(KEY_DATETIME, dateToString(log.getDatetime()));
                input.put(KEY_DATA, log.getData() == -1 ? null : log.getData());
                db.insert(TABLE_LOGS, null, input);
                input.clear();
            }
            setIntSetting(LAST_UPLOADED_INDEX, maxId);
        }
    }

    public IdLog getLastLog() {
        Log.i("getLastLog", "getting last log");
        String query = "SELECT " + KEY_ID + ", " + KEY_ACTION + ", " + KEY_DATETIME + ", " + KEY_DATA +  " FROM " + TABLE_LOGS + " ORDER BY " + KEY_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        IdLog result = null;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = new IdLog(cursor.getInt(0), cursor.getString(1), stringToDate(cursor.getString(2)), cursor.getDouble(3));
        }
        if (cursor != null) cursor.close();
        return result;
    }
    public ArrayList<DBLog> getTodaysConnectionLogs() {
        Log.i("getTodaysConnectionLogs", "getting today's connect & disconnect logs");
        String query = "SELECT " + KEY_ACTION + ", " + KEY_DATETIME + ", " + KEY_DATA + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " IN('" + LOG_CONNECT + "', '" + LOG_DISCONNECT + "') AND " + KEY_ID + " > (SELECT " + KEY_ID + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " = '" + LOG_START_DAY + "' ORDER BY " + KEY_ID + " DESC LIMIT 1) ORDER BY " + KEY_ID + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<DBLog> result = null;
        if(cursor != null && cursor.getCount() > 0) {
            result = new ArrayList<>();
            while(cursor.moveToNext()) {
                result.add(new DBLog(cursor.getString(0), stringToDate(cursor.getString(1)), cursor.getDouble(2)));
            }
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public ArrayList<DBLog> getTodaysLogs() {
        Log.i("getTodaysLogs", "getting today's logs");
        String query = "SELECT " + KEY_ACTION + ", " + KEY_DATETIME + ", " + KEY_DATA + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " NOT IN ('" + LOG_START_DAY + "', '" + LOG_ACH_SCORE_PERC + "', '" + LOG_ACH_SCORE + "', '" + LOG_STOP_DAY + "') AND " + KEY_ID + " > (SELECT " + KEY_ID + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " = '" + LOG_START_DAY + "' ORDER BY " + KEY_ID + " DESC LIMIT 1) ORDER BY " + KEY_ID + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<DBLog> result = null;
        if(cursor != null && cursor.getCount() > 0) {
            result = new ArrayList<>();
            while(cursor.moveToNext()) {
                result.add(new DBLog(cursor.getString(0), stringToDate(cursor.getString(1)), cursor.getDouble(2)));
            }
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public ArrayList<DBLog> get2WeekEndLogs() {
        Log.i("get2WeekEndLogs", "getting end day logs from last 2 weeks");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.SECOND, 0);cal.set(Calendar.MINUTE, 0);cal.set(Calendar.HOUR_OF_DAY, 0);cal.set(Calendar.MILLISECOND, 0);
        Date stop = cal.getTime();
        cal.add(Calendar.DATE, -14);
        String query = "SELECT " + KEY_ACTION + ", " + KEY_DATETIME + ", " + KEY_DATA + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " IN ('" + LOG_ACH_SCORE_PERC + "', '" + LOG_STOP_DAY + "') AND " + KEY_DATETIME + " > '" + dateToString(cal.getTime()) + "' AND " + KEY_DATETIME + " < '" + dateToString(stop) + "' ORDER BY " + KEY_ID + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<DBLog> result = null;
        if(cursor != null && cursor.getCount() > 0) {
            result = new ArrayList<>();
            while(cursor.moveToNext()) {
                result.add(new DBLog(cursor.getString(0), stringToDate(cursor.getString(1)), cursor.getDouble(2)));
            }
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public ArrayList<DBLog> getLogsBetween(Date start, Date end) {
        Log.i("getLogsBetween", "getting logs between " + dateToString(start) + " and " + dateToString(end));
        String query = "SELECT " + KEY_ACTION + ", " + KEY_DATETIME + ", " + KEY_DATA +  " FROM " + TABLE_LOGS + " WHERE " + KEY_DATETIME + " > '" + dateToString(start) + "' AND " + KEY_DATETIME + " < '" + dateToString(end) + "' ORDER BY " + KEY_ID + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<DBLog> result = null;
        if(cursor != null && cursor.getCount() > 0) {
            result = new ArrayList<>();
            while(cursor.moveToNext()) {
                result.add(new DBLog(cursor.getString(0), stringToDate(cursor.getString(1)), cursor.getDouble(2)));
            }
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public ArrayList<DBLog> getSitStandBetween(Date start, Date end) {
        Log.i("getSitStandBetween", "getting sit and stand between " + dateToString(start) + " and " + dateToString(end));
        String query = "SELECT " + KEY_ACTION + ", " + KEY_DATETIME + ", " + KEY_DATA +  " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " IN('" + LOG_SIT + "', '" + LOG_STAND + "') AND " + KEY_DATETIME + " > '" + dateToString(start) + "' AND " + KEY_DATETIME + " < '" + dateToString(end) + "' ORDER BY " + KEY_ID + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<DBLog> result = null;
        if(cursor != null && cursor.getCount() > 0) {
            result = new ArrayList<>();
            while(cursor.moveToNext()) {
                result.add(new DBLog(cursor.getString(0), stringToDate(cursor.getString(1)), cursor.getDouble(2)));
            }
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public DBLog getLastLogBefore(Date dateTime) {
        Log.i("getLastLogBefore", "running");
        String query = "SELECT " + KEY_ACTION + ", " + KEY_DATETIME + ", " + KEY_DATA + " FROM " + TABLE_LOGS + " WHERE " + KEY_DATETIME + " < '" + dateToString(dateTime) + "' ORDER BY " + KEY_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        DBLog result = null;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result =  new DBLog(cursor.getString(0), stringToDate(cursor.getString(1)), cursor.getDouble(2));
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public DBLog getFirstRecordOfDay() {
        Log.i("getFirstRecordOfDay", "running");
        String query = "SELECT " + KEY_ACTION + ", " + KEY_DATETIME + ", " + KEY_DATA + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " IN('" + LOG_SIT + "', '" + LOG_STAND + "') AND " + KEY_ID + " > (SELECT " + KEY_ID + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " = '" + LOG_START_DAY + "' ORDER BY " + KEY_ID + " DESC LIMIT 1) ORDER BY " + KEY_ID + " ASC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        DBLog result = null;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = new DBLog(cursor.getString(0), stringToDate(cursor.getString(1)), cursor.getDouble(2));
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public DBLog getLastSitStand() {
        Log.i("getLastSitStand", "running");
        if(dayStarted() == null) {
            return null;
        }
        String query = "SELECT " + KEY_ACTION + ", " + KEY_DATETIME + ", " + KEY_DATA + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " IN('" + LOG_SIT + "', '" + LOG_STAND + "') AND " + KEY_ID + " > (SELECT " + KEY_ID + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " = '" + LOG_START_DAY + "' ORDER BY " + KEY_ID + " DESC LIMIT 1) ORDER BY " + KEY_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        DBLog result = null;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = new DBLog(cursor.getString(0), stringToDate(cursor.getString(1)), cursor.getDouble(2));
        }
        if(cursor != null) cursor.close();
        return result;
    }

    public boolean isConnected() {
        Log.i("isConnected", "checking");
        String query = "SELECT " + KEY_ACTION + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " IN('" + LOG_CONNECT + "', '" + LOG_DISCONNECT + "') AND " + KEY_ID + " > (SELECT " + KEY_ID + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " = '" + LOG_START_DAY + "' ORDER BY " + KEY_ID + " DESC LIMIT 1) ORDER BY " + KEY_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        boolean result = false;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getString(0).equals(LOG_CONNECT);
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public Date dayStarted() {
        Log.i("dayStarted", "checking");
        String query = "SELECT " + KEY_ACTION + ", " + KEY_DATETIME + " FROM " + TABLE_LOGS + " WHERE " + KEY_ACTION + " IN('" + LOG_START_DAY + "', '" + LOG_STOP_DAY + "') ORDER BY " + KEY_ID + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        Date result = null;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getString(0).equals(LOG_START_DAY) ? stringToDate(cursor.getString(1)) : null;
        }
        if (cursor != null) cursor.close();
        return result;
    }

    //--------------------------------------------------------PROFILES--------------------------------------------------------------------------

    public void storeProfile(Profile input){
        if(getProfile(input.getId()) != null) {
            updateProfile(input);
        } else {
            ContentValues value = new ContentValues(7);
            if (input.getId() != 0) {
                value.put(KEY_ID, input.getId());
            }
            value.put(KEY_FIRSTNAME, input.getFirstName());
            value.put(KEY_LASTNAME, input.getLastName());
            value.put(KEY_USERNAME, input.getUsername());
            value.put(KEY_EMAIL, input.getEmail());
            value.put(KEY_MONEY, input.getMoney());
            value.put(KEY_EXPERIENCE, input.getExperience());
            value.put(KEY_AVATAR, input.getAvatar());
            value.put(KEY_RANK, input.getRank());
            value.put(KEY_UPDATED, dateToString(input.getLastUpdate()));
            db.insert(TABLE_PROFILES, null, value);
        }
    }

    /**
     * updates the local profiles database with the given input profile where the id from the database matches the id in the given profile
     * when an update of a specific field is not needed, insert null for a string or -1 for an int.
     * if profile.id is -1, nothing happens
     * @param input the input profile
     */
    public void updateProfile(Profile input) {
        if(input != null && input.getId() != -1) {
            ContentValues value = new ContentValues();
            if(input.getFirstName() != null) {
                value.put(KEY_FIRSTNAME, input.getFirstName());
            }
            if(input.getLastName() != null) {
                value.put(KEY_LASTNAME, input.getLastName());
            }
            if(input.getUsername() != null) {
                value.put(KEY_USERNAME, input.getUsername());
            }
            if(input.getEmail() != null) {
                value.put(KEY_EMAIL, input.getEmail());
            }
            if(input.getMoney() != -1) {
                value.put(KEY_MONEY, input.getMoney());
            }
            if(input.getExperience() != -1) {
                value.put(KEY_EXPERIENCE, input.getExperience());
            }
            if(input.getAvatar() != null) {
                value.put(KEY_AVATAR, input.getAvatar());
            }
            if(input.getRank() != -1) {
                value.put(KEY_RANK, input.getRank());
            }
            if(input.getLastUpdate() != null) {
                value.put(KEY_UPDATED, dateToString(input.getLastUpdate()));
            }
            if(value.size() == 0)
                return;
            db.update(TABLE_PROFILES, value, KEY_ID + " = ?", new String[]{Integer.toString(input.getId())});
        }
    }

    public Profile getProfile(int id) {
        String query = "SELECT " + KEY_FIRSTNAME + ", " + KEY_LASTNAME + ", " + KEY_USERNAME + ", " + KEY_EMAIL + ", " + KEY_MONEY + ", " + KEY_EXPERIENCE + ", " + KEY_AVATAR + ", " + KEY_RANK + ", " + KEY_UPDATED + " FROM " + TABLE_PROFILES + " WHERE " + KEY_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {Integer.toString(id)});
        Profile result = null;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = new Profile(id, cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getInt(5), cursor.getString(6), cursor.getInt(7), stringToDate(cursor.getString(8)));
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public Profile getProfile(String username) {
        String query = "SELECT " + KEY_ID + ", " + KEY_FIRSTNAME + ", " + KEY_LASTNAME + ", " + KEY_EMAIL + ", " + KEY_MONEY + ", " + KEY_EXPERIENCE + ", " + KEY_AVATAR + ", " + KEY_RANK + ", " + KEY_UPDATED + " FROM " + TABLE_PROFILES + " WHERE " + KEY_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {username});
        Profile result = null;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = new Profile(cursor.getInt(0), cursor.getString(1), cursor.getString(2), username, cursor.getString(3), cursor.getInt(4), cursor.getInt(5), cursor.getString(6), cursor.getInt(7), stringToDate(cursor.getString(8)));
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public ArrayList<Profile> getLeaderboardByRank(int rank) {
        ArrayList<Profile> prof = null;
        rank -= (rank % 10) == 0 ? 10 : rank % 10;
        String query = "SELECT " + KEY_ID + ", " + KEY_FIRSTNAME + ", " + KEY_LASTNAME + ", " + KEY_USERNAME + ", " + KEY_EMAIL + ", " + KEY_MONEY + ", " + KEY_EXPERIENCE + ", " + KEY_AVATAR + ", " + KEY_RANK + ", " + KEY_UPDATED + " FROM " + TABLE_PROFILES + " ORDER BY " + KEY_RANK + " ASC LIMIT ?, 10";
        Cursor cursor = db.rawQuery(query, new String[]{Integer.toString(rank)});
        if(cursor != null && cursor.getCount() == 10) {
            prof = new ArrayList<>(10);
            while(cursor.moveToNext()) {
                prof.add(new Profile(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5), cursor.getInt(6), cursor.getString(7), cursor.getInt(8), stringToDate(cursor.getString(9))));
            }
        }
        if (cursor != null) cursor.close();
        return prof == null ? null : prof.size() == 10 ? prof : null;
    }

    public ArrayList<Profile> getProfilesByIds(int[] ids) {
        ArrayList<Profile> prof = null;
        String idString = Arrays.toString(ids);
        idString = idString.substring(1, idString.length() - 1);
        String query = "SELECT " + KEY_ID + ", " + KEY_FIRSTNAME + ", " + KEY_LASTNAME + ", " + KEY_USERNAME + ", " + KEY_EMAIL + ", " + KEY_MONEY + ", " + KEY_EXPERIENCE + ", " + KEY_AVATAR + ", " + KEY_RANK + ", " + KEY_UPDATED + " FROM " + TABLE_PROFILES + " WHERE " + KEY_ID + " IN (" + idString + ")";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor != null && cursor.getCount() == ids.length) {
            prof = new ArrayList<>(ids.length);
            while(cursor.moveToNext()) {
                prof.add(new Profile(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5), cursor.getInt(6), cursor.getString(7), cursor.getInt(8), stringToDate(cursor.getString(9))));
            }
        }
        if (cursor != null) cursor.close();
        return prof == null ? null : prof.size() == ids.length ? prof : null;
    }

    //--------------------------------------------------------SETTINGS--------------------------------------------------------------------------

    private int getIntSetting(String name) {
        String query = "SELECT " + KEY_VALUE_INT + " FROM " + TABLE_SETTINGS + " WHERE " + KEY_SETTING + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name});
        int result = -1;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
        }
        if (cursor != null) cursor.close();
        return result;
    }

    private String getStringSetting(String name) {
        String query = "SELECT " + KEY_VALUE_STRING + " FROM " + TABLE_SETTINGS + " WHERE " + KEY_SETTING + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name});
        String result = null;
        if(cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getString(0);
        }
        if (cursor != null) cursor.close();
        return result;
    }

    private void setIntSetting(String name, int value) {
        ContentValues input = new ContentValues(1);
        input.put(KEY_VALUE_INT, value);
        db.update(TABLE_SETTINGS, input, KEY_SETTING + " = ?", new String[]{name});
    }

    private void setStringSetting(String name, String value) {
        ContentValues input = new ContentValues(1);
        input.put(KEY_VALUE_STRING, value);
        db.update(TABLE_SETTINGS, input, KEY_SETTING + " = ?", new String[]{name});
    }

    public int getOwnerId() {
        return getIntSetting(OWNER);
    }

    public void setOwnerId(int newOwnerId) {
        if(newOwnerId != getOwnerId()) {
            setIntSetting(OWNER, newOwnerId);
            truncateLogs();
            truncateLC();
            synchronized (StApp.challenges) {
                StApp.challenges.clear();
            }
        }
    }

    public Profile getOwner() {
        return getProfile(getOwnerId());
    }

    public boolean getNotification() {
        return getIntSetting(NOTIF) == 1;
    }

    public void setNotification(boolean notification) {
        setIntSetting(NOTIF, notification ? 1 : 0);
    }

    public String getToken() {
        return getStringSetting(TOKEN);
    }

    public void setToken(String newToken) {
        setStringSetting(TOKEN, newToken);
    }

    public String getLastEnteredUsername() {
        return getStringSetting(LAST_ENTERED_USERNAME);
    }

    public void setLastEnteredUsername(String newUsername) {
        setStringSetting(LAST_ENTERED_USERNAME, newUsername);
    }

    public boolean uploadOn3G() {
        return getIntSetting(UPLOAD3G) == 0;
    }

    public void setUploadOn3G(boolean uploadOn3G) {
        setIntSetting(NOTIF, uploadOn3G ? 1 : 0);
    }

    public int getUploadFrequency() {
        return getIntSetting(UPLOAD_FREQUENCY);
    }

    public void setUploadFrequency(int uploadFrequency) {
        setIntSetting(UPLOAD_FREQUENCY, uploadFrequency);
    }

    public void setProfileActivityGraphSettings(boolean visible){
        if(visible){
            setIntSetting(PROFILE_GRAPH, 1);
        }else{
            setIntSetting(PROFILE_GRAPH, 0);
        }
    }

    public void setProfileActivityOpenChallengeSettings(boolean visible){
        if(visible){
            setIntSetting(PROFILE_OPEN_CHALLENGE,1);
        }else{
            setIntSetting(PROFILE_OPEN_CHALLENGE,0);
        }
    }

    public int getProfileActivityGrapSetting(){
        return getIntSetting(PROFILE_GRAPH);
    }

    public int getProfileActivityOpenChallengeSetting(){
        return getIntSetting(PROFILE_GRAPH);
    }

    public String getSensor() {
        return getStringSetting(LAST_USED_SENSOR);
    }

    public void setSensor(String newSensor) {
        setStringSetting(LAST_USED_SENSOR, newSensor);
    }

    //-----------------------------------------------------LIVECHALLENGES-----------------------------------------------------------------------

    public void truncateLC() {
        db.execSQL("DELETE FROM " + TABLE_LC);
        db.execSQL("DELETE FROM " + TABLE_OPPONENTSTATUS);
        db.execSQL("DELETE FROM sqlite_sequence where name='" + TABLE_LC + "'");
        db.execSQL("DELETE FROM sqlite_sequence where name='" + TABLE_OPPONENTSTATUS + "'");
    }

    public void createLC(LiveChallenge lc) {
        ContentValues input = new ContentValues(4);
        input.put(KEY_UNIQUE_ID, lc.getUniqueId());
        input.put(KEY_CHALLENGE_ID, lc.getChallenge().getId());
        input.put(KEY_STATUS, lc.getMyStatus().name());
        input.put(KEY_DATA, lc.getMyStatusData());
        input.put(KEY_STATUS_MESSAGE, lc.getStatusMessage());
        db.insert(TABLE_LC, null, input);
        for(Map.Entry<Integer, ChallengeStatus> entry : lc.getOpponentStatus().entrySet()) {
            input.clear();
            input.put(KEY_UNIQUE_ID, lc.getUniqueId());
            input.put(KEY_OPPONENT_ID, entry.getKey());
            input.put(KEY_STATUS, entry.getValue().getStatus().name());
            input.put(KEY_DATA, entry.getValue().getData());
            db.insert(TABLE_OPPONENTSTATUS, null, input);
        }
    }

    public void updateLC(String uniqueId, Status status, String data, String statusMessage) {
        ContentValues input = new ContentValues(3);
        input.put(KEY_STATUS, status.name());
        input.put(KEY_DATA, data);
        input.put(KEY_STATUS_MESSAGE, statusMessage);
        db.update(TABLE_LC, input, KEY_UNIQUE_ID + " = ?", new String[]{uniqueId});
    }

    public void removeLC(String uniqueId) {
        db.delete(TABLE_LC, KEY_UNIQUE_ID + " = ?", new String[]{uniqueId});
        db.delete(TABLE_OPPONENTSTATUS, KEY_UNIQUE_ID + " = ?", new String[]{uniqueId});
    }

    public void updateOpponent(String uniqueId, int opponentId, Status status, String data) {
        ContentValues input = new ContentValues(2);
        input.put(KEY_STATUS, status.name());
        input.put(KEY_DATA, data);
        db.update(TABLE_OPPONENTSTATUS, input, KEY_UNIQUE_ID + " = ? AND " + KEY_OPPONENT_ID + " = ?", new String[]{uniqueId, Integer.toString(opponentId)});
    }

    public Map<String, LiveChallenge> getLiveChallenges() {
        String query = "SELECT " + KEY_UNIQUE_ID + ", " + KEY_CHALLENGE_ID + ", " + KEY_STATUS + ", " + KEY_DATA + ", " + KEY_STATUS_MESSAGE + " FROM " + TABLE_LC;
        Cursor cursor = db.rawQuery(query, null);
        Map<String, LiveChallenge> result = new HashMap<>();
        if(cursor != null && cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                String query2 = "SELECT " + KEY_OPPONENT_ID + ", " + KEY_STATUS + ", " + KEY_DATA + " FROM " + TABLE_OPPONENTSTATUS + " WHERE " + KEY_UNIQUE_ID + " = '" + cursor.getString(0) + "'";
                Cursor cursor2 = db.rawQuery(query2, null);
                Map<Integer, ChallengeStatus> resultOpponents = new HashMap<>();
                if(cursor2 != null && cursor2.getCount() > 0) {
                    while(cursor2.moveToNext()) {
                        resultOpponents.put(cursor2.getInt(0), new ChallengeStatus(Status.valueOf(cursor2.getString(1)), cursor2.getString(1)));
                    }
                }
                if(cursor2 != null) cursor2.close();
                result.put(cursor.getString(0), new LiveChallenge(cursor.getString(0), cursor.getInt(1), Status.valueOf(cursor.getString(2)), cursor.getString(3), resultOpponents, cursor.getString(4)));
            }
        }
        if (cursor != null) cursor.close();
        return result;
    }

    public List<Integer> getOpponentIds(){
        List<Integer> opponentIdsList = new ArrayList<>();
        String query = "SELECT " + KEY_OPPONENT_ID + " , " + KEY_STATUS + " FROM " + TABLE_OPPONENTSTATUS;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0){
            while(cursor.moveToNext()){
                if(!cursor.getString(1).equals(Status.NOT_ACCEPTED)) {
                    opponentIdsList.add(cursor.getInt(0));
                }
            }
            cursor.close();
        }
        return opponentIdsList;
    }
}