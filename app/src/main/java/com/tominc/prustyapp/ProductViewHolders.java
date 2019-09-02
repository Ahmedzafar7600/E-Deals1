package com.tominc.prustyapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;

import net.bohush.geometricprogressview.GeometricProgressView;

import java.util.ArrayList;

/**
 * Created by shubham on 21/3/16.
 */
public class ProductViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView name;
    public TextView price;
    public ImageView image;
    public SpinKitView pb;
    ArrayList<Product> items;


    public ProductViewHolders(View itemView, ArrayList<Product> items) {
        super(itemView);
        this.items= items;
//        itemView.setOnClickListener(this);
        name = (TextView) itemView.findViewById(R.id.product_name);
        price = (TextView) itemView.findViewById(R.id.product_cost);
        image = (ImageView) itemView.findViewById(R.id.product_pic);
        pb = (SpinKitView) itemView.findViewById(R.id.progress_bar);

        CardView card = (CardView) itemView.findViewById(R.id.list_item_card);
        card.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        Log.d("ProductViewHolder", "Clicked " + getLayoutPosition() + " " + getAdapterPosition());

        Intent in = new Intent(view.getContext(), ShowProductActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        in.putExtra("prod", items.get(this.getLayoutPosition()));
        view.getContext().startActivity(in);

    }
}
