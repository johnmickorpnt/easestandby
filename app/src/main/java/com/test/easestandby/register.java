package com.test.easestandby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {

    EditText regfname, regsurname, regemail, regnumber, regpassword, regrepeatpassword;
    Button RegisterBtn;
    TextView login;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regfname = findViewById(R.id.regfname);
        regsurname = findViewById(R.id.regsurname);
        regemail = findViewById(R.id.regemail);
        regnumber = findViewById(R.id.regnumber);
        regpassword = findViewById(R.id.regpassword);
        regrepeatpassword = findViewById(R.id.regrepeatpassword);
        RegisterBtn = findViewById(R.id.RegisterBtn);
        login = findViewById(R.id.login);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        if (fAuth.getCurrentUser() !=null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = regemail.getText().toString().trim();
                String password = regpassword.getText().toString().trim();
                String repeatpassword = regrepeatpassword.getText().toString().trim();
                String fname = regfname.getText().toString();
                String surname = regsurname.getText().toString();
                String number = regnumber.getText().toString();

                if (TextUtils.isEmpty(fname)) {
                    regfname.setError("First Name is required.");
                    return;
                }

                if (TextUtils.isEmpty(surname)) {
                    regsurname.setError("Surname is required.");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    regemail.setError("Email is required.");
                    return;
                }

                if (TextUtils.isEmpty(number)) {
                    regnumber.setError("Mobile number is required.");
                    return;
                }

                else if (number.length() != 11) {
                    regnumber.setError("Mobile number should be atleast 11 numbers");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    regpassword.setError("password is required.");
                    return;
                }

                if (password.length() < 8) {
                    regpassword.setError("password must have atleast 8 characters");
                    return;
                }

                if (!password.equals(repeatpassword)){
                    regrepeatpassword.setError("passwords do not match");
                    return;
                }



                progressBar.setVisibility(View.VISIBLE);

                //register
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            //send verification link
//                            FirebaseUser Fuser = fAuth.getCurrentUser();
//                            Fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void unused) {
//                                    Toast.makeText(register.this, "Verification link has sent to your email", Toast.LENGTH_SHORT).show();
//
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.d("TAG", "onFailure: Email not sent" + e.getMessage());
//                                }
//                            });


                            Toast.makeText(register.this, "User Created", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            Map<String, Boolean> levels = new HashMap<>();
                            levels.put("is_level1_clear", false);
                            levels.put("is_level2_clear", false);
                            levels.put("is_level3_clear", false);
                            levels.put("is_level4_clear", false);
                            user.put("fname", fname);
                            user.put("surname", surname);
                            user.put("email", email);
                            user.put("number", number);
                            user.put("levels", levels);
                            user.put("password", password);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("TAG", "onSuccess: user profile is created for" + userID);
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }

                        else {
                            Toast.makeText(register.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), login.class));
            }
        });
    }
}