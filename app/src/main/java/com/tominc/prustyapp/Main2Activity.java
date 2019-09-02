package com.tominc.prustyapp;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nineoldandroids.view.ViewHelper;
import com.tominc.prustyapp.views.ViewHolder;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener{

    private CardView foodcard, clothescard, electronicscard, fashioncard;

    RecyclerView mRecyclerView;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        foodcard = (CardView) findViewById(R.id.food_card);
        clothescard = (CardView) findViewById(R.id.clothes_card);
        electronicscard = (CardView) findViewById(R.id.electronices_card);
        fashioncard = (CardView) findViewById(R.id.fashion_card);

        foodcard.setOnClickListener((View.OnClickListener) this);
        clothescard.setOnClickListener((View.OnClickListener) this);
        electronicscard.setOnClickListener(this);
        fashioncard.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent i;

        switch (v.getId()){
            case R.id.food_card : i = new Intent(this,food.class);startActivity(i); break;
            case R.id.clothes_card : i = new Intent(this,clothes.class);startActivity(i); break;
            case R.id.electronices_card : i = new Intent(this,electronics.class);startActivity(i); break;
            case R.id.fashion_card : i = new Intent(this,fashion.class);startActivity(i); break;
            default:break;
        }

    }
}



