package com.tominc.prustyapp;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class password extends AppCompatActivity {

    private EditText passwordemail;
    private Button resetpassword;
    private FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        passwordemail = (EditText)findViewById(R.id.etforgotemail);
        resetpassword = (Button)findViewById(R.id.btnforgotpass);
        firebaseAuth = FirebaseAuth.getInstance();
        resetpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String useremail = passwordemail.getText().toString().trim();


                if (useremail.equals("")){
                    Toast.makeText(password.this,"Please Enetr Your Registerd Email Id!",Toast.LENGTH_LONG).show();


                }else{

                    firebaseAuth.sendPasswordResetEmail(useremail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                Toast.makeText(password.this,"Password Reset Code Sent To Email!",Toast.LENGTH_LONG).show();
                                finish();
                                startActivity(new Intent(password.this,LoginActivity.class));
                            }else{
                                Toast.makeText(password.this,"Error! Reset Code Not Sent ",Toast.LENGTH_LONG).show();



                            }



                        }
                    });
                }



            }
        });




    }
}
