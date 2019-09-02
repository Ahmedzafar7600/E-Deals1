package com.tominc.prustyapp.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import de.mateware.snacky.Snacky;

/**
 * Created by shubham on 30/04/17.
 */

public class LocationService implements LocationListener {
    Location loc;
    double longitude, latitute;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 100;
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000 * 60 * 1; // 60sec
    private static final int PERMISSION_REQUSET_NETWORK = 12;
    private static final int PERMISSION_REQUSET_GPS = 13;

    public static final String TAG = "LocationService";

    private boolean canGetLocation = false;

    Context mContext;
    protected LocationManager locationManager;

    public LocationService(Context c) {
        this.mContext = c;
//        getLocation();
    }

    public Location getLocation() {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        boolean checkGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean checkNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!checkGPS && !checkNetwork) {
            Snacky.builder()
                    .setActivty((Activity) mContext)
                    .setDuration(Snacky.LENGTH_SHORT)
                    .setText("No Service Provider Available")
                    .error();
        } else {
            this.canGetLocation = true;

            if (checkNetwork) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BETWEEN_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATE, this
                    );

                    if(locationManager != null){
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    if(loc != null){
                        latitute = loc.getLatitude();
                        longitude = loc.getLongitude();
                    }
                    return loc;
                }
                Log.d(TAG, "getLocation: Got location" + loc.getLatitude() + " " + loc.getLongitude());

            }
            if(checkGPS){
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BETWEEN_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATE, this
                    );

                    if(locationManager != null){
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }

                    if(loc != null){
                        latitute = loc.getLatitude();
                        longitude = loc.getLongitude();
                    }
                    return loc;
                }

                Log.d(TAG, "getLocation: Got location" + loc.getLatitude() + " " + loc.getLongitude());
            }
        }
        return null;

    }

    public double getLongitude(){
        if(loc != null){
            longitude = loc.getLongitude();
        }
        return longitude;
    }

    public double getLatitute(){
        if(loc != null){
            latitute = loc.getLatitude();
        }
        return latitute;
    }

    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(this);
        }
    }

    public boolean isLocationAvailable(){
        return this.canGetLocation;
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
