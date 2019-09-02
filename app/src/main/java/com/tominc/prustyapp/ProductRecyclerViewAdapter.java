package com.tominc.prustyapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tominc.prustyapp.utilities.DownloadFirebaseImage;
import com.tominc.prustyapp.utilities.DownloadMethods;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by shubham on 21/3/16.
 */
public class ProductRecyclerViewAdapter extends RecyclerView.Adapter<ProductViewHolders> {

    Context c;
    ArrayList<Product> items;
    private final String TAG = "AllProductsAdapter";

    private StorageReference mStorage;

    private final String IMAGE_DOWNLOAD_BASE = Config.BASE_URL + "uploads/";

    public ProductRecyclerViewAdapter(Context c, ArrayList<Product> items){
        this.c = c;
        this.items = items;
        mStorage = FirebaseStorage.getInstance().getReference("ProductImages");
    }

    @Override
    public ProductViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, null);
        ProductViewHolders rcv = new ProductViewHolders(layoutView, items);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final ProductViewHolders holder, int position)  {
        Log.d(TAG, "onBindViewHolder: " + position + " " + items.get(position).getName() + " " + items.get(position).getPrice());
        holder.name.setText(items.get(position).getName());
//        holder.price.setText(items.get(position).getPrice());
        holder.price.setText(Integer.toString(items.get(position).getPrice()));
        final File localFile;

        try{
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "onBindViewHolder: Cannot Create Temp file " + e.toString());
            return;
        }

        holder.pb.setVisibility(View.VISIBLE);

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorage = FirebaseStorage.getInstance().getReference("ProductImages")
                .child(items.get(position).getProductId())
                .child("images/0.jpg");

        DownloadFirebaseImage imageDownloader = new DownloadFirebaseImage(c);
        imageDownloader.download(mStorage, holder.image, "", new DownloadMethods() {
            @Override
            public void successMethod() {
                holder.pb.setVisibility(View.GONE);
            }

            @Override
            public void failMethod() {
                holder.pb.setVisibility(View.GONE);
            }
        });


//        mStorage.getFile(localFile)
//                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                        holder.pb.setVisibility(View.GONE);
//                        Glide.with(c)
//                                .load(localFile)
//                                .into(holder.image);
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e(TAG, "onFailure: File could not be downloaded or available " + e.toString());
//                        holder.pb.setVisibility(View.GONE);
//                        Glide.with(c)
//                                .load(R.drawable.ic_not_available)
//                                .into(holder.image);
//                    }
//                });

    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }
}
