package com.tominc.prustyapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.tominc.prustyapp.adapters.CollegeAutoCompleteAdapter;
import com.tominc.prustyapp.utilities.ImageCompression;
import com.tominc.prustyapp.views.DelayAutoCompleteTextView;

import java.io.File;
import java.io.IOException;

import de.mateware.snacky.Snacky;
import es.dmoral.toasty.Toasty;


public class ProfileFragment extends Fragment {

    Toolbar toolbar;
    CircularImageView profile_image;
    TextView name, email, phone, year, college;

    RelativeLayout profile_pic_change;

    TextInputLayout edit_layout_name, edit_layout_phone, edit_layout_college, edit_layout_email, edit_layout_password, edit_layout_confirm_password;
    TextInputEditText edit_input_name, edit_input_phone, edit_input_email, edit_input_password, edit_input_confirm_password;
    LinearLayout profile_change_button, password_change_layout;
    CardView profile_password_change_card;

    ImageView password_dropdown_image;

    DelayAutoCompleteTextView edit_input_college_auto;

    Spinner edit_profile_year_spinner;

    private String selected_year;

    SharedPreferences mPrefs;
    StorageReference mStorage;
    final private String TAG = "ProfileFragment";

    SpinKitView pb;
    View pb_background;

    FloatingActionButton profile_edit_fab, edit_done_fab;
    AwesomeValidation validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);

    boolean isEditOpen = false, isPasswordsVisible = false;

    private final int PERMISSION_REQUSET = 43;
    private final int IMAGE_REQUEST = 44;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(User user) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);

        Bundle bundle = getArguments();
        User user = (User) bundle.getSerializable("user");

        Log.d("ProfileFragment", user.getEmail());

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        mStorage = FirebaseStorage
                .getInstance()
                .getReference("profiles")
                .child("images/" + mUser.getUid() + "/profile.jpg");

        pb = (SpinKitView) root.findViewById(R.id.loading);
        pb_background = root.findViewById(R.id.loading_background);
        name = (TextView) root.findViewById(R.id.show_profile_name);
        email = (TextView) root.findViewById(R.id.show_profile_email);
        phone = (TextView) root.findViewById(R.id.show_profile_phone);
        year = (TextView) root.findViewById(R.id.show_profile_year);
        college = (TextView) root.findViewById(R.id.show_profile_college);
        profile_image = (CircularImageView) root.findViewById(R.id.show_profile_image);
        profile_pic_change = (RelativeLayout) root.findViewById(R.id.profile_change_pic);
        profile_change_button = (LinearLayout) root.findViewById(R.id.profile_change_password);
        password_change_layout = (LinearLayout) root.findViewById(R.id.profile_password_layout);
        profile_password_change_card = (CardView) root.findViewById(R.id.profile_password_change_card);
        password_dropdown_image = (ImageView) root.findViewById(R.id.profile_drop_down_image);

        edit_layout_name = (TextInputLayout) root.findViewById(R.id.edit_profile_name);
        edit_layout_phone = (TextInputLayout) root.findViewById(R.id.edit_profile_phone);
//        edit_layout_year = (TextInputLayout) root.findViewById(R.id.edit_profile_year);
        edit_layout_college = (TextInputLayout) root.findViewById(R.id.edit_profile_college);
        edit_layout_password = (TextInputLayout) root.findViewById(R.id.profile_layout_password);
        edit_layout_confirm_password = (TextInputLayout) root.findViewById(R.id.profile_layout_confirm_password);
        edit_layout_email = (TextInputLayout) root.findViewById(R.id.edit_profile_email);
        edit_profile_year_spinner = (Spinner) root.findViewById(R.id.edit_profile_year);

        edit_input_name = (TextInputEditText) root.findViewById(R.id.input_profile_name);
        edit_input_phone = (TextInputEditText) root.findViewById(R.id.input_profile_phone);
        edit_input_email = (TextInputEditText) root.findViewById(R.id.input_profile_email);
        edit_input_password = (TextInputEditText) root.findViewById(R.id.profile_input_password);
        edit_input_confirm_password = (TextInputEditText) root.findViewById(R.id.profile_input_confirm_password);
