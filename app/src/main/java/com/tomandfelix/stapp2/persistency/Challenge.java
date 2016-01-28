package com.tomandfelix.stapp2.persistency;


import android.os.Message;

/**
 * Created by Flixse on 27/01/2015.
 */
public class Challenge extends Quest {
    private int minAmount;
    private int maxAmount;
    private int xp;
    private int duration;
    private boolean showProgressBar;
    private boolean showOpponentStatusIcons;
    private double progress;
    private Processor processor;

    public Challenge(int id, String name, String description, int minAmount, int maxAmount,int xp, int duration, Type type, boolean showProgressBar, boolean showOpponentStatusIcons, Processor validator){
        super(id, name, description, type);
        this.progress = 0;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.xp = xp;
        this.duration = duration;
        this.showProgressBar = showProgressBar;
        this.showOpponentStatusIcons = showOpponentStatusIcons;
        this.processor = validator;
    }

    @Override
    public String getDescription() {
        return super.getDescription().replace("<duration>", Integer.toString(duration));
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
        info += (name == null ? "":" name:" + name);
        info += (description == null ? "":" description:" + description);
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
