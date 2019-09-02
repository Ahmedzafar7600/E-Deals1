package com.tominc.prustyapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.tominc.prustyapp.utilities.DownloadFirebaseImage;
import com.tominc.prustyapp.utilities.DownloadMethods;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.mateware.snacky.Snacky;
import es.dmoral.toasty.Toasty;

public class ShowProductActivity extends AppCompatActivity {
    RelativeLayout sliderLayout;
    Toolbar toolbar;

    private final String TAG = "ShowProductActivity";

    TextView productName, productPrice, productDescription, productWanted, productLocation,
            profileName, profileEmail, profilePhone, profileCollege;
    CircularImageView profile_pic;

    ImageView share_call, share_message, share_email;

    LinearLayout profileCardFront, profileCardBack;
    CardView cardRootLayout;
    ImageView like_button, share_button;
    ScrollView allItems;

    private final String UPDATE_WANTED_URL = Config.BASE_URL +  "update_wanted.php";
    private final String IMAGE_DOWNLOAD_BASE = Config.BASE_URL + "uploads/";
    private final int IMAGE_HEIGHT = 200;

    private SliderLayout mSlider;
    SharedPreferences mPrefs;
    SharedPreferences mPrefs_user;
    private boolean iWantIt=false;

    ArrayList<String> imagePaths;

    private final int PERMISSION_REQUSET = 13;

    User user;
    Product prod;
    private ArrayList<Bitmap> imagesBitmap = new ArrayList<>();

    DatabaseReference mRef;
    StorageReference mStorage;
    FirebaseUser mUser;

    SpinKitView profile_pb, images_pb;
    View profile_pb_background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_product_adapter);

        toolbar = (Toolbar) findViewById(R.id.show_product_toolbar);

        profile_pb = (SpinKitView) findViewById(R.id.loading);
        profile_pb_background = findViewById(R.id.loading_background);
        images_pb = (SpinKitView) findViewById(R.id.loading_images);
        productName = (TextView) findViewById(R.id.show_product_name);
        productPrice = (TextView) findViewById(R.id.show_product_price);
        productLocation = (TextView) findViewById(R.id.show_product_location);
        productDescription = (TextView) findViewById(R.id.show_product_description);
        profileCardFront = (LinearLayout) findViewById(R.id.profile_front);
        profileCardBack = (LinearLayout) findViewById(R.id.profile_back);
        cardRootLayout = (CardView) findViewById(R.id.profile_cards_root);
        profile_pic = (CircularImageView) findViewById(R.id.show_profile_image);
        profileName = (TextView) findViewById(R.id.show_profile_name);
        profilePhone = (TextView) findViewById(R.id.show_profile_phone);
        profileEmail = (TextView) findViewById(R.id.show_profile_email);
        profileCollege = (TextView) findViewById(R.id.show_profile_college);
        like_button = (ImageView) findViewById(R.id.slider_item_like);
        share_button = (ImageView) findViewById(R.id.slider_item_share);
        share_call = (ImageView) findViewById(R.id.product_call);
        share_message = (ImageView) findViewById(R.id.product_message);
        share_email = (ImageView) findViewById(R.id.product_email);
        allItems = (ScrollView) findViewById(R.id.show_product_scroll);

        showProfileLoading();
        showLoadingImages();

        imagePaths = new ArrayList<>();

        profileCardFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard(profileCardFront, profileCardBack, cardRootLayout);
            }
        });

        profileCardBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard(profileCardFront, profileCardBack, cardRootLayout);
            }
        });

        sliderLayout = (RelativeLayout) findViewById(R.id.slider_relative);
        mSlider = (SliderLayout) findViewById(R.id.show_image_slider);

        mSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomIndicator((PagerIndicator) findViewById(R.id.show_image_indicator));
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(12000);
        mSlider.stopAutoCycle();


        Intent in = getIntent();
        prod = (Product) in.getSerializableExtra("prod");
        mPrefs = getSharedPreferences(prod.getProductId(), MODE_PRIVATE);

        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putInt("imageCount", prod.getImageCount());
        edit.apply();

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mPrefs_user = getSharedPreferences("app", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs_user.getString("user", "");
        user = gson.fromJson(json, User.class);

//        loadProfilePic();
        downloadProfilePicViaURL();

        String liked = mPrefs.getString(prod.getName(), null);
        if(liked!=null){
            iWantIt = true;
            like_button.setImageResource(R.drawable.ic_bookmark_black_24dp);
        }

        toolbar.setTitle(prod.getName().toUpperCase());
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productName.setText(prod.getName());
        productPrice.setText("Price: Rs. " + prod.getPrice());
        productDescription.setText(prod.getDescription());
        if(prod.getCity() != null){
            productLocation.setText(prod.getCity() + ", " + prod.getState() + ", " + prod.getCountry());
        } else{
            productLocation.setText("Location not available");
        }
//        productWanted.setText("Wanted: " + prod.getWanted());

        DatabaseReference mProdUser = FirebaseDatabase.getInstance().getReference("users").child(prod.getUserId());

        mProdUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User prodUser = dataSnapshot.getValue(User.class);
                profileName.setText(prodUser.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: unable to load user");
                Toasty.error(ShowProductActivity.this, "Unable to load user data", Toast.LENGTH_SHORT).show();
            }
        });

