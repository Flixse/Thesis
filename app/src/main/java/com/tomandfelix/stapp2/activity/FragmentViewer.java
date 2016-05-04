package com.tomandfelix.stapp2.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.application.StApp;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.Profile;
import com.tomandfelix.stapp2.persistency.ServerHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.tomandfelix.stapp2.service.ShimmerService;

/**
 * Created by Tom on 3/11/2014.
 * This is the first activity, it is a viewpager with 3 fragments: login, start and register
 */
public class FragmentViewer extends FragmentActivity implements FragmentProvider.OnFragmentInteractionListener{
    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private String avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int playServicesResultCode = ((StApp) getApplication()).getPlayServicesResultCode();
        if(playServicesResultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(playServicesResultCode)) {
                GooglePlayServicesUtil.getErrorDialog(playServicesResultCode, this, 9000).show();
            } else {
                finish();
            }
        }
        String token = DatabaseHelper.getInstance().getToken();
        if (token != null && !token.equals("")) {
            Log.d("start", "Token present");
            ServerHelper.getInstance().getProfile(new ServerHelper.ResponseFunc<Profile>() {
                @Override
                public void onResponse(Profile response) {
                    if (response != null) {
                        Log.d("start", "Token accepted");
                        Intent intent = new Intent(FragmentViewer.this, ProfileView.class);
                        intent.putExtra("profile", response);
                        startActivity(intent);
                        ((StApp) getApplication()).commandService(ShimmerService.UPLOAD_DATA);
                        ((StApp) getApplication()).commandService(ShimmerService.DOWNLOAD_DATA);
                        finish();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("start", "Token rejected / No internet connection");
                    loadStart();
                }
            }, true);
        } else {
            Log.d("start", "Token absent");
            loadStart();
        }

        /*ArrayList<DBLog> logs = new ArrayList<>();
        logs.add(new DBLog("sit", new Date(2015, 1, 27, 18, 1, 0), ""));
        logs.add(new DBLog("stand", new Date(2015, 1, 27, 18, 1, 30), ""));
        logs.add(new DBLog("sensor_disconnect", new Date(2015, 1, 27, 18, 1, 45), ""));
        logs.add(new DBLog("sensor_connect", new Date(2015, 1, 27, 18, 2, 0), ""));
        logs.add(new DBLog("stand", new Date(2015, 1, 27, 18, 2, 30), ""));
        logs.add(new DBLog("sit", new Date(2015, 1, 27, 18, 3, 0), ""));
        logs.add(new DBLog("sensor_disconnect", new Date(2015, 1, 27, 18, 4, 0), ""));
        logs.add(new DBLog("sensor_connect", new Date(2015, 1, 27, 18, 5, 0), ""));
        logs.add(new DBLog("sit", new Date(2015, 1, 27, 18, 5, 30), ""));
        logs.add(new DBLog("stand", new Date(2015, 1, 27, 18, 5, 45), ""));
        Log.i("TIME_STOOD", Long.toString(Algorithms.millisecondsStood(logs)));
        Date start = new Date(2015, 1, 27, 18, 0, 45);
        DBLog logBefore = new DBLog ("stand", new Date(2015, 1, 27, 18, 0, 30), "");
        long result = Algorithms.millisecondsStood(logs);
        if(logBefore.getAction().equals(DatabaseHelper.LOG_STAND)) {
            result += logs.get(0).getDatetime().getTime() - start.getTime();
        }
        Log.i("TIME_STOOD SINCE", Long.toString(result));
        Date end = new Date(2015, 1, 27, 18, 6, 0);
        if(logs.get(logs.size() - 1).getAction().equals(DatabaseHelper.LOG_STAND)) {
            result += end.getTime() - logs.get(logs.size() - 1).getDatetime().getTime();
        }
        Log.i("TIME_STOOD SINCE, UNTIL", Long.toString(result));*/
    }

    private void loadStart() {
        setContentView(R.layout.activity_start);
        PagerAdapter mAdapter = new FragmentAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(1, false);
        avatar = "manager";
    }

    public void loginBtn(final View v) {
        if(ServerHelper.getInstance().checkInternetConnection()) {
            v.setEnabled(false);
            final EditText username = (EditText) findViewById(R.id.login_username);
            final EditText password = (EditText) findViewById(R.id.login_password);
            final TextView txtView = (TextView) findViewById(R.id.succes);
            DatabaseHelper.getInstance().setLastEnteredUsername(username.getText().toString());
            Log.d("username + pass",username.getText().toString() + "   ,   " +  password.getText().toString());
            ServerHelper.getInstance().login(username.getText().toString(), password.getText().toString(), new ServerHelper.ResponseFunc<Profile>() {
                @Override
                public void onResponse(Profile response) {
                    if (response != null) {
                        txtView.setText(R.string.login_profile_loaded);
                        Intent intent = new Intent(FragmentViewer.this, ProfileView.class);
                        intent.putExtra("profile", response);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter_top, R.anim.leave_bottom);
                        ((StApp) getApplication()).commandService(ShimmerService.UPLOAD_DATA);
                        ((StApp) getApplication()).commandService(ShimmerService.DOWNLOAD_DATA);
                        finish();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error != null && error.getMessage() != null && error.getMessage().equals("wrong")) {
                        txtView.setText(R.string.login_error);
                        password.setText(null);
                        v.setEnabled(true);
                    } else {
                        if(error == null) {
                            Log.e("loginBtn", "Something went wrong, we don't know exactly what.");
                            loginBtn(v);
                        }
                    }
                }
            });
        }else{
            Toast.makeText(getApplicationContext(), "Unable to login, no internet connection", Toast.LENGTH_SHORT).show();
        }

    }


    public void registerBtn(View v) {
        if(ServerHelper.getInstance().checkInternetConnection()) {
        EditText firstName = (EditText) findViewById(R.id.new_firstname);
        EditText lastName = (EditText) findViewById(R.id.new_lastname);
        EditText username = (EditText) findViewById(R.id.new_username);
        EditText email = (EditText) findViewById(R.id.new_email);
        EditText password = (EditText) findViewById(R.id.new_password);
            ServerHelper.getInstance().createProfile(firstName.getText().toString(), lastName.getText().toString(), username.getText().toString(), email.getText().toString(), avatar, password.getText().toString(), new ServerHelper.ResponseFunc<Profile>() {
                @Override
                public void onResponse(Profile response) {
                    Intent intent = new Intent(FragmentViewer.this, ProfileView.class);
                    intent.putExtra("profile", response);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter_top, R.anim.leave_bottom);
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("registerBtn", error.getMessage());
                }
            });
        }else{
            Toast.makeText(getApplicationContext(), "Unable to register, no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void toLogin(View v) {
        mPager.setCurrentItem(0, true);
    }

    public void toNewProfile(View v) {
        mPager.setCurrentItem(2, true);
    }

    public void toStart(View v) {
        mPager.setCurrentItem(1, true);
    }

    @Override
    public void onFragmentInteraction(String message) {
        Log.i("onFragmentInteraction", message);
        avatar = message;
        setAvatarImage(message);
    }

    public void setAvatarImage(String avatar) {
        int avatarID = getResources().getIdentifier("avatar_" + avatar + "_512", "drawable", getPackageName());
        ImageView avatarView = (ImageView) findViewById(R.id.new_avatar);
        avatarView.setImageResource(avatarID);
    }

    private class FragmentAdapter extends FragmentPagerAdapter {
        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public Fragment getItem(int position) {
            return FragmentProvider.init(position);
        }
    }
}
