package com.test.easestandby;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity {
        Handler h = new Handler();

        public static ArrayList<OptionClass> listofQ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        listofQ=new ArrayList<>();
        listofQ.add(new OptionClass("What does PAGASA means?","A","B","C","D","A"));
        listofQ.add(new OptionClass("What does PAGASA means?","A","B","C","D","A"));
        listofQ.add(new OptionClass("What does PAGASA means?","A","B","C","D","A"));
        listofQ.add(new OptionClass("What does PAGASA means?","A","B","C","D","A"));
        listofQ.add(new OptionClass("What does PAGASA means?","A","B","C","D","A"));
        listofQ.add(new OptionClass("What does PAGASA means?","A","B","C","D","A"));
        listofQ.add(new OptionClass("What does PAGASA means?","A","B","C","D","A"));
        listofQ.add(new OptionClass("What does PAGASA means?","A","B","C","D","A"));
        listofQ.add(new OptionClass("What does PAGASA means?","A","B","C","D","A"));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent (SplashScreen.this,register.class);
                startActivity(i);
                finish();
            }
        },4000);
    }
}