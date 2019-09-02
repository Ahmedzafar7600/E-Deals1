package com.tominc.prustyapp;

import android.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.usb.UsbRequest;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.tominc.prustyapp.adapters.CollegeAutoCompleteAdapter;
import com.tominc.prustyapp.utilities.ImageCompression;
import com.tominc.prustyapp.views.DelayAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.mateware.snacky.Snacky;

public class RegisterActivity extends AppCompatActivity {
    //TODO: Register should be divided into multiple pages
    Button submit;
    TextView clear;
    CircularImageView profile_image;
    Toolbar toolbar;

    TextInputLayout input_layout_f_name, input_layout_l_name, input_layout_email, input_layout_password, input_layout_confirm_password,
                input_layout_phone, input_layout_city, input_layout_year;

    TextInputEditText input_f_name, input_l_name, input_email, input_password, input_confirm_password, input_phone,
                input_year;

    DelayAutoCompleteTextView input_city_auto;

    private String selected_year;

    Spinner spinner_year;

    LinearLayout allRegisterItems;
    RelativeLayout change_profile_pic;

    View pb;

    private final int IMAGE_REQUEST=12;
    SharedPreferences mPref;

    private final String TAG = "RegisterActivity";
    private final int PERMISSION_REQUSET = 14;

    private Uri profilePic;

    private StorageReference mStorageRef;
    private DatabaseReference mRefs;

    private FirebaseAuth mAuth;

