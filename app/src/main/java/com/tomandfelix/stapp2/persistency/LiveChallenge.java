package com.tomandfelix.stapp2.persistency;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.common.primitives.Ints;
import com.tomandfelix.stapp2.activity.OpenChallenge;
import com.tomandfelix.stapp2.activity.OpenChallengesFragment;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.persistency.ChallengeStatus.Status;
import com.tomandfelix.stapp2.persistency.GCMMessage.MessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Tom on 30/06/2015.
 */
public class LiveChallenge extends Handler {
    private static final String TAG = LiveChallenge.class.getSimpleName();
    private String uniqueId;
    private Challenge challenge;
    private ChallengeStatus myStatus;
    private Map<Integer, ChallengeStatus> opponentStatus;
    public static final String WAIT_FOR_RESULT_MSG = "Waiting for results from other players";

    private String statusMessage;

    public LiveChallenge(int id, int[] opponents) {
        this(UUID.randomUUID().toString(), id, opponents);
    }

    public LiveChallenge(String uniqueId, int id, int[] opponents) {
        super(StApp.getChallengeLooper());
        this.uniqueId = uniqueId;
        challenge = ChallengeList.getChallenge(id);
        myStatus = new ChallengeStatus(Status.NOT_ACCEPTED, null);
        opponentStatus = new HashMap<>(opponents.length);
        for(int opp : opponents) {
            opponentStatus.put(opp, new ChallengeStatus(Status.NOT_ACCEPTED, null));
        }
        statusMessage = null;
        DatabaseHelper.getInstance().createLC(this);
    }

