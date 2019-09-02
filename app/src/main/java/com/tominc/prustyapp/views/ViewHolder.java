package com.tominc.prustyapp.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.tominc.prustyapp.R;

import static com.squareup.picasso.Picasso.*;

public class ViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public ViewHolder(View itemView) {
        super(itemView);

    mView = itemView;

    }

    public void setDetails(Context ctx,String price, String description,String image,String location,String brand){

        TextView mtextview = (TextView) mView.findViewById(R.id.rTitleTv);
        TextView mdetailview = (TextView) mView.findViewById(R.id.rDescriptionTv);
        ImageView mimageview = (ImageView) mView.findViewById(R.id.rImageView);
        TextView mlocationview = (TextView) mView.findViewById(R.id.rlocationtv);
        TextView mbrandnameview = (TextView) mView.findViewById(R.id.rbrandtv);

        mtextview.setText(price);
        mdetailview.setText(description);
        Picasso.get().load(image).into(mimageview);
        mlocationview.setText(location);
        mbrandnameview.setText(brand);





    }



}
