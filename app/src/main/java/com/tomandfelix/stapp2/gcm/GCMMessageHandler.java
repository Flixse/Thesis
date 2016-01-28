package com.tomandfelix.stapp2.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.activity.OpenChallenge;
import com.tomandfelix.stapp2.activity.OpenChallengesFragment;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.persistency.ChallengeStatus.Status;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.GCMMessage;
import com.tomandfelix.stapp2.persistency.GCMMessage.MessageType;
import com.tomandfelix.stapp2.persistency.LiveChallenge;

import org.json.JSONArray;
import org.json.JSONException;

public class GCMMessageHandler extends IntentService {
    private static NotificationCompat.Builder mBuilder = null;
    private static NotificationManager notificationManager = null;

    public GCMMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(mBuilder == null) {
            mBuilder = new NotificationCompat.Builder(GCMMessageHandler.this);
            mBuilder.setSmallIcon(R.drawable.icon_notification);
            mBuilder.setContentTitle("Stapp 2");
            mBuilder.setAutoCancel(true);
        }
        if(notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        int[] recIds = null;
        try {
            JSONArray recIdsJSON = new JSONArray(extras.getString("receiver_ids"));
            recIds = new int[recIdsJSON.length()];
            for(int i = 0; i < recIdsJSON.length(); i++) {
                recIds[i] = recIdsJSON.getInt(i);
            }
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
        try {
            GCMMessage message = new GCMMessage(recIds,
                    extras.getString("challenge_unique_id"),
                    MessageType.valueOf(extras.getString("message_type")),
                    Integer.parseInt(extras.getString("sender_id")),
                    extras.getString("message"));
            Log.d("GCM", "Received : " + message.toString());
            if(message.getMessageType() == MessageType.TEST_TOAST) {
                StApp.makeToast(extras.getString("message"));
            } else {
                if(message.getMessageType() == MessageType.REQUEST) {
                    handleRequest(message);
                } else {
                    synchronized (StApp.challenges) {
                        LiveChallenge challenge = StApp.challenges.get(message.getUniqueId());
                        if(challenge != null)
                            challenge.postGCMmessage(message);
                    }
                }
            }
        } catch (NullPointerException e) {
            return;
        }

        GCMBroadCastReceiver.completeWakefulIntent(intent);
    }

    private void handleRequest(GCMMessage message) {
        int[] opponents = message.getReceivers();
        int me = DatabaseHelper.getInstance().getOwnerId();
        for(int i = 0; i < opponents.length; i++) {
            if(opponents[i] == me) {
                opponents[i] = message.getSenderId();
            }
        }
        StApp.challenges.put(message.getUniqueId(), new LiveChallenge(message.getUniqueId(), Integer.parseInt(message.getMessage()), opponents));
        StApp.challenges.get(message.getUniqueId()).setStatusById(message.getSenderId(), Status.ACCEPTED, null);
        if(OpenChallengesFragment.hasAdapter()) {
            OpenChallengesFragment.getBoundedView().post(new Runnable() {
                @Override
                public void run() {
                    OpenChallengesFragment.getAdapter().notifyDataSetChanged();
                }
            });
        } else {
            PendingIntent pendingIntent;
            mBuilder.setContentText("You have a new challenge");
            Intent intent = new Intent(this, OpenChallenge.class);
            intent.putExtra("challenge_unique_index", message.getUniqueId());
            if(Build.VERSION.SDK_INT >= 16) {
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(OpenChallenge.class);
                stackBuilder.addNextIntent(intent);
                pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            mBuilder.setContentIntent(pendingIntent);
            notificationManager.notify(1, mBuilder.build());
        }
    }
}