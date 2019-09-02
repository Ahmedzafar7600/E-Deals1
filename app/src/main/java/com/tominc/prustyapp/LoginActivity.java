package com.tominc.prustyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import de.mateware.snacky.Snacky;

public class LoginActivity extends AppCompatActivity {
    TextView reset, skip, register;


    Button submit;

    View pb;
    LinearLayout allLoginItems,registerItems;

    TextInputEditText input_email, input_password;

    private final String LOGIN_URL = Config.BASE_URL + "login.php";
    SharedPreferences mPrefs;

    private final String TAG = "LoginActivity";
    SharedPreferences mPref;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference mRefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login2);

        pb =  findViewById(R.id.logging_in);
        allLoginItems = (LinearLayout) findViewById(R.id.login_items);
        registerItems = (LinearLayout) findViewById(R.id.register_layout);

        mAuth = FirebaseAuth.getInstance();

        mPref = getSharedPreferences("app", MODE_PRIVATE);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    logingInView();
                    Log.d(TAG, "onAuthStateChanged: User Logged In");
                    successfulLogin(user.getUid());
                } else{
                    Log.d(TAG, "onAuthStateChanged: User not Logged In");
                }
            }
        };

        mPrefs = getSharedPreferences("app", MODE_PRIVATE);
        String isLogin = mPrefs.getString("logedIn", null);
        if(isLogin!=null && isLogin.equals("yes")){
            Intent in = new Intent(LoginActivity.this, MainActivity.class);
//            startActivity(in);
//            finish();
        }

        input_email = (TextInputEditText) findViewById(R.id.input_email);
        input_password = (TextInputEditText) findViewById(R.id.input_password);
        submit = (Button) findViewById(R.id.login_submit);
        reset = (TextView) findViewById(R.id.reset);


        findViewById(R.id.login_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signInAnonymously()
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "onComplete: User logged annousmously");

                                if(!task.isSuccessful()){
//                                    Toast.makeText(LoginActivity.this, "Annonymous User Authentication Failed", Toast.LENGTH_SHORT).show();
                                    Snacky.builder().setView(allLoginItems)
                                            .setActivty(LoginActivity.this)
                                            .setText(R.string.annon_user_auth_error)
                                            .setDuration(Snacky.LENGTH_SHORT)
                                            .error();
                                }
                                Intent in = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(in);
                                finish();
                            }
                        });

            }
        });

        findViewById(R.id.login_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(in);
                finish();
            }
        });
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,password.class));

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s_email = input_email.getText().toString();
                String s_pass = input_password.getText().toString();
                if(Patterns.EMAIL_ADDRESS.matcher(s_email).matches()){
                    if (s_email.length() == 0 || s_pass.length() == 0) {
//                        Toast.makeText(getApplicationContext(), "Incomplete Information", Toast.LENGTH_SHORT).show();
                        Snacky.builder().setView(allLoginItems)
                                .setActivty(LoginActivity.this)
                                .setText(R.string.fill_details_warning)
                                .setDuration(Snacky.LENGTH_SHORT)
                                .error();
                    } else {
                        logingInView();
                        loginUser(s_email, s_pass);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    private void loginUser(final String email, final String pass){

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: User Signed In");
                        if(!task.isSuccessful()){
                            defaultView();
                            try{
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
//                                Toast.makeText(LoginActivity.this, "Either email or password is inccorrect", Toast.LENGTH_SHORT).show();
                                Snacky.builder()
                                        .setActivty(LoginActivity.this)
                                        .setText(R.string.email_pass_incorrect_error)
                                        .setDuration(Snacky.LENGTH_SHORT)
                                        .error()
                                        .show();
                                LoginActivity.this.input_email.requestFocus();
                            } catch (FirebaseAuthInvalidUserException e){
//                                Toast.makeText(LoginActivity.this, "Invalid Username", Toast.LENGTH_SHORT).show();
                                Snacky.builder()
                                        .setActivty(LoginActivity.this)
                                        .setText(R.string.invalid_username_error)
                                        .setDuration(Snacky.LENGTH_SHORT)
                                        .error()
                                        .show();
                            } catch (Exception e) {
//                                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                                Snacky.builder()
                                        .setActivty(LoginActivity.this)
                                        .setText(R.string.auth_failed_error)
                                        .setDuration(Snacky.LENGTH_SHORT)
                                        .error()
                                        .show();
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void successfulLogin(String userId){

        Log.d(TAG, "onComplete: Userid: " + userId);
        mRefs = FirebaseDatabase.getInstance().getReference("users").child(userId);
        mRefs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                User user = dataSnapshot.getValue(User.class);
                Log.d(TAG, "onDataChange: " + user.toString());
                if(user!=null) {
                    Log.d(TAG, "onDataChange: Got user info " + user.toString());

                    SharedPreferences.Editor edit = mPref.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(user);
                    edit.putString("user", json);
                    edit.putString("logedIn", "yes");
                    edit.apply();

                    Intent in = new Intent(LoginActivity.this, MainActivity.class);
//                                  in.putExtra("user", user);
                    startActivity(in);
                } else{
                    Log.d(TAG, "onDataChange: user logged in annoymously");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                defaultView();
                Log.d(TAG, "onCancelled: Getting user Info failed");
            }
        });
    }

    private void logingInView(){
        registerItems.setVisibility(View.GONE);
        allLoginItems.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);
    }

    private void defaultView(){
        registerItems.setVisibility(View.GONE);
        allLoginItems.setVisibility(View.VISIBLE);
        pb.setVisibility(View.INVISIBLE);
    }
}
