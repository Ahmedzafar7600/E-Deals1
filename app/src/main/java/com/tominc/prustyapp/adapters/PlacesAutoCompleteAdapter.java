package com.tominc.prustyapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.tominc.prustyapp.R;
import com.tominc.prustyapp.models.PlaceModel;
import com.tominc.prustyapp.services.PlaceAPI;

import java.util.ArrayList;

/**
 * Created by shubham on 01/05/17.
 */

public class PlacesAutoCompleteAdapter extends BaseAdapter implements Filterable {
    Context mContext;
    PlaceAPI mPlaceAPI = new PlaceAPI();
    ArrayList<PlaceModel> resultList;

    public PlacesAutoCompleteAdapter(Context c){
        this.mContext = c;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Object getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.auto_complete_list_item, parent, false);
        }
        TextView _city = (TextView) convertView.findViewById(R.id.autocomplete_city);
        TextView _state = (TextView) convertView.findViewById(R.id.autocomplete_state);

        String city = resultList.get(position).getCity();
        if(city != null){
            _city.setText(city);
        }
        String state = resultList.get(position).getState();
        String country = resultList.get(position).getCountry();

        if(state!=null){
            _state.setText(state);
        }
        if(country!=null){
            _state.setText(state + ", " + country);
        }

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null){
                    resultList = mPlaceAPI.autoComplete(constraint.toString());
                    filterResults.values =  resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0){
                    notifyDataSetChanged();
                } else{
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}
