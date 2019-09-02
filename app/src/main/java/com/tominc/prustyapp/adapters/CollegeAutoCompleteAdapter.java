package com.tominc.prustyapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.tominc.prustyapp.utilities.CollegeService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shubham on 02/04/17.
 */

public class CollegeAutoCompleteAdapter extends BaseAdapter implements Filterable {
    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<String> resultList = new ArrayList<String>();

    public CollegeAutoCompleteAdapter(Context c){
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
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView text = (TextView) convertView.findViewById(android.R.id.text1);
        text.setText((String) getItem(position));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null){
                    List<String> cities = findcities(mContext, constraint.toString());
                    filterResults.values = cities;
                    filterResults.count = cities.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0){
                    resultList = (List<String>) results.values;
                    notifyDataSetChanged();
                } else{
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    private List<String> findcities(Context c, String city){
        //TODO: Fetch citiess from Web
        List<String> arrayList = new ArrayList<>();
        arrayList.add("Sialkot");
        arrayList.add("Lahore");
        arrayList.add("Faisalbad");
        arrayList.add("Peshawer");
        arrayList.add("Islambad");
        arrayList.add("Karachi");
        arrayList.add("Queta");
        arrayList.add("Multan");
        arrayList.add("Gujrat");
        arrayList.add("Gujranwala");
        arrayList.add("Kamoke");
        arrayList.add("Sukhar");
        arrayList.add("Qasoor");
        arrayList.add("Bohawalpur");
        arrayList.add("Pak Pattan");
        arrayList.add("Hafiz Abad");
        arrayList.add("Muzafar Garh");
        arrayList.add("Wazirabad");
        arrayList.add("Rawalpindi");
        return arrayList;
    }
}