    private AwesomeValidation validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        mAuth = FirebaseAuth.getInstance();
        selected_year = "1st Year";
    }

    @Override
    protected void onStart() {
        super.onStart();

        mStorageRef = FirebaseStorage.getInstance().getReference("profiles");
        mRefs = FirebaseDatabase.getInstance().getReference("users");

        toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        toolbar.setTitle("Register");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        mPref = getSharedPreferences("app", MODE_PRIVATE);

        input_layout_f_name = (TextInputLayout) findViewById(R.id.input_layout_f_name);
        input_layout_l_name = (TextInputLayout) findViewById(R.id.input_layout_l_name);
        input_layout_email = (TextInputLayout) findViewById(R.id.input_layout_email);
        input_layout_password = (TextInputLayout) findViewById(R.id.input_layout_password);
        input_layout_confirm_password = (TextInputLayout) findViewById(R.id.input_layout_confirm_password);
        input_layout_city = (TextInputLayout) findViewById(R.id.input_layout_college);
        input_layout_phone = (TextInputLayout) findViewById(R.id.input_layout_phone);
//        input_layout_year = (TextInputLayout) findViewById(R.id.input_layout_year);

        input_f_name = (TextInputEditText) findViewById(R.id.input_f_name);
        input_l_name = (TextInputEditText) findViewById(R.id.input_l_name);
        input_email = (TextInputEditText) findViewById(R.id.input_email);
        input_password = (TextInputEditText) findViewById(R.id.input_password);
        input_confirm_password = (TextInputEditText) findViewById(R.id.input_confirm_password);
//        input_college = (TextInputEditText) findViewById(R.id.input_college);
        input_city_auto = (DelayAutoCompleteTextView) findViewById(R.id.input_college);
        input_phone = (TextInputEditText) findViewById(R.id.input_phone);
//        input_year = (TextInputEditText) findViewById(R.id.input_year);
        spinner_year = (Spinner) findViewById(R.id.spinner_year);

        change_profile_pic = (RelativeLayout) findViewById(R.id.profile_change_pic);

        profile_image = (CircularImageView) findViewById(R.id.register_image);
        submit = (Button) findViewById(R.id.register_submit);
        clear = (TextView) findViewById(R.id.register_clear);
        pb =  findViewById(R.id.logging_in);
        allRegisterItems = (LinearLayout) findViewById(R.id.register_items);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(RegisterActivity.this,
                R.array.year_choices, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_year.setAdapter(adapter);

        spinner_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_year = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        input_city_auto.setThreshold(1);
        input_city_auto.setAdapter(new CollegeAutoCompleteAdapter(RegisterActivity.this));
        input_city_auto.setLoadingIndicator((ProgressBar) findViewById(R.id.pb_loading_indicator));
        input_city_auto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String city = (String) parent.getItemAtPosition(position);
                input_city_auto.setText(city);
            }
        });


        validator.addValidation(RegisterActivity.this, R.id.input_layout_f_name, "^(?!\\s*$).+", R.string.first_name_validation);
        validator.addValidation(RegisterActivity.this, R.id.input_layout_l_name, "^(?!\\s*$).+", R.string.last_name_validation);
        validator.addValidation(RegisterActivity.this, R.id.input_layout_email, Patterns.EMAIL_ADDRESS, R.string.email_validation);
        validator.addValidation(RegisterActivity.this, R.id.input_layout_password, "^(?!\\s*$).+", R.string.register_password_validation);
        validator.addValidation(RegisterActivity.this, R.id.input_layout_confirm_password, "^(?!\\s*$).+", R.string.register_password_validation);
        validator.addValidation(RegisterActivity.this, R.id.input_layout_phone, RegexTemplate.TELEPHONE, R.string.phone_validation);
        validator.addValidation(RegisterActivity.this, R.id.input_layout_college, "[a-zA-Z\\s]+", R.string.college_validation);

        String regexPassword = "(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d])(?=.*[~`!@#\\$%\\^&\\*\\(\\)\\-_\\+=\\{\\}\\[\\]\\|\\;:\"<>,./\\?]).{8,}";

        validator.addValidation(RegisterActivity.this, R.id.register_con_pass, R.id.register_pass, R.string.password_duplication_validation);


        change_profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int permissionCheck = ContextCompat.checkSelfPermission(RegisterActivity.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE);

                if(permissionCheck != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUSET);
                } else{
                    pickPhoto();
                }

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validator.clear();
                if(validator.validate()){
                    String s_name = input_f_name.getText().toString() + " " + input_l_name.getText().toString();
                    String s_email = input_email.getText().toString();
                    String s_pass = input_password.getText().toString();
                    String s_c_pass = input_confirm_password.getText().toString();
                    String s_phone = input_phone.getText().toString();
//                    String s_college = input_college.getText().toString();
                    String s_city = input_city_auto.getText().toString();
                    String s_year = selected_year;

                    if (s_name.length() == 0 || s_email.length() == 0 || s_pass.length() == 0 || s_c_pass.length() == 0
                            || s_phone.length() == 0 || s_c_pass.length() == 0 || s_city.length() == 0
                            || s_year.length()==0) {
                        Toast.makeText(getApplicationContext(), "Fill all details", Toast.LENGTH_SHORT).show();
                        Snacky.builder()
                                .setActivty(RegisterActivity.this)
                                .setText(R.string.fill_details_warning)
                                .setDuration(Snacky.LENGTH_SHORT)
                                .error()
                                .show();
                    } else {
                        if (s_pass.equals(s_c_pass)) {
                            logingInView();
                            addUser(s_name, s_email, s_pass, s_city, s_phone, s_year);
                        } else {
                           Toast.makeText(getApplicationContext(), "Password donot match", Toast.LENGTH_SHORT).show();
                            Snacky.builder()
                                    .setActivty(RegisterActivity.this)
                                    .setText(R.string.password_mismatch_warning)
                                    .setDuration(Snacky.LENGTH_SHORT)
                                    .error()
                                    .show();
                        }
                    }
                }

            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeValidationInfo();
            }
        });

    }

    private void pickPhoto(){
        Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(in, IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUSET:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickPhoto();
                } else{
     Toast.makeText(getApplicationContext(), "Permisston required to upload pic", Toast.LENGTH_SHORT)
                         .show();
                    Snacky.builder()
                            .setActivty(RegisterActivity.this)
                            .setText(R.string.permission_warning)
                            .setDuration(Snacky.LENGTH_SHORT)
                            .warning()
                            .show();
                    defaultView();
                }
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST){
            if(resultCode == RESULT_OK){
                Uri selectedImage = data.getData();
                profilePic = selectedImage;
                    ImageCompression compress = new ImageCompression(RegisterActivity.this);
                    Bitmap bitmap = compress.compressImage(selectedImage.toString());
                    //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
//                    Bitmap bitmap = BitmapFactory.decodeFile(imgDecoableString);
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                    int height = bitmap.getHeight();
//                    int width = bitmap.getWidth();
//                    Bitmap bmp = Bitmap.createScaledBitmap(bitmap, 100, 100, true);

                    profile_image.setImageBitmap(bitmap);
            }
        }
    }

    private void addUser(final String name, final String email, final String pass, final String college, final String phone, final String year){

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(!task.isSuccessful()){
//                            Toast.makeText(RegisterActivity.this, "Unable to add user ", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: " + task.getException());
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {
//                                RegisterActivity.this.pass.setError(getString(R.string.error_weak_password));
                                Snacky.builder()
                                        .setActivty(RegisterActivity.this)
                                        .setText(R.string.error_weak_password)
                                        .setDuration(Snacky.LENGTH_SHORT)
                                        .error()
                                        .show();
                                RegisterActivity.this.input_layout_password.requestFocus();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
//                                RegisterActivity.this.email.setError(getString(R.string.error_invalid_email));
                                Snacky.builder()
                                        .setActivty(RegisterActivity.this)
                                        .setText(R.string.error_invalid_email)
                                        .setDuration(Snacky.LENGTH_SHORT)
                                        .error()
                                        .show();
                                RegisterActivity.this.input_layout_email.requestFocus();
                            } catch(FirebaseAuthUserCollisionException e) {
//                                RegisterActivity.this.email.setError(getString(R.string.error_user_exists));
                                Snacky.builder()
                                        .setActivty(RegisterActivity.this)
                                        .setText(R.string.error_user_exists)
                                        .setDuration(Snacky.LENGTH_SHORT)
                                        .error()
                                        .show();
                                RegisterActivity.this.input_layout_email.requestFocus();
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                            return;
                        }

                        FirebaseUser firebaseUser = task.getResult().getUser();

                        Log.d(TAG, "onComplete: User Created");
                        if(profilePic != null) {

                            mStorageRef = mStorageRef.child("images/" + firebaseUser.getUid() + "/profile.jpg");
                            StorageMetadata metadata = new StorageMetadata.Builder()
                                    .setContentType("image/jpg")
                                    .build();
                            UploadTask uploadTask = mStorageRef.putFile(profilePic, metadata);


                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Image cannot be uploaded");
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    Log.d(TAG, "onSuccess: File Uploaded succesfully");
                                }
                            });

                        }


                        UserProfileChangeRequest profileUpdates;
                        if(profilePic != null){
                            profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .setPhotoUri(profilePic)
                                    .build();
                        } else{
                            profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                        }

                        firebaseUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d(TAG, "onComplete: User Profile Updated");
                                        }
                                    }
                                });


                        String userId = mAuth.getCurrentUser().getUid();

                        User user = new User();
                        user.setName(name);
                        user.setEmail(email);
                        user.setCollege(college);
                        user.setPhone(phone);
                        user.setYear(year);
                        user.setEmail(RegisterActivity.this.input_email.getText().toString());
                        user.setUserId(userId);

                        mRefs.child(userId).setValue(user);


                        SharedPreferences.Editor edit = mPref.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(user);
                        edit.putString("user", json);
                        edit.putString("logedIn", "yes");
                        edit.apply();

                        Intent in = new Intent(RegisterActivity.this, MainActivity.class);
//                        in.putExtra("user", user);
                        startActivity(in);
                        finish();
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(RegisterActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        Snacky.builder()
                                .setActivty(RegisterActivity.this)
                                .setText(R.string.user_creation_error)
                                .setDuration(Snacky.LENGTH_SHORT)
                                .error()
                                .show();
                        defaultView();
                    }
                });
    }

    private void removeValidationInfo(){
        validator.clear();
    }

    private void logingInView(){
        allRegisterItems.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);
    }

    private void defaultView(){
        allRegisterItems.setVisibility(View.VISIBLE);
        pb.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent in = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(in);
        finish();
    }
}