    public LiveChallenge(String uniqueId, int id, Status status, String data, Map<Integer, ChallengeStatus> opponentStatus, String statusMessage) {
        this.uniqueId = uniqueId;
        this.challenge = ChallengeList.getChallenge(id);
        myStatus = new ChallengeStatus(status, data);
        this.opponentStatus = opponentStatus;
        this.statusMessage = statusMessage;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public int[] getOpponents() {
        return Ints.toArray(opponentStatus.keySet());
    }

    public Status getMyStatus() {
        return myStatus.getStatus();
    }

    public String getMyStatusData() {
        return myStatus.getData();
    }

    public void setMyStatus(Status status, String data) {
        if(status != null)
            myStatus.setStatus(status);
        if(data != null)
            myStatus.setData(data);
        DatabaseHelper.getInstance().updateLC(uniqueId, myStatus.getStatus(), myStatus.getData(), statusMessage);
    }

    public void setStatusById(int id, Status status, String data) {
        opponentStatus.get(id).setStatus(status);
        if(data != null)
            opponentStatus.get(id).setData(data);
        DatabaseHelper.getInstance().updateOpponent(uniqueId, id, opponentStatus.get(id).getStatus(), opponentStatus.get(id).getData());
    }

    public Map<Integer, ChallengeStatus> getOpponentStatus() {
        return opponentStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        if(OpenChallenge.getHandler() != null) {
            OpenChallenge.getHandler().obtainMessage(OpenChallenge.MSG_REFRESH).sendToTarget();
        }
    }

    public boolean hasEveryoneAccepted() {
        if(myStatus.getStatus() == Status.NOT_ACCEPTED) {
            return false;
        }
        for(ChallengeStatus s : opponentStatus.values()) {
            if(s.getStatus() == Status.NOT_ACCEPTED) {
                return false;
            }
        }
        return true;
    }

    public boolean isEverybodyDone() {
        if(myStatus.getStatus() != Status.DONE) {
            return false;
        }
        for(ChallengeStatus s : opponentStatus.values()) {
            if(s.getStatus() != Status.DONE) {
                return false;
            }
        }
        return true;
    }

    public void request() {
        post(new Runnable() {
            @Override
            public void run() {
                setMyStatus(Status.ACCEPTED, null);
                sendMessage(MessageType.REQUEST, Integer.toString(challenge.getId()));
            }
        });
    }

    public void accept() {
        post(new Runnable() {
            @Override
            public void run() {
                setMyStatus(Status.ACCEPTED, null);
                sendMessage(MessageType.ACCEPT, null);
                if (OpenChallenge.getHandler() != null) {
                    OpenChallenge.getHandler().obtainMessage(OpenChallenge.MSG_REFRESH).sendToTarget();
                }
            }
        });
    }

    public void decline() {
        post(new Runnable() {
            @Override
            public void run() {
                sendMessage(MessageType.DECLINE, null);
                StApp.challenges.remove(uniqueId);
                removeCallbacksAndMessages(null);
            }
        });
    }

    public void start() {
        post(new Runnable() {
            @Override
            public void run() {
                challenge.getProcessor().start(LiveChallenge.this);
            }
        });
    }

    public void won(String message) {
        ServerHelper.getInstance().updateMoneyAndExperience(0, DatabaseHelper.getInstance().getOwner().getExperience() + challenge.getxp(), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        scored(message);
    }

    public void wonCustomXP(String message, int xp) {
        ServerHelper.getInstance().updateMoneyAndExperience(0, DatabaseHelper.getInstance().getOwner().getExperience() + xp, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
        scored(message);
    }

    public void lost(String message) {
        scored(message);
    }

    private void scored(String message) {
        setMyStatus(Status.SCORED, null);
        statusMessage = message;
        Log.d("Challenge", message);
        if(OpenChallenge.getHandler() != null) {
            OpenChallenge.getHandler().obtainMessage(OpenChallenge.MSG_REFRESH).sendToTarget();
        } else {
            StApp.makeToast(message);
        }
    }

    public void postGCMmessage(final GCMMessage msg) {
        post(new Runnable() {
            @Override
            public void run() {
                switch (msg.getMessageType()) {
                    case DECLINE:
                        StApp.challenges.remove(uniqueId);
                        DatabaseHelper.getInstance().removeLC(uniqueId);
                        removeCallbacksAndMessages(null);
                        if(OpenChallengesFragment.hasAdapter()) {
                            OpenChallengesFragment.getBoundedView().post(new Runnable() {
                                @Override
                                public void run() {
                                    OpenChallengesFragment.getAdapter().notifyDataSetChanged();
                                }
                            });
                        }
                        break;
                    case ACCEPT:
                        setStatusById(msg.getSenderId(), Status.ACCEPTED, null);
                        break;
                    case STOP:
                        StApp.challenges.remove(uniqueId);
                        DatabaseHelper.getInstance().removeLC(uniqueId);
                        removeCallbacksAndMessages(null);
                        if(OpenChallengesFragment.hasAdapter()) {
                            OpenChallengesFragment.getBoundedView().post(new Runnable() {
                                @Override
                                public void run() {
                                    OpenChallengesFragment.getAdapter().notifyDataSetChanged();
                                }
                            });
                        }
                        break;
                    case COMMUNICATION:
                        challenge.getProcessor().handleCommunicationMessage(LiveChallenge.this, msg);
                        break;
                    case RESULT:
                        setStatusById(msg.getSenderId(), Status.DONE, msg.getMessage());
                        if (isEverybodyDone()) {
                            challenge.getProcessor().onEverybodyDone(LiveChallenge.this);
                        }
                        break;
                }
                if (OpenChallenge.getHandler() != null) {
                    OpenChallenge.getHandler().obtainMessage(OpenChallenge.MSG_REFRESH).sendToTarget();
                }
            }
        });
    }

    public void sendMessage(MessageType messageType, String message) {
        final GCMMessage msg = new GCMMessage(getOpponents(), uniqueId, messageType, -1, message);
        ServerHelper.getInstance().sendMessage(msg, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError){
                if(volleyError != null && volleyError.getMessage() != null) {
                    if (volleyError.getMessage().equals("none")) {
                        Log.d(TAG, "Sent: " + msg.toString());
                    } else {
                        Log.e(TAG, volleyError.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void handleMessage(Message msg) {
        challenge.getProcessor().handleLoggingMessage(LiveChallenge.this, msg);
    }
}
