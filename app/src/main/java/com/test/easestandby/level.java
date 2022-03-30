package com.test.easestandby;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class level extends AppCompatActivity{
    Button Easy,Average,Hard,Difficult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

//     For Grade_7
    Easy = findViewById(R.id.Easy);
    Easy.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), Grade_7.class));
        }
    });
    // For Grade_8
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


    }
}