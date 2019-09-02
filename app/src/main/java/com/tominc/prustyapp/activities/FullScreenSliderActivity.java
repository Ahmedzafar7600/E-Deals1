package com.tominc.prustyapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.tominc.prustyapp.R;
import com.tominc.prustyapp.models.FullScreenSliderView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FullScreenSliderActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener {

    private SliderLayout mSlider;
    private ArrayList<String> imagePaths;
    private String prodId;
    SharedPreferences mPrefs;
    int imageCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideWindow();

        setContentView(R.layout.activity_full_screen_slider);

        mSlider = (SliderLayout) findViewById(R.id.slider);

        Intent in = getIntent();
        prodId = in.getStringExtra("prodId");

        mPrefs = getSharedPreferences(prodId, MODE_PRIVATE);

        imageCount = mPrefs.getInt("imageCount", 0);

        for(int i=0;i<imageCount;i++){
            String filePath = mPrefs.getString("image"+i, null);
            if(filePath!=null){
                addImage(filePath, i);
            }
        }

        mSlider.setPresetTransformer(SliderLayout.Transformer.Stack);
//        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);
        mSlider.stopAutoCycle();

        mSlider.setPresetTransformer("Stack");

    }

    private void addImage(String filePath, int count){
        File file = new File(filePath);

        FullScreenSliderView sliderView = new FullScreenSliderView(FullScreenSliderActivity.this);
        sliderView.description((count+1) + "/" + (imageCount) + "")
                .image(file)
                .setScaleType(BaseSliderView.ScaleType.FitCenterCrop)
                .setOnSliderClickListener(this);

        sliderView.bundle(new Bundle());
        sliderView.getBundle().putString("extra", "image" + count);
        mSlider.addSlider(sliderView);
    }

    @Override
    protected void onStop() {
        mSlider.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        //TODO: show/hide toolbar
        Toast.makeText(FullScreenSliderActivity.this, "CLicked", Toast.LENGTH_SHORT).show();
    }

    private void showWindow(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void hideWindow(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