//        profileName.setText(prod.getName());
        profileEmail.setText(prod.getEmail());
        profilePhone.setText(prod.getPhone());
        profileCollege.setText("type " + prod.getYear() + ", " + prod.getCollege());

//        new DownloadImages(prod.getImageCount(), prod.getEmail(), prod.getName()).execute();

        if(prod.getImageCount() == 0){
            hideLoadingImages();
            ((ImageView) findViewById(R.id.no_images)).setVisibility(View.VISIBLE);
        } else{
            downloadImagePermission();
        }

        like_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                updateWanted();
              //  like_button.setEnabled(true);
                if(!iWantIt){
                    bookmarkProduct();


                } else{
                    unbookmarkProduct();

                }
            }
        });

        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Handle share
            }
        });

        share_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+"+92"+prod.getPhone()));
                startActivity(in);
            }
        });

        share_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 smsMessage();
                sendMessage();
            }
        });

        share_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mailMessage();

            }
        });
    }

    private void downloadImagePermission(){
        int permissionCheck = ContextCompat.checkSelfPermission(ShowProductActivity.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.d(TAG, "downloadImagePermission: Requesting permisstion");
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ShowProductActivity.this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUSET);
        } else{
            Log.d(TAG, "onRequestPermissionsResult: permission granted");
            downloadAllImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUSET:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
//                    new DownloadImages(prod.getImageCount(), prod.getEmail(), prod.getName()).execute();
                    downloadAllImages();
                } else{
//                    Toast.makeText(getApplicationContext(), "Permisston required to upload pic", Toast.LENGTH_SHORT)
//                            .show();
                    Snacky.builder().setView(allItems)
                            .setActivty(ShowProductActivity.this)
                            .setText(R.string.permission_warning)
                            .setDuration(Snacky.LENGTH_SHORT)
                            .warning();
                }
                return;
        }
    }


    private void flipCard(View front, View back, View root) {
        FlipAnimation flipanim = new FlipAnimation(front, back);
        if(front.getVisibility() == View.GONE){
            flipanim.reverse();
        }
        root.startAnimation(flipanim);

    }

    @Override
    protected void onStop() {
        mSlider.stopAutoCycle();
        super.onStop();
    }

    public class FlipAnimation extends Animation {
        private Camera camera;
        private View fromview, toview;
        private float centerX, centerY;

        private boolean forward = true;

        public FlipAnimation(View fromview, View toview){
            this.fromview=fromview;
            this.toview=toview;

            setDuration(700);
            setFillAfter(false);
            setInterpolator(new AccelerateDecelerateInterpolator());
        }

        public void reverse(){
            forward = false;
            View temp = fromview;
            fromview=toview;
            toview=temp;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            centerX=width/2;
            centerY=height/2;
            camera = new Camera();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final double radians = Math.PI * interpolatedTime;
            float degress = (float) (180.0*radians/Math.PI);

            if(interpolatedTime >= 0.5f){
                degress-=180.f;
                fromview.setVisibility(View.GONE);
                toview.setVisibility(View.VISIBLE);
            }
            if(forward){
                degress=-degress;
            }

            final Matrix matrix = t.getMatrix();
            camera.save();
            camera.rotateY(degress);
            camera.getMatrix(matrix);
            camera.restore();
            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);

        }
    }

    private int[] getImageDimensions(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        //                int height = size.y;

        Resources r = getResources();
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, IMAGE_HEIGHT, r.getDisplayMetrics());

        int[] dimen = new int[2];
        dimen[0] = width;
        dimen[1] = height;
        return dimen;
    }

    private void addImagesToSlider(){
        for(int i=0;i<imagesBitmap.size();i++){
            SliderView imageSliderView = new SliderView(ShowProductActivity.this, i, prod.getProductId());
            imageSliderView.setImage(imagesBitmap.get(i));
            imageSliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
            mSlider.addSlider(imageSliderView);
        }
    }

    private void downloadAllImages(){
        File tempFile;
        for(int i=0;i<prod.getImageCount();i++){
            try {
                tempFile = File.createTempFile("images", "jpg");
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            mStorage = FirebaseStorage.getInstance().getReference("ProductImages")
                    .child(prod.getProductId())
                    .child("images/" + i + ".jpg");

            int[] dimen = getImageDimensions();
            final int width = dimen[0];
            final int height = dimen[1];

            final File tempFile2 = tempFile;
            final int finalI = i;

//            DownloadFirebaseImage imageDownloader = new DownloadFirebaseImage(ShowProductActivity.this, "ShowProductActivity_Donwload Slider Images");
//            final ImageView imageView = new ImageView(ShowProductActivity.this);
//            imageDownloader.download(mStorage, imageView, "Failed to download image", new DownloadMethods() {
//                @Override
//                public void successMethod() {
//                    Log.d(TAG, "successMethod: Image downloaded, now adding to sliderView");
//                    SliderView sliderView = new SliderView(ShowProductActivity.this, finalI, prod.getProductId());
//                    sliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
//                    imageView.buildDrawingCache();
//                    sliderView.setImage(imageView.getDrawingCache());
////                    sliderView.setImage(BitmapFactory.decodeFile(tempFile2.toString()));
//
//                    mSlider.addSlider(sliderView);
//                    Log.d(TAG, "onSuccess: got image ");
//
//                    imagePaths.add(tempFile2.getAbsolutePath());
//                    SharedPreferences.Editor edit = mPrefs.edit();
//                    edit.putString("image"+finalI, tempFile2.toString());
//                    edit.apply();
//
//                    hideLoadingImages();
//                }
//
//                @Override
//                public void failMethod() {
//                    hideLoadingImages();
//                }
//            });

            mStorage.getFile(tempFile2)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            if(!ShowProductActivity.this.isDestroyed()){
                                hideLoadingImages();
                                ImageView imageView = new ImageView(ShowProductActivity.this);
                                Glide.with(ShowProductActivity.this)
                                        .load(tempFile2)
                                        .into(imageView);

                                SliderView sliderView = new SliderView(ShowProductActivity.this, finalI, prod.getProductId());
                                sliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
                                sliderView.setImage(BitmapFactory.decodeFile(tempFile2.toString()));

                                mSlider.addSlider(sliderView);
                                Log.d(TAG, "onSuccess: got image ");

                                imagePaths.add(tempFile2.getAbsolutePath());
                                SharedPreferences.Editor edit = mPrefs.edit();
                                edit.putString("image"+finalI, tempFile2.toString());
                                edit.apply();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: Image could not downloaded" + e.toString() );
                        }
                    });
        }
    }

    private void bookmarkProduct(){
        final ProgressDialog dialog = new ProgressDialog(ShowProductActivity.this);
        dialog.setMessage("Updating...");
        dialog.setCancelable(false);
        dialog.show();

        DatabaseReference mRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        String productID = prod.getProductId();
        mRef.child("productliked").push().setValue(productID);

        iWantIt = true;
        like_button.setImageResource(R.drawable.ic_bookmark_black_24dp);
//                    like_button.setImageResource(android.R.drawable.star_big_on);

        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putString(prod.getName(), "y");
        edit.apply();

        dialog.dismiss();
    }

    private void unbookmarkProduct(){
        final ProgressDialog dialog = new ProgressDialog(ShowProductActivity.this);
        dialog.setMessage("Updating...");
        dialog.setCancelable(false);
        dialog.show();

        DatabaseReference userRef = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userData = dataSnapshot.getValue(User.class);
                Log.d(TAG, "onDataChange: User: " + userData.toString());
                HashMap<String, String> prodList = userData.getProductliked();
                if(prodList != null){
                    for(Map.Entry<String, String> product: prodList.entrySet()) {
                        String prodId = product.getValue();
                        if(prodId.equals(prod.getProductId())){
                            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users").child(prod.getUserId());
                            mRef.child("productliked").child(product.getKey()).removeValue();
                            iWantIt=false;
                            like_button.setImageResource(R.drawable.ic_bookmark_border_black_24dp);

                            SharedPreferences.Editor edit = mPrefs.edit();
                            edit.putString(prod.getName(), null);
                            edit.apply();
                        }
                    }
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users").child(prod.getUserId());
        mRef.child("productliked").child(prod.getProductId()).removeValue();

        dialog.dismiss();
    }

    private void downloadProfilePicViaURL(){
        StorageReference mProfileRef = FirebaseStorage
                .getInstance()
                .getReference("profiles")
                .child("images/" + prod.getUserId() + "/profile.jpg");

        DownloadFirebaseImage downloadImage = new DownloadFirebaseImage(ShowProductActivity.this);
        downloadImage.download(mProfileRef, profile_pic, "Profile Pic could not be downloaded", new DownloadMethods() {
            @Override
            public void successMethod() {
                hideProfileLoading();
            }

            @Override
            public void failMethod() {
                hideProfileLoading();
            }
        });
    }


    private void mailMessage(){
        Intent in = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "hamzashiekh123456@gmail.com"));
        in.setType("message/rfc822");
        in.putExtra(Intent.EXTRA_SUBJECT, "Product " + prod.getName() + ": on app" + getResources().getString(R.string.app_name));
        in.putExtra(Intent.EXTRA_TEXT, "Hi\nI saw your product "
                + prod.getName()
                + " on app "
                + getResources().getString(R.string.app_name)
                + " and I want to buy it.\nKindly reply if you are still interested to sell the product." );

        startActivity(Intent.createChooser(in, "Choose Mail Client"));
    }

    // if whatsapp is installed then whatsapp else normal message
    private void sendMessage(){
        PackageManager pm = getPackageManager();
        try{
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            whatsappMessage();
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "sendMessage: Whatsapp not installed");
            e.printStackTrace();
            smsMessage();
        }

    }

    private void whatsappMessage(){
        try {
            Intent in = new Intent(Intent.ACTION_SEND, Uri.parse("smsto:"+prod.getPhone()));
            in.setType("text/plain");
            String text = "Regarding your product " + prod.getName() + " in the app, I would like to buy it. Please reply to negotiate.";
            PackageInfo info = getPackageManager().getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            in.setPackage("com.whatsapp");
            in.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(in, "with"));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e("ShowActivity", "whatsapp not installed");
        }

    }

    private void smsMessage(){
        Intent intentsms = new Intent( Intent.ACTION_VIEW, Uri.parse( "sms:" + "" ) );
        intentsms.putExtra( "sms_body", "Regarging your product " + prod.getName() + " in the app " + getResources().getString(R.string.app_name) + ", I would like to buy it, Please reply for furthur dealing" );
        intentsms.putExtra("address", "+92" + prod.getPhone());
        startActivity( intentsms );
    }

    private void showProfileLoading(){
        profile_pb.setVisibility(View.VISIBLE);
        profile_pb_background.setVisibility(View.VISIBLE);
    }

    private void hideProfileLoading(){
        profile_pb.setVisibility(View.GONE);
        profile_pb_background.setVisibility(View.GONE);
    }

    private void showLoadingImages(){
        images_pb.setVisibility(View.VISIBLE);
    }

    private void hideLoadingImages(){
        images_pb.setVisibility(View.GONE);
    }

}
