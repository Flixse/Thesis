package com.tomandfelix.stapp2.persistency;

/**
 * Created by Tom on 30/06/2015.
 */
public class ChallengeStatus {
    public enum Status {
        NOT_ACCEPTED, ACCEPTED, STARTED, DONE, SCORED, STOP
    }

    private Status status;
    private String data;

    public ChallengeStatus(Status status, String data) {
        this.status = status;
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
