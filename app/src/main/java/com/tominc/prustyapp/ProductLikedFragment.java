package com.tominc.prustyapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProductLikedFragment extends Fragment {
    User user;
    RecyclerView recyclerView;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    ProductRecyclerViewAdapter adapter;
    ArrayList<Product> items = new ArrayList<>();
    private final String PRODUCT_LIKED_URL = Config.BASE_URL + "product_liked_by_user.php";
    View pb;
    View noDataFound;

    private final String TAG = "ProductLikedFragment";

    DatabaseReference mRef;

    public ProductLikedFragment() {
        // Required empty public constructor
    }

    public static ProductLikedFragment newInstance(User user) {

        Bundle args = new Bundle();
        args.putSerializable("user", user);

        ProductLikedFragment fragment = new ProductLikedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_product_liked, container, false);


//        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
//        Log.d("userid",mUser.getUid().toString());
//        //mRef = FirebaseDatabase.getInstance().getReference("users").child(mUser.getUid());
//        mRef = FirebaseDatabase.getInstance().getReference("users").child(mUser.getUid());
//       // mRef = FirebaseDatabase.getInstance().getReference();

        Bundle args = getArguments();
        user = (User) args.getSerializable("user");


        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference("users").child(mUser.getUid());

        pb =  root.findViewById(R.id.loading_data);
        noDataFound = root.findViewById(R.id.no_data_found);

        recyclerView = (RecyclerView) root.findViewById(R.id.product_liked_recycler);
        recyclerView.setHasFixedSize(true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);


        getLikedProducts();
        adapter = new ProductRecyclerViewAdapter(getActivity(), items);
        recyclerView.setAdapter(adapter);

        return root;
    }

    private void getLikedProducts(){
        pb.setVisibility(View.VISIBLE);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userData = dataSnapshot.getValue(User.class);
                Log.d(TAG, "onDataChange: User: " + userData.toString());
                HashMap<String, String> prodList = userData.getProductliked();
//                ArrayList<String> prodList = userData.getProductliked();
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
            Log.d(TAG, "fetchProductsFromList: Product: " + prodId);
            mProdRef = FirebaseDatabase.getInstance().getReference("products").child(prodId);

            mProdRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Product prod = dataSnapshot.getValue(Product.class);
                    if(prod.getProductId()!=null){
                        Log.d(TAG, "onDataChange: Product found " + prod.getProductId());
                        items.add(prod);
                        adapter.notifyDataSetChanged();
                        pb.setVisibility(View.GONE);
                    }
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


}
