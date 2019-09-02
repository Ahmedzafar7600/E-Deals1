package com.tominc.prustyapp;

import android.*;
import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.tominc.prustyapp.adapters.PlacesAutoCompleteAdapter;
import com.tominc.prustyapp.models.PlaceModel;
import com.tominc.prustyapp.services.LocationService;
import com.tominc.prustyapp.utilities.DownloadFirebaseImage;
import com.tominc.prustyapp.utilities.DownloadMethods;
import com.tominc.prustyapp.views.DelayAutoCompleteTextView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.mateware.snacky.Snacky;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fragmentManager;
    SharedPreferences mPrefs;
    User user;
    Fragment1 fragment1;
    private final int REQUEST_ADD_PRODUCT = 122;
    private final String TAG = "MainActivity";

    //Button btnscrap;

    TextView nav_header_name, nav_header_email;
    ImageView nav_header_image;
    RelativeLayout nav_header_loading_image;

    DelayAutoCompleteTextView autocomplateCity;

    StorageReference mStorage;

    private FirebaseAuth mAuth;
    private boolean isAtHome=true;

    private LocationService locService;

    private static final int PERMISSION_LOCATION_REQUSET = 546;

    TextView mainToolbarTitle;
    LinearLayout mainToolbarBlock;

    //TODO: Added Change location feature - DONE
    //TODO: filter product based on location - DONE
    // TODO: When selecting state option to set Current location


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragment1=(Fragment1) getSupportFragmentManager().findFragmentById(R.id.fragment_one_layout);

      //  btnscrap = (Button)findViewById(R.id.scrapbtn);
       // btnscrap.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { Intent myIntent = new Intent(MainActivity.this, Main2Activity.class); MainActivity.this.startActivity(myIntent); } });


       // usetTitle(R.string.products);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        mStorage = FirebaseStorage
                .getInstance()
                .getReference("profiles")
                .child("images/" + mUser.getUid() + "/profile.jpg");


        mPrefs = getSharedPreferences("app", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("user", "");
        user = gson.fromJson(json, User.class);


        fragment1 = Fragment1.newInstance(user);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_content, fragment1).commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                Intent in = new Intent(MainActivity.this, AddProductActivity.class);
                startActivity(in);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);

        nav_header_name= (TextView) view.findViewById(R.id.nav_header_name);
        nav_header_email = (TextView) view.findViewById(R.id.nav_header_email);
        nav_header_image = (ImageView) view.findViewById(R.id.nav_header_profile_pic);
        nav_header_loading_image = (RelativeLayout) view.findViewById(R.id.loading);

        mainToolbarTitle = (TextView) findViewById(R.id.main_toolbar_title);
        mainToolbarBlock = (LinearLayout) findViewById(R.id.main_toolbar_block);
        autocomplateCity = (DelayAutoCompleteTextView) findViewById(R.id.main_toolbar_city_input);

        hideCityInput();

        autocomplateCity.setAdapter(new PlacesAutoCompleteAdapter(MainActivity.this));

        autocomplateCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaceModel placeSelected = (PlaceModel) parent.getItemAtPosition(position);


                String location="";
                SharedPreferences.Editor edit = mPrefs.edit();
                if(placeSelected.getCity() != null){
                    Log.e("city",placeSelected.getCity());
                    edit.putString("userCity", placeSelected.getCity());
                    location += placeSelected.getCity();
                    mainToolbarTitle.setText(placeSelected.getCity());
                    autocomplateCity.setText(placeSelected.getCity());
                }
                if(placeSelected.getState() != null){
                    edit.putString("userState", placeSelected.getState());
                    location += placeSelected.getState();
                    mainToolbarTitle.setText(placeSelected.getCity() + ", " + placeSelected.getState());
                    autocomplateCity.setText(placeSelected.getCity() + ", " + placeSelected.getState());

                }
                if(placeSelected.getCountry() != null){
                    edit.putString("userCountry", placeSelected.getCountry());
                    location += placeSelected.getCountry();
                }
                if(Geocoder.isPresent()){
                    try{
                        Geocoder geoCoder = new Geocoder(MainActivity.this);
                        List<Address> addresses = geoCoder.getFromLocationName(location, 1);
                        edit.putString("lat", Double.toString(addresses.get(0).getLatitude()));
                        edit.putString("long", Double.toString(addresses.get(0).getLongitude()));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                edit.apply();

                hideCityInput();
                fragment1.getAllProducts();
            }
        });

        showNavLoadingImage();

//        downloadProfilePic();
        downloadProfilePicViaURL();

        if(user != null){
            nav_header_name.setText(user.getName());
            nav_header_email.setText(user.getEmail());
        }

        FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mUser = firebaseAuth.getCurrentUser();
                if(mUser == null){
                    Intent in = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(in);
                    finish();
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSION_LOCATION_REQUSET);
        } else{
            getLocation();

        }


        mainToolbarBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toasty.success(MainActivity.this, "Change Location", Toast.LENGTH_SHORT).show();
                showCityInput();
            }
        });