//        edit_input_year = (TextInputEditText) root.findViewById(R.id.input_profile_year);
//        edit_input_college = (TextInputEditText) root.findViewById(R.id.input_profile_college);

        edit_input_college_auto = (DelayAutoCompleteTextView) root.findViewById(R.id.input_profile_college);

        profile_edit_fab = (FloatingActionButton) root.findViewById(R.id.profile_edit);
        edit_done_fab = (FloatingActionButton) root.findViewById(R.id.profile_edit_done);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.year_choices, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        edit_profile_year_spinner.setAdapter(adapter);

        selected_year = year.getText().toString();

        edit_profile_year_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_year = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edit_input_college_auto.setThreshold(1);
        edit_input_college_auto.setAdapter(new CollegeAutoCompleteAdapter(getActivity()));
        edit_input_college_auto.setLoadingIndicator((ProgressBar) root.findViewById(R.id.pb_loading_indicator));
        edit_input_college_auto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String college = (String) parent.getItemAtPosition(position);
                edit_input_college_auto.setText(college);
            }
        });

        showProfileView();
        showLoading();
        addValidations();

        name.setText(user.getName());
        email.setText(user.getEmail());
        phone.setText("Phone: " + user.getPhone());
        year.setText("Type: " + user.getYear());
        college.setText("City: " + user.getCollege());

        profile_edit_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfileView();
            }
        });

        edit_done_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfile();
                showProfileView();
            }
        });

        profile_pic_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE);

                if(permissionCheck != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUSET);
                } else{
                    pickPhoto();
                }
            }
        });

        final File tempFile;
        try {
            tempFile = File.createTempFile("images", "jpg");

            mStorage.getFile(tempFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            if(!getActivity().isDestroyed()){
                                Glide.with(getActivity())
                                        .load(tempFile).diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(profile_image);
                                hideLoading();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Profile image could not be downloaded " + e.toString());
                            // make avatar is default
                            hideLoading();
//                            Glide.with(getActivity())
//                                    .load(R.drawable.ic_male_avatar)
//                                    .into(profile_image);
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "onCreateView: Temp file could not be created");
        }


        profile_change_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordView();
            }
        });

        return root;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUSET:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickPhoto();
                } else{
                    Snacky.builder()
                            .setActivty(getActivity())
                            .setText(R.string.permission_warning)
                            .setDuration(Snacky.LENGTH_SHORT)
                            .warning()
                            .show();
                }
        }
    }

    private void pickPhoto(){
        Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(in, IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == IMAGE_REQUEST){
            if(resultCode == getActivity().RESULT_OK){
                changeProfilePic(data.getData());
            }
        }
    }

    private void hideLoading(){
        pb.setVisibility(View.GONE);
        pb_background.setVisibility(View.GONE);
    }

    private void showLoading(){
        pb.setVisibility(View.VISIBLE);
        pb_background.setVisibility(View.VISIBLE);
    }

    private void showProfileView(){
        isEditOpen = !isEditOpen;
        name.setVisibility(View.VISIBLE);
        phone.setVisibility(View.VISIBLE);
        year.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);
        college.setVisibility(View.VISIBLE);

        edit_layout_name.setVisibility(View.GONE);
        edit_layout_email.setVisibility(View.GONE);
        edit_layout_phone.setVisibility(View.GONE);
        edit_profile_year_spinner.setVisibility(View.GONE);
        edit_layout_college.setVisibility(View.GONE);
        profile_password_change_card.setVisibility(View.GONE);

        profile_edit_fab.setVisibility(View.VISIBLE);
        edit_done_fab.setVisibility(View.GONE);

        profile_pic_change.setVisibility(View.GONE);

    }

    private void editProfileView(){
        isEditOpen = !isEditOpen;
        name.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
        phone.setVisibility(View.GONE);
        year.setVisibility(View.GONE);
        college.setVisibility(View.GONE);

        edit_layout_name.setVisibility(View.VISIBLE);
        edit_layout_email.setVisibility(View.VISIBLE);
        edit_layout_phone.setVisibility(View.VISIBLE);
        edit_profile_year_spinner.setVisibility(View.VISIBLE);
        edit_layout_college.setVisibility(View.VISIBLE);
        profile_password_change_card.setVisibility(View.VISIBLE);

        edit_input_name.setText(name.getText().toString());
        edit_input_email.setText(email.getText().toString());
        edit_input_phone.setText(phone.getText().toString().replace("Phone: ", ""));
//        edit_input_year.setText(year.getText().toString());
        selected_year = year.getText().toString();
        edit_input_college_auto.setText(college.getText().toString().replace("City: ", ""));
        selected_year = year.getText().toString().replace("Year: ", "");

        String[] years = getResources().getStringArray(R.array.year_choices);

        int temp_pos=0;
        for(int i=0;i<years.length;i++){
            if(selected_year.equals(years[i])){
                temp_pos = i;
                break;
            }
        }

        edit_profile_year_spinner.setSelection(temp_pos);

        profile_edit_fab.setVisibility(View.GONE);
        edit_done_fab.setVisibility(View.VISIBLE);

        profile_pic_change.setVisibility(View.VISIBLE);
    }

    private void changeProfile(){
        clear_validation();
        if(validator.validate()){
            String new_name = edit_input_name.getText().toString();
            String new_phone = edit_input_phone.getText().toString();
            String new_email = edit_input_email.getText().toString();
//            String new_year = edit_input_year.getText().toString();
            String new_year = selected_year;
            String new_college = edit_input_college_auto.getText().toString();
            String new_password = edit_input_password.getText().toString();
            String new_confirm_password = edit_input_confirm_password.getText().toString();

            if(new_password.length() !=0 && new_password.equals(new_confirm_password)){

            } else{
                buildSnackBar("Password do not match");
                return;
            }

            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            Boolean success=true;

            if(!new_name.equals(name.getText().toString())){
                Task<Void> task = mRef.child("name").setValue(new_name);
                if(!task.isSuccessful()) success=false;
            }
            if(!new_phone.equals(phone.getText().toString())){
                Task<Void> task = mRef.child("phone").setValue(new_phone);
                if(!task.isSuccessful()) success=false;
            }
            if(!new_year.equals(year.getText().toString())){
                Task<Void> task = mRef.child("year").setValue(new_year);
                if(!task.isSuccessful()) success=false;
            }
            if(!new_college.equals(college.getText().toString())){
                Task<Void> task =  mRef.child("college").setValue(new_college);
                if(!task.isSuccessful()) success=false;
            }

            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            if(!new_email.equals(email.getText().toString())){
                Task<Void> task = mUser.updateEmail(new_email);
                if(!task.isSuccessful()) success = false;
            }
            if(new_password.length()!=0){
                Task<Void> task = mUser.updatePassword(new_password);
                if(!task.isSuccessful()) success = false;
            }

            if(success){
                Snacky.builder()
                        .setActivty(getActivity())
                        .setDuration(Snacky.LENGTH_SHORT)
                        .setText("Profile Changed")
                        .success();

            } else{
                buildSnackBar("Profile Change Failed");
            }

        }

    }

    public void onBackPressed(){
        if(isEditOpen){
            showProfileView();
        }
    }

    private void buildSnackBar(String message){
        Snacky.builder()
                .setActivty(getActivity())
                .setText(message)
                .setDuration(Snacky.LENGTH_SHORT)
                .error();
    }


    private void changeProfilePic(final Uri selectedImage){
        showLoading();

        if(selectedImage!=null){
            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("profiles");
            mStorageRef = mStorageRef.child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/profile.jpg");
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build();
            UploadTask uploadTask = mStorageRef.putFile(selectedImage, metadata);


            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Image cannot be uploaded");
                    Snacky.builder()
                            .setActivty(getActivity())
                            .setDuration(Snacky.LENGTH_SHORT)
                            .setText("Image could not be uploaded")
                            .error()
                            .show();
                    hideLoading();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d(TAG, "onSuccess: File Uploaded succesfully");
                    ImageCompression compress = new ImageCompression(getActivity());
                    Bitmap bitmap = compress.compressImage(selectedImage.toString());
                    profile_image.setImageBitmap(bitmap);
//                    Glide.with(getActivity())
//                            .load(bitmap)
//                            .into(profile_image);
                    hideLoading();
                }
            });
        }


    }

    public void clear_validation(){
        validator.clear();
    }

    public void addValidations(){
        validator.addValidation(getActivity(), R.id.edit_profile_name, "^(?!\\s*$).+", R.string.first_name_validation);
        validator.addValidation(getActivity(), R.id.input_layout_email, Patterns.EMAIL_ADDRESS, R.string.email_validation);
        validator.addValidation(getActivity(), R.id.edit_profile_phone, "^(?!\\s*$).+", R.string.phone_validation);
        validator.addValidation(getActivity(), R.id.edit_profile_college, "^(?!\\s*$).+", R.string.error_empty_college);
    }


    private void togglePasswordView(){
        if(isPasswordsVisible){
            RotateAnimation rotate = new RotateAnimation(90, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//            RotateAnimation rotate = new RotateAnimation(90, 180);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            rotate.setInterpolator(new LinearInterpolator());
            password_dropdown_image.startAnimation(rotate);

            Animation hideAnim = new AnimationUtils().loadAnimation(getActivity(), R.anim.hide_anim);
            password_change_layout.startAnimation(hideAnim);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    password_change_layout.setVisibility(View.GONE);
                }
            }, 300);

        } else{
            RotateAnimation rotate = new RotateAnimation(0, 90, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//            RotateAnimation rotate = new RotateAnimation(180, 90);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            rotate.setInterpolator(new LinearInterpolator());
            password_dropdown_image.startAnimation(rotate);


            password_change_layout.setVisibility(View.VISIBLE);
            Animation showupAnim = new AnimationUtils().loadAnimation(getActivity(), R.anim.show_up_anim);
            password_change_layout.startAnimation(showupAnim);


        }
        isPasswordsVisible = !isPasswordsVisible;
    }
}
