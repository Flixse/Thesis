//v0.2 -  8 January 2013

/*
 * Copyright (c) 2010, Shimmer Research, Ltd.
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:

 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of Shimmer Research, Ltd. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Jong Chern Lim
 * @date   October, 2013
 */

//Future updates needed
//- the handler should be converted to static

package com.tomandfelix.stapp2.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tomandfelix.stapp2.driver.*;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.IdLog;
import com.tomandfelix.stapp2.persistency.ServerHelper;
import com.tomandfelix.stapp2.tools.Logging;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class ShimmerService extends Service {
    private static final String TAG = ShimmerService.class.getSimpleName();
    private static final boolean mEnableLogging=true;
    private Shimmer sensor;
    private static String address = "";
    private int retryAmount = 0;
    private Handler uploadHandler;
    private Runnable uploadCheck;
    private int uploadFrequency;
    public static final int REGISTER_MESSENGER = 0;
    public static final int UPLOAD_DATA = 1;
    public static final int DOWNLOAD_DATA = 2;
    public static final int CONNECT = 3;
    public static final int PAUSE = 4;
    public static final int END_DAY = 5;
    public static final int REQUEST_STATE = 6;
    public static final int LOG_START_DAY = 7;
    public static final int XP_REQUEST = 8;
    private Handler handler = new Handler(Looper.getMainLooper());
    final Runnable tryReconnectWithPause = new Runnable() {
        public void run() {
            retryAmount = 0;
            tryReconnect();
        }
    };

    private static Messenger toApp;
    private static class AppHandler extends Handler {
        private WeakReference<ShimmerService> mShimmerService;

        public AppHandler(ShimmerService aShimmerService) {
            mShimmerService = new WeakReference<>(aShimmerService);
        }
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case REGISTER_MESSENGER:
                    toApp = msg.replyTo;
                    break;
                case UPLOAD_DATA:
                    mShimmerService.get().uploadData();
                    break;
                case DOWNLOAD_DATA:
                    mShimmerService.get().downloadData();
                    break;
                case CONNECT:
                    mShimmerService.get().handler.removeCallbacksAndMessages(null);
                    address = msg.getData().getString("address");
                    mShimmerService.get().connect();
                    break;
                case PAUSE:
                    mShimmerService.get().disconnect();
                    break;
                case REQUEST_STATE:
                    mShimmerService.get().sendToApp(Logging.getInstance(mShimmerService.get()).getState());
                    break;
                case LOG_START_DAY:
                    Logging.getInstance(mShimmerService.get()).logStartDay();
                    break;
                case END_DAY:
                    mShimmerService.get().endDay();
                    break;
                case XP_REQUEST:
                    mShimmerService.get().sendXPToApp(Logging.getInstance(mShimmerService.get()).getCurrentXP());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    private final Messenger mMessenger = new Messenger(new AppHandler(this));

    public void sendToApp(int state) {
        if(toApp != null) {
            try {
                toApp.send(Message.obtain(null, state));
                Log.d(TAG, "State=" + state);
            } catch (RemoteException e) {
                toApp = null;
            }
        }
    }

    public void sendXPToApp(int xp) {
        if(toApp != null) {
            try {
                toApp.send(Message.obtain(null, 100, xp, -1));
            } catch (RemoteException e) {
                toApp = null;
            }
        }
    }

    public void tryReconnect() {
        if(address.equals("")) {
            disconnect();
        } else if (retryAmount < 7) {
            Log.e(TAG, "RETRY " + retryAmount);
            retryAmount++;
            connect();
        } else {
            Log.e(TAG, "waiting 30 seconds to RETRY");
            handler.postDelayed(tryReconnectWithPause, 30000);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        uploadFrequency = DatabaseHelper.getInstance().getUploadFrequency();
        uploadHandler = new Handler();
        uploadCheck = new Runnable() {
            @Override
            public void run() {
                uploadHandler.postDelayed(uploadCheck, uploadFrequency);
                uploadData();
            }
        };
        Log.d(TAG, "onCreate");
        if(DatabaseHelper.getInstance().dayStarted() == null) {
            address = "";
        }
    }

    private void uploadData() {
        if(ServerHelper.getInstance().checkInternetConnection()) {
            ArrayList<IdLog> logs = DatabaseHelper.getInstance().getLogsToUpload();
            if(logs != null) {
                Log.d(TAG, "uploading records " + logs.get(0).getId() + "->" + logs.get(logs.size() - 1).getId());
                final boolean more = logs.size() == 1000;
                ServerHelper.getInstance().uploadLogs(logs, new ServerHelper.ResponseFunc<Integer>() {
                    @Override
                    public void onResponse(Integer response) {
                        DatabaseHelper.getInstance().confirmUpload(response);
                        if(more) {
                            uploadData();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if(volleyError.getMessage() != null && volleyError.getMessage().equals("token")) {
                            // TODO password prompt
                        } else {
                            if(volleyError.getMessage() != null) {
                                Log.e("uploadData", volleyError.getMessage());
                            } else {
                                if(volleyError.networkResponse != null) {
                                    Log.e("uploadDat", Integer.toString(volleyError.networkResponse.statusCode));
                                } else {
                                    Log.e("uploadData", "Something went wrong, we don't know exactly what.");
                                }
                            }
                        }
                    }
                });
            } else {
                int state = Logging.getInstance(this).getState();
                if(state == Logging.STATE_CONNECTING || state == Logging.STATE_DISCONNECTED || state == Logging.STATE_DAY_STOPPED) {
                    stopUploadTask();
                }
            }
        }
    }

    private void downloadData() {
        IdLog last = DatabaseHelper.getInstance().getLastLog();
        ServerHelper.getInstance().downloadLogs(last == null ? 0 : last.getId(), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (volleyError.getMessage().equals("none")) {
                    Logging.getInstance(ShimmerService.this).checkDB();
                    uploadData();
                } else {
                    Log.e("downloadData", volleyError.getMessage());
                }
            }
        });
    }

    private void startUploadTask() {
        Log.d(TAG, "starting upload task");
        uploadHandler.removeCallbacks(uploadCheck);
        uploadCheck.run();
    }

    private void stopUploadTask() {
        Log.d(TAG, "stopping upload task");
        uploadHandler.removeCallbacks(uploadCheck);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        sensor.stop();
        sensor = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        if(DatabaseHelper.getInstance().dayStarted() != null) {
            connect();
        }
        return START_STICKY;
    }

    public void connect() {
        if(sensor == null) {
            if(!address.equals("")){
                sensor = new Shimmer(this, mHandler, "Sensor", false);
                sensor.connect(address);
            }
        } else if(sensor.getShimmerState() == Shimmer.STATE_CONNECTED && !sensor.getStreamingStatus()) {
            Logging.getInstance(this).logConnecting();
            sensor.startStreaming();
        } else {
            sensor.stop();
            sensor = null;
        }
    }

    public void disconnect() {
        if(sensor != null) {
            if(sensor.getShimmerState() == Shimmer.STATE_CONNECTED && sensor.getStreamingStatus()) {
                sensor.stopStreaming();
            } else if(address.equals("")){
                sensor.stop();
                sensor = null;
            }
        }
    }

    public void endDay() {
        address = "";
        if(sensor != null)
            sensor.stop();
        Logging.getInstance(this).logDisconnect();
        Logging.getInstance(this).logAchievedScore();
    }

    public final Handler mHandler = new ShimmerHandler(this);

    private static class ShimmerHandler extends Handler {
        private WeakReference<ShimmerService> mShimmerService;

        public ShimmerHandler(ShimmerService aShimmerService) {
            mShimmerService = new WeakReference<>(aShimmerService);
        }

        @Override
        public void handleMessage(Message msg) {
            if(mShimmerService.get() != null) {
                switch (msg.what) { // handlers have a what identifier which is used to identify the type of msg
                    case Shimmer.MESSAGE_READ:
                        if ((msg.obj instanceof ObjectCluster)) {    // within each msg an object can be include, objectclusters are used to represent the data structure of the shimmer device
                            ObjectCluster objectCluster = (ObjectCluster) msg.obj;
                            if (mEnableLogging) {
                                Logging.getInstance(mShimmerService.get()).logData(objectCluster);
                            }
                        }
                        break;
                    case Shimmer.MESSAGE_TOAST:
                        break;
                    case Shimmer.MESSAGE_STATE_CHANGE:
                        Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                        switch (msg.arg1) {
                            case Shimmer.STATE_CONNECTING:
                                Logging.getInstance(mShimmerService.get()).logConnecting();
                                break;
                            case Shimmer.STATE_CONNECTED:
                                break;
                            case Shimmer.MSG_STATE_FULLY_INITIALIZED:
                                mShimmerService.get().connect();
                                break;
                            case Shimmer.MSG_STATE_STREAMING:
                                Logging.getInstance(mShimmerService.get()).logConnect();
                                mShimmerService.get().startUploadTask();
                                mShimmerService.get().retryAmount = 0;
                                break;
                            case Shimmer.MSG_STATE_STOP_STREAMING:
                                Logging.getInstance(mShimmerService.get()).logDisconnect();
                                break;
                            case Shimmer.STATE_NONE:
                                Logging.getInstance(mShimmerService.get()).logDisconnect();
                                mShimmerService.get().tryReconnect();
                                break;
                        }
                        break;
                    case Shimmer.MESSAGE_STOP_STREAMING_COMPLETE:
                        break;
                }
            }
        }
    }
}
