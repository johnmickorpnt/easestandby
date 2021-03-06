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
import android.widget.Button;
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

// The object class of the Leaderboard
public class LeaderBoard extends AppCompatActivity {


    // Recycler View
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    List<ModelClass> userlist;
    Adapter adapter;
    Button backBtn;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String grades[] = {"All", "Easy", "Medium", "Hard" ,"Expert"};
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        backBtn = findViewById(R.id.GoBack);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        adapterItems = new ArrayAdapter<>(this,  R.layout.lsit_item, grades);
        autoCompleteTextView.setAdapter(adapterItems);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            //Once clicked on item, it will look at it and it will drop down and will be filtered
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                Log.d("item", item);
                clear();
                item = (item == "All") ? ("0") : (item.trim());
                filter(item);
            }
        });

        userlist = new ArrayList<>();
        initRecyclerView();
        initData();
}
//    //Function that clears the recycle view
    public void clear() {
        userlist.clear();
        adapter.notifyDataSetChanged();
    }

    // For the Filter Function
    public void filter(String grade){
        if(grade=="0"){
            initData();
        }
        db.collection("leaderboards").whereEqualTo("grade", grade).orderBy("score", Query.Direction.DESCENDING)
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
                            userlist.add(new ModelClass(
                                    "No." + String.valueOf(rank),
                                    newScore.getStringScore()+ "pts",
                                    (newScore.getGrade()) + ", " + String.valueOf(newScore.getUsername()),
                                    "_______________________"));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // All the data that has been stored in the leaderboard from the firebase will be assorted in a descending form
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
                            Log.d("yawa", newScore.getGrade());
                            userlist.add(new ModelClass(
                                    "#" + String.valueOf(rank),
                                    newScore.getStringScore()+ "pts",
                                    newScore.getGrade() + ", " + String.valueOf(newScore.getUsername()),
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





