package com.tomandfelix.stapp2.persistency;


import android.os.Message;

/**
 * Created by Flixse on 27/01/2015.
 */
public class Challenge extends Quest {
    static public final int ONE_ON_ONE = 1;
    static public final int GROUP_COMPETITION = 2;
    static public final int FOLLOW_THE_TRACK = 3;
    static public final int ALTERNATELY_STANDING = 4;
    private int kind;
    private int minAmount;
    private int maxAmount;
    private int xp;
    private int duration;
    private boolean showProgressBar;
    private boolean showOpponentStatusIcons;
    private double progress;
    private Processor processor;

    public Challenge(int id, int kind, int minAmount, int maxAmount,int xp, int duration, Type type, boolean showProgressBar, boolean showOpponentStatusIcons, Processor validator){
        super(id, type);
        this.kind = kind;
        this.progress = 0;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.xp = xp;
        this.duration = duration;
        this.showProgressBar = showProgressBar;
        this.showOpponentStatusIcons = showOpponentStatusIcons;
        this.processor = validator;
    }

    public int getKind(){
        return kind;
    }

    public int getxp() {
        return xp;
    }

    public void setxp(int xp) {
        this.xp = xp;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public int getDuration() {
        return duration;
    }

    public boolean showProgress() {
        return showProgressBar;
    }

    public boolean showOpponentStatusIcons() {
        return showOpponentStatusIcons;
    }

    public Processor getProcessor() {
        return processor;
    }

    @Override
    public String toString() {
        String info = "";
        info += "id:" + id;
        info += " people:" + minAmount + "-" + maxAmount;
        return info;
    }

    public static abstract class Processor {
        abstract void start(LiveChallenge challenge);
        void handleCommunicationMessage(LiveChallenge challenge, GCMMessage msg) {}
        void onEverybodyDone(LiveChallenge challenge) {}
        void handleLoggingMessage(LiveChallenge challenge, Message message) {}
    }
}