//        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideCityInput();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_LOCATION_REQUSET:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation();
                } else{
                    Snacky.builder()
                            .setActivty(MainActivity.this)
                            .setDuration(Snacky.LENGTH_SHORT)
                            .setText("Location Permission required")
                            .warning().show();
                }
        }
    }

    private void getLocation(){
        locService = new LocationService(MainActivity.this);

        Location loc = locService.getLocation();

        if(loc != null){
            Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geoCoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                String userCity = addresses.get(0).getLocality();
                String userState = addresses.get(0).getAddressLine(1);
                String userCountry = addresses.get(0).getCountryName();

                mainToolbarTitle.setText(userCity + ", " + userCountry);
                Log.d(TAG, "getLocation: Location availalbe " + userCity + ", " + userState + ", " + userCountry);

                SharedPreferences.Editor edit = mPrefs.edit();
                edit.putString("userCity", userCity);
                edit.putString("userState", userState);
                edit.putString("userCountry", userCountry);
                edit.putString("long", Double.toString(loc.getLongitude()));
                edit.putString("lat", Double.toString(loc.getLatitude()));

                edit.apply();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "getLocation: Error getting address " +  e.toString());
            }
        } else{
            Log.e(TAG, "getLocation: Location not available");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locService.stopUsingGPS();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(isAtHome){
                if(autocomplateCity.getVisibility() == View.VISIBLE){
                    hideCityInput();
                } else{
                    super.onBackPressed();
                }
            }
            else {
                fragmentManager.beginTransaction().replace(R.id.frame_content,Fragment1.newInstance(user)).commit();
                isAtHome=true;
//                setTitle(R.string.products);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout:
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);

                return true;

                default:
                    return super.onOptionsItemSelected(item);
        }
    }
        // int id = item.getItemId();

        //noinspection SimplifiableIfStatement




          /*  SharedPreferences.Editor edit = mPrefs.edit();
            edit.clear();
            edit.apply();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ((ActivityManager)getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
            }

            Intent in = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(in);

            mAuth.signOut();

            return true;
        }*/
     //   return true;
       // }

        //return super.onOptionsItemSelected(item);
    //}


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            isAtHome=true;
            fragmentManager.beginTransaction().replace(R.id.frame_content, Fragment1.newInstance(user)).commit();
            setTitle(item.getTitle());
        }  else if (id == R.id.nav_slideshow) {
            isAtHome=false;
            fragmentManager.beginTransaction().replace(R.id.frame_content, ProfileFragment.newInstance(user)).commit();
            setTitle(item.getTitle());
        }



        else if (id == R.id.scrapbtn) {
            isAtHome=false;
            Intent myIntent = new Intent(MainActivity.this, Main2Activity.class); MainActivity.this.startActivity(myIntent);
            setTitle(item.getTitle());
        }





        else if (id == R.id.nav_manage) {
            isAtHome=false;
            fragmentManager.beginTransaction().replace(R.id.frame_content, ProductLikedFragment.newInstance(user)).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_listed) {
            isAtHome=false;
            fragmentManager.beginTransaction().replace(R.id.frame_content, ProductLIstedFragment.newInstance(user)).commit();
            setTitle(item.getTitle());
        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Download this amazing app from play store: E-Deals");
            sendIntent.setType("text/plain");

            if (sendIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(sendIntent);
            }
        } else if (id == R.id.nav_send){
            Intent intent = new Intent(MainActivity.this,Aboutus.class);
            startActivity(intent);
           // sendFeedback();
        }

//        setTitle(item.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendFeedback(){
        Intent in = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "hamzashiekh1234562gmail.com@gmail.com"));
        in.setType("message/rfc822");
        in.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + " app feedback");
        in.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.feedback_text));

        startActivity(Intent.createChooser(in, "Choose Mail Client"));
    }

    private void downloadProfilePicViaURL(){
        DownloadFirebaseImage downloadImage = new DownloadFirebaseImage(MainActivity.this);
        downloadImage.download(mStorage, nav_header_image, "Profile Pic could not be downloaded", new DownloadMethods() {
            @Override
            public void successMethod() {
                hideNavLoadingImage();
            }

            @Override
            public void failMethod() {
                hideNavLoadingImage();
            }
        });
    }

    private void showNavLoadingImage(){
        nav_header_loading_image.setVisibility(View.VISIBLE);
    }

    private void hideNavLoadingImage(){
        nav_header_loading_image.setVisibility(View.GONE);
    }


    private void hideCityInput(){
        autocomplateCity.setVisibility(View.GONE);
        mainToolbarBlock.setVisibility(View.VISIBLE);

        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autocomplateCity.getWindowToken(), 0);

//        fragment1.getAllProducts();
    }

    private void showCityInput(){
        autocomplateCity.setVisibility(View.VISIBLE);
        mainToolbarBlock.setVisibility(View.GONE);
        autocomplateCity.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(autocomplateCity, InputMethodManager.SHOW_IMPLICIT);
    }

}
