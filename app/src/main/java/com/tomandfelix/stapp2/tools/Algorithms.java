package com.tomandfelix.stapp2.tools;

import com.tomandfelix.stapp2.persistency.DBLog;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Tom on 27/1/2015.
 */
public class Algorithms {

    public static long millisecondsStood(ArrayList<DBLog> logs) {
        long result = 0;
        for(int i = 1; i < logs.size(); i++) {
            if(logs.get(i).getAction().equals(DatabaseHelper.LOG_SIT)) {
                if(logs.get(i - 1).getAction().equals(DatabaseHelper.LOG_STAND)) {
                    result += logs.get(i).getDatetime().getTime() - logs.get(i - 1).getDatetime().getTime();
                } //else LOG_CONNECT, which is considered a sit.
            } else if(logs.get(i).getAction().equals(DatabaseHelper.LOG_DISCONNECT)) {
                if(logs.get(i - 1).getAction().equals(DatabaseHelper.LOG_STAND)) {
                    result += logs.get(i).getDatetime().getTime() - logs.get(i - 1).getDatetime().getTime();
                } // else LOG_SIT or LOG_CONNECT, which are considered a sit
            } // else LOG_STAND, in which case the previous one is LOG_SIT or LOG_CONNECT, which are considered a sit.
        }
        return result;
    }

    public static long millisecondsStood(Date start, Date end) {
        ArrayList<DBLog> logs = DatabaseHelper.getInstance().getLogsBetween(start, end);
        DBLog logBefore = DatabaseHelper.getInstance().getLastLogBefore(start);
        long result = logs == null ? 0 : millisecondsStood(logs);
        if(logs != null && logs.size() > 0) {
            if (logBefore.getAction().equals(DatabaseHelper.LOG_STAND)) {
                result += logs.get(0).getDatetime().getTime() - start.getTime();
            }
            if (logs.get(logs.size() - 1).getAction().equals(DatabaseHelper.LOG_STAND)) {
                result += end.getTime() - logs.get(logs.size() - 1).getDatetime().getTime();
            }
        } else if(logBefore.getAction().equals(DatabaseHelper.LOG_STAND)) {
            result = end.getTime() - start.getTime();
        }
        return result;
    }
}
