package com.tominc.prustyapp.utilities;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shubham on 03/04/17.
 */

public class CollegeService {
    private final String TAG = "CityService";

    public CollegeService(){
    }

    public List<String> getColleges(String queryString){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("colleges");
        final List<String> result = new ArrayList<>();
        Query query = mRef.startAt(queryString).limitToFirst(10);
        final int[] counter = {0};
        Log.d(TAG, "getCity: Getting city names");

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    Log.d(TAG, "onDataChange: Got result");
                    for(DataSnapshot city: dataSnapshot.getChildren()){
                        result.add((String) city.getValue());
                        Log.d(TAG, "onDataChange: Got Value: " + city.getValue());
                    }
                } else{
                    Log.d(TAG, "onDataChange: No Data Found");
                }
                counter[0]++;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        while(true){
            if(counter[0]>0){
                break;
            }
        }
        return result;
    }

}
