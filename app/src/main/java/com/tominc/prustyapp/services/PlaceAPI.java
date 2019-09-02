package com.tominc.prustyapp.services;

import android.util.Log;

import com.tominc.prustyapp.models.PlaceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by shubham on 01/05/17.
 */

public class PlaceAPI {
    private static final String TAG = PlaceAPI.class.getSimpleName();

  //  private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    //private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
//    private static final String OUT_JSON = "/json";

  //  private static final String API_KEY = "AIzaSyDmJIDv7kpZ-YxVEPtFJB1lPjJoyH7nRME";



     private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyDmJIDv7kpZ-YxVEPtFJB1lPjJoyH7nRME";
  static final String KEY_description= "description";
    static final String KEY_place_id= "place_id";




    public ArrayList<PlaceModel> autoComplete(String input){
        ArrayList<PlaceModel> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&types=(cities)");
            sb.append("&components=country:pk");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Log.d(TAG, jsonResults.toString());

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");


            // Extract the PlaceModel descriptions from the results
            resultList = new ArrayList<PlaceModel>(predsJsonArray.length());

            for(int i=0;i<predsJsonArray.length();i++){
                JSONArray terms = predsJsonArray.getJSONObject(i).getJSONArray("terms");
                PlaceModel temp = new PlaceModel();
                for(int j=0;j<terms.length();j++){
                    switch(j){
                        case 0:
                            temp.setCity(terms.getJSONObject(0).getString("value"));
                            break;
                        case 1:
                            temp.setState(terms.getJSONObject(1).getString("value"));
                            break;
                        case 2:
                            temp.setCountry(terms.getJSONObject(2).getString("value"));
                            break;
                    }
                }
                resultList.add(temp);
            }

//            for (int i = 0; i < predsJsonArray.length(); i++) {
//                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
//            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

}
