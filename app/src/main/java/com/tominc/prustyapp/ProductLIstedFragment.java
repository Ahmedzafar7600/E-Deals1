package com.tominc.prustyapp;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProductLIstedFragment extends Fragment {
    User user;
    RecyclerView recyclerView;
    Button delete;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    ProductRecyclerViewAdapter adapter;
    ArrayList<Product> items = new ArrayList<>();
    private final String PRODUCT_LISTED_URL = Config.BASE_URL + "product_by_user.php";
    View pb;
    View noDataFound;

    private final String TAG = "ProductListFragment";

    DatabaseReference mRef;


    public ProductLIstedFragment() {
        // Required empty public constructor
    }

    public static ProductLIstedFragment newInstance(User user) {

        Bundle args = new Bundle();
        args.putSerializable("user", user);

        ProductLIstedFragment fragment = new ProductLIstedFragment();
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_product_listed, container, false);

        Bundle args = getArguments();
        user = (User) args.getSerializable("user");

        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference("users").child(mUser.getUid());

        pb = root.findViewById(R.id.loading_data);
        noDataFound = root.findViewById(R.id.no_data_found);

        recyclerView = (RecyclerView) root.findViewById(R.id.product_liked_recycler);
        recyclerView.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        adapter = new ProductRecyclerViewAdapter(getActivity(), items);
        recyclerView.setAdapter(adapter);






        getListedProducts();


        return root;



    }




    private void getListedProducts(){
        pb.setVisibility(View.VISIBLE);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userData = dataSnapshot.getValue(User.class);
                Log.d(TAG, "onDataChange: User: " + userData.toString());
                HashMap<String, String> prodList = userData.getProductAdded();
//           ArrayList<String> prodList = userData.getProductAdded();
                if(prodList != null){
                    fetchProductsFromList(prodList);
                } else{
                    noDataFound.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.toString());
                pb.setVisibility(View.GONE);
            }

        });


    }




    private void fetchProductsFromList(HashMap<String, String> productsIds){
        DatabaseReference mProdRef;

        for(Map.Entry<String, String> product: productsIds.entrySet()){
            String prodId = product.getValue();
            mProdRef = FirebaseDatabase.getInstance().getReference("products").child(prodId);
            mProdRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Product prod = dataSnapshot.getValue(Product.class);
                    Log.d(TAG, "onDataChange: Product found " + prod.getProductId());
                    items.add(prod);
                    adapter.notifyDataSetChanged();
                    pb.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    pb.setVisibility(View.GONE);
                    Log.d(TAG, "onCancelled: " + databaseError.toString());
                }
            });
        }
    }

    private void fetchProductsFromList(ArrayList<String> productsIds){
        DatabaseReference mProdRef;

        for(String prodId: productsIds){
            mProdRef = FirebaseDatabase.getInstance().getReference("products").child(prodId);







            mProdRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Product prod = dataSnapshot.getValue(Product.class);
                    Log.d(TAG, "onDataChange: Product found " + prod.getProductId());
                    items.add(prod);
                    adapter.notifyDataSetChanged();
                    pb.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    pb.setVisibility(View.GONE);
                    Log.d(TAG, "onCancelled: " + databaseError.toString());
                }
            });
        }
    }

    //TODO: create no data found layout XML and add it whwn required.

    // TODO: create logging in layout

}
