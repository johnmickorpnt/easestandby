package com.test.easestandby;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class test_activity extends AppCompatActivity {


    // Recycler View
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<ModelClass> userlist;
    Adapter adapter;

    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String grades[] = {"All", "Grade 7", "Grade 8", "Grade 9" ,"Grade 10"};
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        autoCompleteTextView = findViewById(R.id.auto_complete_txt);

        adapterItems = new ArrayAdapter<>(this,  R.layout.lsit_item, grades);
        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                clear();
                item = (item == "All") ? ("0") : (item.substring(item.indexOf(" ")).trim());
                filter(item);
            }
        });

        userlist = new ArrayList<>();
        initRecyclerView();
        initData();
}
//    Function that clears the recycle view
    public void clear() {
        userlist.clear();
        adapter.notifyDataSetChanged();
    }

    public void filter(String grade){
        if(grade=="0"){
            initData();
        }
        db.collection("leaderboards").whereEqualTo("grade",Integer.parseInt(grade)).orderBy("score", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Log.e("error", error.getMessage());
                            return;
                        }
                        int rank = 0;
                        for(DocumentChange dc : value.getDocumentChanges()){
                            rank++;
                            LeaderBoardScores newScore = dc.getDocument().toObject(LeaderBoardScores.class);
                            userlist.add(new ModelClass(R.drawable.ken,
                                    "No." + String.valueOf(rank),
                                    newScore.getStringScore()+ "pts",
                                    "Grade " + String.valueOf(newScore.getGrade()) + ", " + String.valueOf(newScore.getUsername()),
                                    "_______________________"));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    public void initData(){
        db.collection("leaderboards").orderBy("score", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Log.e("error", error.getMessage());
                            return;
                        }
                        int rank = 0;
                        for(DocumentChange dc : value.getDocumentChanges()){
                            rank++;
                            LeaderBoardScores newScore = dc.getDocument().toObject(LeaderBoardScores.class);
                            userlist.add(new ModelClass(R.drawable.ken,
                                    "#" + String.valueOf(rank),
                                    newScore.getStringScore()+ "pts",
                                    "Grade " + String.valueOf(newScore.getGrade()) + ", " + String.valueOf(newScore.getUsername()),
                                    "_______________________"));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void initRecyclerView() {
        recyclerView=findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter= new Adapter(userlist);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

}





