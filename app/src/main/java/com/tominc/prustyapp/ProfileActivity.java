package com.tominc.prustyapp;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;

public class ProfileActivity extends AppCompatActivity {
    Toolbar toolbar;
    Button liked, listed;
    CircularImageView profile_image;
    TextView name, email, phone, year, college;
    SharedPreferences mPrefs;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mPrefs = getSharedPreferences("app", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("user", "");
        user = gson.fromJson(json, User.class);

        toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);

        name.setText(user.getName());
        email.setText(user.getEmail());
        phone.setText(user.getPhone());
        year.setText(user.getYear());
        college.setText(user.getCollege());

        liked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        listed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        

    }
}
