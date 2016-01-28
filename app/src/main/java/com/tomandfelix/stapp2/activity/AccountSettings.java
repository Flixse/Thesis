package com.tomandfelix.stapp2.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tomandfelix.stapp2.R;
import com.tomandfelix.stapp2.persistency.DatabaseHelper;
import com.tomandfelix.stapp2.persistency.Profile;
import com.tomandfelix.stapp2.persistency.ServerHelper;

/**
 * Created by Tom on 24/03/2015.
 */
public class AccountSettings extends ServiceActivity {
    private Profile mProfile;
    private ImageView avatar;
    private GridView avatarGrid;
    private EditText username, firstname, lastname, email, password;
    private String newAvatar = null;
    private TypedArray avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_account_settings);
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        avatar = (ImageView) findViewById(R.id.edit_avatar);
        avatarGrid = (GridView) findViewById(R.id.edit_avatar_grid);
        username = (EditText) findViewById(R.id.edit_username);
        firstname = (EditText) findViewById(R.id.edit_firstname);
        lastname = (EditText) findViewById(R.id.edit_lastname);
        email = (EditText) findViewById(R.id.edit_email);
        password = (EditText) findViewById(R.id.edit_password);
        mProfile = DatabaseHelper.getInstance().getOwner();
        int avatarID = getResources().getIdentifier("avatar_" + mProfile.getAvatar() + "_512", "drawable", getPackageName());
        avatar.setImageResource(avatarID);
        username.setText(mProfile.getUsername());
        firstname.setText(mProfile.getFirstName());
        lastname.setText(mProfile.getLastName());
        email.setText(mProfile.getEmail());
        if(ServerHelper.getInstance().checkInternetConnection()) {
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (avatarGrid.getVisibility() == View.GONE) {
                        avatarGrid.setVisibility(View.VISIBLE);
                    } else {
                        avatarGrid.setVisibility(View.GONE);
                    }
                }
            });

            avatars = getResources().obtainTypedArray(R.array.avatars);
            AvatarGridAdapter avatarGridAdapter = new AvatarGridAdapter(this, R.layout.grid_item_avatar, avatars);
            avatarGrid.setAdapter(avatarGridAdapter);
            avatarGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    avatar.callOnClick();
                    newAvatar = getResources().getStringArray(R.array.avatar_names)[position];
                    int avatarID = getResources().getIdentifier("avatar_" + newAvatar + "_512", "drawable", getPackageName());
                    avatar.setImageResource(avatarID);
                }
            });
        }else{
            Toast.makeText(getApplicationContext(), "Unable to change account settings, no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        avatars.recycle();
    }

    public void onCancel(View v) {
        NavUtils.navigateUpFromSameTask(this);
    }

    public void onConfirm(View v) {
        final String newUsername = validateInput(username.getText().toString(), mProfile.getUsername());
        final String newFirstName =  validateInput(firstname.getText().toString(), mProfile.getFirstName());
        final String newLastName =  validateInput(lastname.getText().toString(), mProfile.getLastName());
        final String newEmail =  validateInput(email.getText().toString(), mProfile.getEmail());
        final String newPassword = validateInput(password.getText().toString(), "");
        newAvatar = validateInput(newAvatar, mProfile.getAvatar());

        if(newUsername != null) mProfile.setUsername(newUsername);
        if(newFirstName != null) mProfile.setFirstName(newFirstName);
        if(newLastName != null) mProfile.setLastName(newLastName);
        if(newEmail != null) mProfile.setEmail(newEmail);
        if(newAvatar != null) mProfile.setAvatar(newAvatar);

        if(newUsername != null || newFirstName != null || newLastName != null || newEmail != null || newPassword != null || newAvatar != null) {
            if(newPassword != null) {
                updateWithPassword(newFirstName, newLastName, newUsername, newEmail, newAvatar, newPassword);
            } else {
                updateWithToken(newFirstName, newLastName, newUsername, newEmail, newAvatar);
            }
        }
    }


    private void updateWithPassword(final String newFirstName, final String newLastName, final String newUsername, final String newEmail, final String newAvatar, final String newPassword) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Please enter your current password").setTitle("Password");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        alert.setView(input);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String oldPassword = input.getText().toString();
                        if(ServerHelper.getInstance().checkInternetConnection()) {
                            ServerHelper.getInstance().updateProfileSettings(newFirstName, newLastName, newUsername, newEmail, newAvatar, oldPassword, newPassword, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    if (volleyError.getMessage().equals("none")) {
                                        finish();
                                    } else if (volleyError.getMessage().equals("password")) {
                                        updateWithPassword(newFirstName, newLastName, newUsername, newEmail, newAvatar, newPassword);
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(getApplicationContext(), "Unable to change password, no internet connection", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        password.setText(null);
                        break;
                }
            }
        };
        alert.setPositiveButton("CONFIRM", listener);
        alert.setNegativeButton("CANCEL", listener);
        alert.show();
    }

    private void updateWithToken(final String newFirstName, final String newLastName, final String newUsername, final String newEmail, final String newAvatar) {
        if(ServerHelper.getInstance().checkInternetConnection()) {
            ServerHelper.getInstance().updateProfileSettings(newFirstName, newLastName, newUsername, newEmail, newAvatar, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (volleyError.getMessage().equals("none")) {
                        finish();
                    } else if (volleyError.getMessage().equals("token")) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(AccountSettings.this);
                        alert.setMessage("It seems like an error occured, Please enter your password again").setTitle("Password");
                        final EditText input = new EditText(AccountSettings.this);
                        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        alert.setView(input);
                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        String password = input.getText().toString();
                                        ServerHelper.getInstance().login(DatabaseHelper.getInstance().getOwner().getUsername(), password,
                                                new ServerHelper.ResponseFunc<Profile>() {
                                                    @Override
                                                    public void onResponse(Profile response) {
                                                        DatabaseHelper.getInstance().updateProfile(response);
                                                        onConfirm(null);
                                                    }
                                                }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError volleyError) {
                                                        if (volleyError.getMessage().equals("wrong")) {
                                                            updateWithToken(newFirstName, newLastName, newUsername, newEmail, newAvatar);
                                                        }
                                                    }
                                                });
                                        break;
                                }
                            }
                        };
                        alert.setPositiveButton("CONFIRM", listener);
                        alert.setNegativeButton("CANCEL", listener);
                        alert.show();
                    }
                }
            });
        }else{
            Toast.makeText(getApplicationContext(), "Unable to change account settings, no internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private String validateInput(String input, String old) {
        if(input == null || input.equals("") || input.equals(old)) {
            return null;
        } else {
            return input;
        }
    }
}
