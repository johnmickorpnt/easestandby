package com.test.easestandby;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LeaderBoard extends AppCompatActivity {

    // Recycler View
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
        Log.v("yawa", "ahhaahah");
    }

    private void initData() {

        userlist = new ArrayList<>();
        userlist.add(new ModelClass(R.drawable.ken,"No.1","10pts", "Grade 7, Ken Cocjin","_______________________"));
        userlist.add(new ModelClass(R.drawable.ken,"No.1","10pts", "Grade 7, Ken Cocjin","_______________________"));

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