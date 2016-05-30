package com.tomandfelix.stapp2.persistency;

import android.os.Handler;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tomandfelix.stapp2.application.StApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Flixse on 27/01/2015.
 */
public class Solo extends Quest{
    public enum Difficulty {EASY, MEDIUM, HARD}
    static public final int STAND_TO_WIN = 1;
    static public final int RANDOM_STAND_UP = 2;
    static public final int RANDOM_SWITCH = 3;
    static public final int ENDURANCE = 4;
    static public final int EARN_YOUR_SITTING_TIME = 5;
    static public final int EARN_DURATION_TIME = 6;
    static public final int SOLVE_QUESTION_FOR_MORE_XP = 7;
    private int kind;
    private int xp;
    private int xpNeeded;
    private int duration;
    private Difficulty difficulty;
    private double progress;
    private Object data;
    private Processor processor;
    private Handler handler;
    private List<Quiz> questions;
    private double multiplier;
    private int answersCorrect;
    private boolean started;

    public Solo(int id,int kind, int xp,int xpNeeded, int duration, Difficulty difficulty, Processor processor){
        super(id, Type.SOLO);
        this.kind = kind;
        this.xp = xp;
        this.xpNeeded = xpNeeded;
        this.duration = duration;
        this.difficulty = difficulty;
        this.progress = 0.0;
        this.multiplier = 0.8;
        this.answersCorrect = 0;
        this.processor = processor;
        questions = new ArrayList<>();
        started = false;
    }

    public int getKind() {
        return kind;
    }

    public int getxp() {
        return xp;
    }

    public boolean isSoloStarted(){
        return this.started;
    }

    public int getXpNeeded() {
        return xpNeeded;
    }

    public int getDuration() {
        return duration;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        Log.d("solo", "Progress=" + progress);
        this.progress = progress;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<Quiz> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Quiz> questions) {
        this.questions = questions;
    }

    public double getMultiplier() {
        return multiplier + 0.02 * getAnswersCorrect();
    }

    public int getAnswersCorrect() {
        return answersCorrect;
    }

    public void incrementAnswersCorrect() {
        this.answersCorrect ++;

    }

    public Handler getHandler() {
        return handler;
    }

    public void start() {
        this.started = true;
        handler = new Handler(StApp.getChallengeLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                processor.start(Solo.this);
                Log.d("in run","true");
            }
        });
    }

    public void won() {
        this.answersCorrect = 0;
        StApp.makeToast("QUEST_WON");
        data = "QUEST_WON";
        Log.d("Solo", "Quest complete, you have won!");
        ServerHelper.getInstance().updateMoneyAndExperience(0, DatabaseHelper.getInstance().getOwner().getExperience() + (int) Math.round(xp * getMultiplier()), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        });
        stop();
    }

    public void lost() {
        this.answersCorrect = 0;
        StApp.makeToast("QUEST_LOST");
        data = "QUEST_LOST";
        Log.d("Solo", "Quest complete, you have lost!");
        stop();
    }

    private void stop() {
        this.answersCorrect = 0;
        this.started = false;
        handler.removeCallbacksAndMessages(null);
        handler = null;
        this.multiplier = 0.8;
        progress = 0;
    }

    public void clear() {
        data = null;
    }

    @Override
    public String toString() {
        String info = "";
        info += "id:" + id;
        info += " experience:" + xp;
        info += " duration:" + duration;
        info += " difficulty:";
        switch(difficulty) {
            case EASY:
                info += "EASY";
                break;
            case MEDIUM:
                info += "MEDIUM";
                break;
            case HARD:
                info += "HARD";
                break;
        }
        return info;
    }

    public static abstract class Processor {
        public abstract void start(Solo solo);
    }
}
