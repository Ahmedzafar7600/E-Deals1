package com.tominc.prustyapp;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.soundcloud.android.crop.Crop;
import com.tominc.prustyapp.utilities.ImageCompression;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import de.mateware.snacky.Snacky;

public class AddProductActivity extends AppCompatActivity {
    RelativeLayout sliderLayout;
    Toolbar toolbar;
    private SliderLayout mSlider;

    EditText name, price, description, phone, email, year, college, location;
    ScrollView allItems;

    ArrayList<Bitmap> images_bitmap = new ArrayList<>();

    private int IMAGE_GALLERY=12;
    private int IMAGE_CAMERA=13;
    File photoFile = null;
    boolean fromCamera=false;
    SharedPreferences mPrefs;
    User user;

    private final String TAG = "AddProductActivity";

    private final int IMAGE_HEIGHT = 200;

    private final int PERMISSION_REQUSET = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        allItems = (ScrollView) findViewById(R.id.add_prod_items);
        toolbar = (Toolbar) findViewById(R.id.add_product_toolbar);
        toolbar.setTitle("Add Product");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPrefs = getSharedPreferences("app", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("user", "");
        user = gson.fromJson(json, User.class);

        name = (EditText) findViewById(R.id.add_product_name);
        price = (EditText) findViewById(R.id.add_product_price);
        phone = (EditText) findViewById(R.id.add_product_phone);
        email = (EditText) findViewById(R.id.add_product_email);
        year = (EditText) findViewById(R.id.add_product_year);
        college = (EditText) findViewById(R.id.add_product_college);
        location = (EditText) findViewById(R.id.add_product_location);
        description = (EditText) findViewById(R.id.add_product_description);
        sliderLayout = (RelativeLayout) findViewById(R.id.add_slider_relative);
        mSlider = (SliderLayout) findViewById(R.id.add_image_slider);

        phone.setText(user.getPhone());
        email.setText(user.getEmail());
        year.setText(user.getYear());
        college.setText(user.getCollege());
        location.setText(mPrefs.getString("userCity", "") + ", " + mPrefs.getString("userState", "") + ", " + mPrefs.getString("userCountry", ""));


        findViewById(R.id.add_product_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int permissionCheck = ContextCompat.checkSelfPermission(AddProductActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE);

                if(permissionCheck != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AddProductActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUSET);
                } else{
                    performImageSelection();
                }


            }
        });

        findViewById(R.id.add_product_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProduct(name.getText().toString(), price.getText().toString(), description.getText().toString(),
                        phone.getText().toString(), email.getText().toString(), year.getText().toString(), college.getText().toString());
            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomIndicator((PagerIndicator) findViewById(R.id.add_image_indicator));
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(1200000);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUSET:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    performImageSelection();
                } else{
//                    Toast.makeText(getApplicationContext(), "Permisston required to upload pic", Toast.LENGTH_SHORT)
//                            .show();

                    Snacky.builder().setView(allItems)
                            .setActivty(AddProductActivity.this)
                            .setText(R.string.permission_warning)
                            .setDuration(Snacky.LENGTH_SHORT)
                            .warning().show();
                }
        }
    }

    private void performImageSelection(){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddProductActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("Select");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddProductActivity.this,
                android.R.layout.simple_list_item_1);
        adapter.add("Take Photo");
        adapter.add("Select from Gallery");

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            ex.printStackTrace();
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, IMAGE_CAMERA);
                        }
                    }
                } else if (i == 1) {
                    Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(in, IMAGE_GALLERY);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_GALLERY && resultCode==RESULT_OK){
            fromCamera=false;
            beginCrop(data.getData());

        } else if(requestCode==IMAGE_CAMERA && resultCode==RESULT_OK){
            fromCamera=true;
            beginCrop(Uri.fromFile(photoFile));

        } else if(requestCode==Crop.REQUEST_CROP && resultCode==RESULT_OK){
            Uri temp = Crop.getOutput(data);

            new MySyncTask().execute(temp);

        }
    }

    private class MySyncTask extends AsyncTask<Uri, Bitmap, Bitmap>{
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(AddProductActivity.this);
            dialog.setMessage("Adding Image");
            dialog.setCancelable(false);
            dialog.show();

        }

        @Override
        protected Bitmap doInBackground(Uri... uris) {

            ImageCompression compress = new ImageCompression(AddProductActivity.this);
            Bitmap bmp = compress.compressImage(uris[0].toString());
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            images_bitmap.add(bitmap);
//                image.setImageBitmap(bmp);
            SliderView imageSliderView = new SliderView(AddProductActivity.this, 1, "prodid");
            imageSliderView.setImage(bitmap);
             // imageSliderView.setScaleType(BaseSliderView.ScaleType.CenterCrop);
            mSlider.addSlider(imageSliderView);
            dialog.dismiss();
        }
    }

    private String saveToInternalSorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        if(fromCamera) {
            FileOutputStream fos = null;
            try {

                fos = new FileOutputStream(photoFile);

                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return photoFile.toString();
        } else {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }

            FileOutputStream fos = null;
            try {

                fos = new FileOutputStream(photoFile);

                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return photoFile.toString();
        }
    }

    private void beginCrop(Uri source) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        Uri destination = Uri.fromFile(new File(getCacheDir(), imageFileName));

        int[] dimen = getImageDimensions();
        int width = dimen[0];
        int height = dimen[1];

//        Crop.of(source, destination).withAspect(width, height).start(this);
        Crop.of(source, destination).start(this);
    }

    public String getStringImage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }


    private void uploadProduct(final String name, final String price, final String description, final String phone, final String email2,
                               final String year, final String college){

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("products");
        StorageReference mStorage = FirebaseStorage.getInstance().getReference("ProductImages");

        DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference("users");

        Product prod = new Product();
        prod.setName(name);
        prod.setPrice(Integer.parseInt(price));
        prod.setDescription(description);
        prod.setPhone(phone);
        prod.setYear(year);
        prod.setEmail(AddProductActivity.this.email.getText().toString());
        prod.setCollege(college);
        prod.setImageCount(images_bitmap.size());
        prod.setCity(mPrefs.getString("userCity", ""));
        prod.setState(mPrefs.getString("userState", ""));
        prod.setCountry(mPrefs.getString("userCountry", ""));

        String mLat = mPrefs.getString("lat", null);
        if(mLat==null){
            prod.setLatitude(0.0);
        } else{
            prod.setLatitude(Double.valueOf(mLat));
        }

        String mLong = mPrefs.getString("long", null);
        if(mLong==null){
            prod.setLatitude(0.0);
        } else{
            prod.setLongitude(Double.valueOf(mLong));
        }

        String productUid = UUID.randomUUID().toString();
        prod.setProductId(productUid);

        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = fUser.getUid();
        prod.setUserId(userId);

        mRef.child(productUid).setValue(prod);

//        ArrayList<String> prodList = new ArrayList<>();
//        prodList.add(productUid);
//        mUserRef.child(userId).child("productAdded").setValue(prodList);
        mUserRef.child(userId).child("productAdded").push().setValue(productUid);

        for(int i=0;i<images_bitmap.size();i++){
            Bitmap bitmap = images_bitmap.get(i);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = mStorage.child(productUid).child("images/" + i + ".jpg").putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "onFailure: Image Upload Failed");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d(TAG, "onSuccess: Image Uploaded");
                }
            });
        }

        finish();

    }
}
