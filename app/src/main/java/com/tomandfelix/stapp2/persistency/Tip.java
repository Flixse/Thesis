package com.tomandfelix.stapp2.persistency;

/**
 * Created by Felix on 26/04/2016.
 */
public class Tip {
    private int tipsId;
    private String text;
    public Tip(int tipsId, String text){
        this.tipsId = tipsId;
        this.text = text;
    }

    public int getTipsId() {
        return tipsId;
    }

    public void setTipsId(int tipsId) {
        this.tipsId = tipsId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
