package com.tominc.prustyapp.models;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.github.chrisbanes.photoview.PhotoView;
import com.tominc.prustyapp.R;
import com.tominc.prustyapp.views.ZoomImageView;

/**
 * Created by shubham on 25/03/17.
 */

public class FullScreenSliderView extends BaseSliderView {
    Context c;
    String filepath;
    int position;
    boolean isWindowShowing;

    public FullScreenSliderView(Context context) {
        super(context);
        isWindowShowing=false;
        this.c = context;
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.full_screen_image_item,null);
        PhotoView target = (PhotoView) v.findViewById(R.id.image);
        TextView description = (TextView)v.findViewById(R.id.description);

        description.setText(getDescription());
        bindEventAndShow(v, target);

        target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isWindowShowing){
                    hideWindow();
                } else{
                    showWindow();
                }
                isWindowShowing = !isWindowShowing;
            }
        });

        return v;
    }

    private void showWindow(){
        ((Activity) c).getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void hideWindow(){
        ((Activity) c).getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
