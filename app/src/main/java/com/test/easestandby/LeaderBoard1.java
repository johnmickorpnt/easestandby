package com.test.easestandby;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class LeaderBoard1 extends AppCompatActivity {

    // Recycler View
    private Button GoBack;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<ModelClass>userlist;
    Adapter adapter;
    final FirebaseDatabase database = FirebaseDatabase.getInstance("https://easestandby-a804f-default-rtdb.asia-southeast1.firebasedatabase.app");
    DatabaseReference ref = database.getReference("leaderboards");


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        // Attach a listener to read the data at our posts reference
        Log.v("hehe", "hihi");

        GoBack = findViewById(R.id.GoBack);

        //Go Back
        GoBack = findViewById(R.id.GoBack);
        GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }



    private void initData() {

        userlist = new ArrayList<>();
        userlist.add(new ModelClass("No.1","10pts", "Grade 7, Ken Cocjin","_______________________"));
        userlist.add(new ModelClass("No.1","10pts", "Grade 7, Ken Cocjin","_______________________"));

    }

    private void initRecyclerView() {

        recyclerView=findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new Adapter(userlist);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}