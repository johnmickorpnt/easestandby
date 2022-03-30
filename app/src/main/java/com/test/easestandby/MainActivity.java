package com.test.easestandby;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.unity3d.player.UnityPlayerActivity;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    Button level,game,leaderboard;
    private Button profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         //Story Mode
        game = findViewById(R.id.play);
        game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UnityPlayerActivity.class);
                startActivity(intent);
            }
        });

        //level
      level = findViewById(R.id.level);
      level.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), com.test.easestandby.level.class));
            }
        });


      //LeaderBoards
       leaderboard = findViewById(R.id.leaderboards);
        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), com.test.easestandby.test_activity.class));
            }
        });

        //Profile btn
        profileBtn = findViewById(R.id.profileBtn);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Openprofile();
            }
        });
    }
    //Switching to profile
    public void Openprofile(){
        Intent intent = new Intent(this,profile.class);
        startActivity(intent);
    }


    //Logout
    public void logout(android.view.View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),login.class));
        finish();
    }





}