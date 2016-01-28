package com.tomandfelix.stapp2.activity;

/**
 * Created by Tom on 18/12/2014.
 * Every activity after the login screen will extend this activity, it takes care of the connection with the service
 */

import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.application.StApp;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

public abstract class ServiceActivity extends ActionBarActivity {
    protected Toolbar toolbar;
    protected StApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.app = (StApp) getApplication();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }
}
