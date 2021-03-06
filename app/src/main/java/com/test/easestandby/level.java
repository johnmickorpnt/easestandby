package com.test.easestandby;


import static com.test.easestandby.Grade_7.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class level extends AppCompatActivity{
    CountDownTimer Timer;
    public  int secondsRemaining = 30;
    Button Easy,Average,Hard,Difficult;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    String UserID;
    StorageReference storageReference;

    private Button GoBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        UserID = fAuth.getCurrentUser().getUid();
        user = fAuth.getCurrentUser();

        GoBack = findViewById(R.id.GoBack);

        //Go Back
        GoBack = findViewById(R.id.GoBack);
        GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //     For Grade_7
        Easy = findViewById(R.id.Easy);
        Easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Grade_7.class));
//                timer.startTimer();
            }
        });


//         For Grade_8
        Average = findViewById(R.id.Average);
        Average.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Grade_8.class));
            }
        });
        // For Grade_9
        Hard = findViewById(R.id.Hard);
        Hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Grade_9.class));
            }
        });

        // For Grade_10
        Difficult = findViewById(R.id.Difficult);
        Difficult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Grade_10.class));
            }
        });
        initBtns();
    }

    // This function is used for checking if the user met the passing score to pass the current level in order to unlock the next level
    private void initBtns(){
        DocumentReference documentReference = fStore.collection("users").document(UserID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e){
                if (documentSnapshot.exists()){
                    try {
                        HashMap<String, Boolean> levels = (HashMap<String, Boolean>) documentSnapshot.get("levels");
                        Log.d("tagZ", Boolean.toString(levels.get("is_level1_clear")));
                        Log.d("tagZ", String.valueOf(levels.size()));
                        for(int x = 1; x < levels.size(); x++){
                            Button btn = (x == 1) ? (Average) : ((x == 2) ? (Hard) : (Difficult));
                            if(x == 1 && !levels.get("is_level" + x + "_clear")) disableButton(btn);
                            if(x == 2 && !levels.get("is_level" + x + "_clear")) disableButton(btn);
                            if(x == 3 && !levels.get("is_level" + x + "_clear")) disableButton(btn);
                        }
                    }catch (Exception exception){
                        Log.d("error", e.getMessage());
                    }
                }
                else{
                    Log.d("tag","Document do not exists");
                }
            }
        });
    }
    // Will be disabled if not yet unlocked
    public void disableButton(Button btn){
        btn.setEnabled(false);
        btn.getBackground().setAlpha(10);
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }
}