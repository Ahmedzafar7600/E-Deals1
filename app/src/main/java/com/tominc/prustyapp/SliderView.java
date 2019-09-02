package com.tominc.prustyapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.tominc.prustyapp.activities.FullScreenSliderActivity;

/**
 * Created by shubham on 7/2/16.
 */
public class SliderView extends BaseSliderView {
    int image;
    Context c;
    Bitmap bitmap;
    private int position;
    int CHOOSE;
    private String prodId;

    protected SliderView(Context context, int position, String prodId) {
        super(context);
        this.c = context;
        this.position = position;
        this.prodId = prodId;
    }

    public void setImage(int image){
        this.image = image;
        CHOOSE=1;
    }

    public void setImage(Bitmap bitmap){
        CHOOSE=2;
        this.bitmap = bitmap;
    }


    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.slider_item, (ViewGroup)null);

        ImageView imageId = (ImageView) v.findViewById(R.id.slider_item_image);

        if(CHOOSE==1){
            imageId.setImageResource(image);
        } else if(CHOOSE==2){
            imageId.setImageBitmap(bitmap);
        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(c, FullScreenSliderActivity.class);
                in.putExtra("prodId", prodId);
                c.startActivity(in);

            }
        });

        return v;
    }

}
