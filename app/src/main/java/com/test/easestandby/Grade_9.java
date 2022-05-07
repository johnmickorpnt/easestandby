package com.test.easestandby;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Grade_9 extends level {

    private TextView optionA,optionB,optionC,optionD;
    private TextView questionnumber,question,score;
    private TextView chechkout1,checkout2;
    public TextView Timer;
    private CountDownTimer countDownTimer;
    private  long timeLeftMilsec = 301000;
    int currentIndex;
    int mscore=0;
    int qn=1;
    private boolean[] history = new boolean[20];

    ProgressBar progressBar;
    int CurrentQuestion,CurrentOptionA,CurrentOptionB,CurrentOptionC,CurrentOptionD;
    List<Integer> usedNumbers = new ArrayList<Integer>();
    private answerclass[] questionBank = new answerclass[20];

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    String UserID;
    StorageReference storageReference;
    int grade = 9;
    final int PROGRESS_BAR = (int) Math.ceil(100/questionBank.length);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz3);
        generateQuestions();
        optionA=findViewById(R.id.optionA);
        optionB=findViewById(R.id.optionB);
        optionC=findViewById(R.id.optionC);
        optionD=findViewById(R.id.optionD);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        UserID = fAuth.getCurrentUser().getUid();
        user = fAuth.getCurrentUser();

        Timer = findViewById(R.id.TimerTextView);
        question = findViewById(R.id.question);
        score=findViewById(R.id.score);
        questionnumber=findViewById(R.id.QuestionNumber);

        chechkout1=findViewById(R.id.selectoption);
        checkout2=findViewById(R.id.CorrectAnswer);
        progressBar=findViewById(R.id.progress_bar);

        startTimer();
        CurrentQuestion=questionBank[currentIndex].getQuestionid();
        question.setText(CurrentQuestion);
        CurrentOptionA=questionBank[currentIndex].getOptionA();
        optionA.setText(CurrentOptionA);
        CurrentOptionB=questionBank[currentIndex].getOptionB();
        optionB.setText(CurrentOptionB);
        CurrentOptionC=questionBank[currentIndex].getOptionC();
        optionC.setText(CurrentOptionC);
        CurrentOptionD=questionBank[currentIndex].getOptionD();
        optionD.setText(CurrentOptionD);

       optionA.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               checkAnswer(CurrentOptionA);
               updateQuestion();

           }
       });

        optionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(CurrentOptionB);
                updateQuestion();


            }
        });
        optionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(CurrentOptionC);
                updateQuestion();


            }
        });
        optionD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkAnswer(CurrentOptionD);
                updateQuestion();

            }
        });




    }

    private void checkAnswer(int userSelection) {

        int correctanswer=questionBank[currentIndex].getAnswerid();

        chechkout1.setText(userSelection);
        checkout2.setText(correctanswer);

        String m= chechkout1.getText().toString().trim();
        String n=checkout2.getText().toString().trim();

        if(m.equals(n))
        {
            Toast.makeText(getApplicationContext(),"Correct",Toast.LENGTH_SHORT).show();
            mscore=mscore+1;
            history[currentIndex] = true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Incorrect",Toast.LENGTH_SHORT).show();
            history[currentIndex] = false;
        }
    }

    private void save(){
        Map<String, Object> scores = new HashMap<>();
        int finScore = mscore;
        float pct =  ((float) mscore / 20) * (float) 100;
        DocumentReference documentReference = fStore.collection("users").document(UserID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String username = documentSnapshot.getString("email");
                HashMap<String, Boolean> levels = (HashMap<String, Boolean>) documentSnapshot.get("levels");
                if(Math.round(pct) >= 70){
                    passed(levels);
                }
                documentSnapshot.getString("email");
                store(username, scores, finScore);
            }
        });
    }

    private void passed(Map<String, Boolean> levels){
        DocumentReference user = fStore.collection("users").document(UserID);
        levels.put("is_level3_clear", true);
        user.update("levels", levels);
    }

    public void store(String username, Map scoreList, int finScore){
        scoreList.put("score", finScore);
        scoreList.put("grade", "Hard");
        scoreList.put("username", username);
        fStore.collection("leaderboards").document()
                .set(scoreList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("tagZ", "data added!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("tagZ", "not added >:(");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateQuestion() {
        final boolean[] newGame = {false};
        String qHistory = "";
        int qNum = 0;
        currentIndex=(currentIndex+1)%questionBank.length;
        if(currentIndex==0)
        {
            generateQuestions();
            countDownTimer.cancel();
            AlertDialog.Builder alert=new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_view3, null);
            TextView qHistoryText = (TextView) dialogView.findViewById(R.id.qHistory6);
            TextView scoreText = (TextView) dialogView.findViewById(R.id.scoreText2);

            for(boolean v: history){
                Log.v("curr_index", Boolean.toString(v));
                qHistory += ((qNum == 0) ? ("\n") : ("")) + ((v) ? ("Correct") : ("Incorrect")) + "\n";
                qNum++;
            }

            scoreText.setText(String.valueOf(mscore) + "\\20");
            qHistoryText.setText(qHistory);
            alert.setView(dialogView);
            alert.setCancelable(false);
            alert.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    save();
                    finish();
                }
            });

            alert.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    generateQuestions();
                    newGame[0] = true;
                    save();
                    mscore=0;
                    qn=1;
                    progressBar.setProgress(0);
                    score.setText("Score" + mscore +"/" +questionBank.length);
                    questionnumber.setText(qn + "/" + questionBank.length +"Question");
                    timeLeftMilsec = 301000;
                    countDownTimer.start();
                }
            });

            alert.show();

        }

        CurrentQuestion=questionBank[currentIndex].getQuestionid();
        question.setText(CurrentQuestion);
        CurrentOptionA=questionBank[currentIndex].getOptionA();
        optionA.setText(CurrentOptionA);
        CurrentOptionB=questionBank[currentIndex].getOptionB();
        optionB.setText(CurrentOptionB);
        CurrentOptionC=questionBank[currentIndex].getOptionC();
        optionC.setText(CurrentOptionC);
        CurrentOptionD=questionBank[currentIndex].getOptionD();
        optionD.setText(CurrentOptionD);
        qn=qn+1;

        if(qn<=questionBank.length)

        {
            questionnumber.setText(qn + "/" + questionBank.length +"Question");
        }
        score.setText("Score" + mscore +"/" +questionBank.length);
        progressBar.incrementProgressBy(PROGRESS_BAR);

    }


    private void generateQuestions(){
        usedNumbers.clear();
        for(int x = 1; x <= 20;){
            int currIndex = getRandomNumber(1,20);
            Log.i("current_index_random",Integer.toString(currIndex));
            if(currIndex == 1 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_21, R.string.question21_A, R.string.question21_B, R.string.question21_C, R.string.question21_D, R.string.answer_21);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 2 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_22, R.string.question_22A, R.string.question_22B, R.string.question_22C, R.string.question_22D, R.string.answer_22C);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 3 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_23, R.string.question_23A, R.string.question_23B, R.string.question_23C, R.string.question_23D, R.string.answer_23);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 4 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_24, R.string.question_24A, R.string.question_24B, R.string.question_24C, R.string.question_24D, R.string.answer_24);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 5 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_25, R.string.question_25A, R.string.question_25B, R.string.question_25C, R.string.question_25D, R.string.answer_25A);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 6 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_26, R.string.question_26A, R.string.question_26B, R.string.question_26C, R.string.question_26D, R.string.answer_26);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 7 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_27, R.string.question_27A, R.string.question_27B, R.string.question_27C, R.string.question_27D, R.string.answer_27B);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 8 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_28, R.string.question_28A, R.string.question_28B, R.string.question_28C, R.string.question_28D, R.string.answer_28A);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 9 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_29, R.string.question_29A, R.string.question_29B, R.string.question_29C, R.string.question_29D, R.string.answer_29B);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 10 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_30, R.string.question_30A, R.string.question_30B, R.string.question_30C, R.string.question_30D, R.string.answer_30A);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 11 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_270, R.string.question270_A, R.string.question270_B, R.string.question_220C, R.string.question270_D, R.string.answer_270);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 12 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_271, R.string.question271_A, R.string.question271_B, R.string.question271_C, R.string.question271_D, R.string.answer_271);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 13 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_272, R.string.question272_A, R.string.question272_B, R.string.question272_C, R.string.question272_D, R.string.answer_272);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 14 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_273, R.string.question273_A, R.string.question273_B, R.string.question273_C, R.string.question273_D, R.string.answer_273);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 15 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_274, R.string.question274_A, R.string.question274_B, R.string.question274_C, R.string.question274_D, R.string.answer_274);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 16 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_275, R.string.question275_A, R.string.question275_B, R.string.question275_C, R.string.question275_D, R.string.answer_275);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 17 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_276, R.string.question276_A, R.string.question276_B, R.string.question276_C, R.string.question276_D, R.string.answer_276);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 18 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_278, R.string.question278_A, R.string.question278_B, R.string.question278_C, R.string.question278_D, R.string.answer_278);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 19 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_279, R.string.question279_A, R.string.question279_B, R.string.question279_C, R.string.question279_D, R.string.answer_279);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 20 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_280, R.string.question280_A, R.string.question280_B, R.string.question280_C, R.string.question280_D, R.string.answer_280);
                usedNumbers.add(currIndex);
                x++;
            }

        }
        Log.i("to_string",toString(usedNumbers));
    }
    public static int getRandomNumber(int min, int max) {
        return (new Random()).nextInt((max - min) + 1) + min;
    }

    public static String toString(List<Integer> s){
        String full = new String();
        for(int x = 0; x < s.size(); x++){
            full += s.get(x) + ", ";
        }
        return full;
    }


    // Countdown TIMER
    public void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftMilsec,1000) {
            @Override
            public void onTick(long l) {
                timeLeftMilsec = l;
                updateTimer();
            }

            @Override
            public void onFinish() {
                currentIndex = 0;
                countDownTimer.cancel();
                timeLeftMilsec = 301000;
                final boolean[] newGame = {false};
                AlertDialog.Builder alert = new AlertDialog.Builder(Grade_9.this);
                alert.setTitle("Game Over");
                alert.setCancelable(false);
                alert.setMessage("Your Score" + mscore +"points");
                alert.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        save();
                        finish();
                    }
                });

                alert.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                    generateQuestions();
                        newGame[0] = true;
                        save();
                        mscore=0;
                        qn=1;
                        progressBar.setProgress(0);
                        score.setText("Score" + mscore +"/" +questionBank.length);
                        questionnumber.setText(qn + "/" + questionBank.length +"Question");
                        timeLeftMilsec = 301000;
                        countDownTimer.start();
                    }
                });
                alert.show();
            }
        }.start();

    }
    public  void updateTimer() {
        int minutes = (int) timeLeftMilsec / 60000;
        int seconds =  (int) timeLeftMilsec % 60000 / 1000;

        String timeLeftText;

        timeLeftText = " " + minutes;
        timeLeftText += ":";
        if (seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;
        Log.d("timer",timeLeftText);

        Timer.setText(timeLeftText);

    }


}