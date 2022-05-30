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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Grade_8 extends level {

    private TextView optionA,optionB,optionC,optionD;
    private TextView questionnumber,question,score;
    private TextView chechkout1,checkout2;
    public TextView Timer;
    private CountDownTimer countDownTimer;
    private  long timeLeftMilsec = 600000;
    int currentIndex;
    int mscore=0;
    int qn=1;
    private boolean[] history = new boolean[10];

    ProgressBar progressBar;
    int CurrentQuestion,CurrentOptionA,CurrentOptionB,CurrentOptionC,CurrentOptionD;
    List<Integer> usedNumbers = new ArrayList<Integer>();
    private answerclass[] questionBank = new answerclass[10];

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    String UserID;
    StorageReference storageReference;
    int grade = 8;

    final int PROGRESS_BAR = (int) Math.ceil(100/questionBank.length);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz2);
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
        float pct =  ((float) mscore / 10) * (float) 100;
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
        levels.put("is_level2_clear", true);
        user.update("levels", levels);
    }

    public void store(String username, Map scoreList, int finScore){
        fStore.collection("leaderboards")
                .whereEqualTo("username", username)
                .whereEqualTo("grade", "Medium")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String id = new String();
                            task.getResult().size();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                id = document.getId();
                            }
                            Log.d("SHESH", id);

                            if(!id.isEmpty()) overwrite(id, scoreList, finScore);
                            else newScore(username, scoreList, finScore);
                        } else {
                            Log.d("SHESH", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void newScore(String username, Map scoreList, int finScore){
        scoreList.put("score", finScore);
        scoreList.put("grade", "Medium");
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

    public void overwrite(String id, Map scoreList, int finScore){
        Log.d("SHESH", "overwrite: " + id);
        DocumentReference documentReference = fStore.collection("leaderboards").document(id);
        documentReference.update("score", finScore)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("SHESH", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("SHESH", "Error updating document", e);
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
            AlertDialog.Builder alert=new AlertDialog.Builder(Grade_8.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_view2, null);
            TextView qHistoryText = (TextView) dialogView.findViewById(R.id.qHistory4);
            TextView scoreText = (TextView) dialogView.findViewById(R.id.scoreText2);

            for(boolean v: history){
                Log.v("curr_index", Boolean.toString(v));
                qHistory += ((qNum == 0) ? ("\n") : ("")) + ((v) ? ("Correct") : ("Incorrect")) + "\n";
                qNum++;
            }

            scoreText.setText(String.valueOf(mscore) + "\\10");
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
                    timeLeftMilsec = 121000;
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
        for(int x = 1; x <= 10;){
            int currIndex = getRandomNumber(1, 25);
            Log.i("current_index_random",Integer.toString(currIndex));

            if(currIndex == 1 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_11, R.string.question_11A, R.string.question_11B, R.string.question_11C, R.string.question_11D, R.string.answer_11A);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 2 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_12, R.string.question_12A, R.string.question_12B, R.string.question_12C, R.string.question_12D, R.string.answer_12B);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 3 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_13, R.string.question_13A, R.string.question_13B, R.string.question_13C, R.string.question_13D, R.string.answer_13B);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 4 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_14, R.string.question_14A, R.string.question_14B, R.string.question_14C, R.string.question_14D, R.string.answer_14A);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 5 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_15, R.string.question_15A, R.string.question_15B, R.string.question_15C, R.string.question_15D, R.string.answer_15);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 6 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_16, R.string.question_16A, R.string.question_16B, R.string.question_16C, R.string.question_16D, R.string.answer_16A);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 7 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_17, R.string.question_17A, R.string.question_17B, R.string.question_17C, R.string.question_17D, R.string.answer_17);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 8 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_18, R.string.question_18A, R.string.question_18B, R.string.question_18C, R.string.question_18D, R.string.answer_18);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 9 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_19, R.string.question_19A, R.string.question_19B, R.string.question_19C, R.string.question_19D, R.string.answer_19);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 10 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_20, R.string.question_20A, R.string.question_20B, R.string.question_20C, R.string.question_20D, R.string.answer_20);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 11 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_220, R.string.question_220A, R.string.question_220B, R.string.question_220C, R.string.question_22D, R.string.answer_220);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 12 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_230, R.string.question_230A, R.string.question_230B, R.string.question_230C, R.string.question_23D, R.string.answer_230);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 13 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_240, R.string.question_240A, R.string.question_240B, R.string.question_240C, R.string.question_24D, R.string.answer_240);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 14 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_250, R.string.question_250A, R.string.question_250B, R.string.question_250C, R.string.question_25D, R.string.answer_250);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 15 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_260, R.string.question_260A, R.string.question_260B, R.string.question_260C, R.string.question_260D, R.string.answer_260);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 16 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_261, R.string.question_261A, R.string.question_261B, R.string.question_261C, R.string.question_261D, R.string.answer_261);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 17 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_262, R.string.question_262A, R.string.question_262B, R.string.question_262C, R.string.question_262D, R.string.answer_262);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 18 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_263, R.string.question_263A, R.string.question_263B, R.string.question_263C, R.string.question_263D, R.string.answer_263);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 19 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_264, R.string.question_264A, R.string.question_264B, R.string.question_264C, R.string.question_264D, R.string.answer_264);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 20 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_265, R.string.question_265A, R.string.question_265B, R.string.question_265C, R.string.question_265D, R.string.answer_265);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 21 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_266, R.string.question_266A, R.string.question_266B, R.string.question_266C, R.string.question_266D, R.string.answer_266);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 22 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_267, R.string.question_267A, R.string.question_267B, R.string.question_267C, R.string.question_267D, R.string.answer_267);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 23 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_268, R.string.question_268A, R.string.question_268B, R.string.question_268C, R.string.question_268D, R.string.answer_268);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 24 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_269, R.string.question_269A, R.string.question_269B, R.string.question_269C, R.string.question_269D, R.string.answer_269);
                usedNumbers.add(currIndex);
                x++;
            }
            else if(currIndex == 25 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_2691, R.string.question_2691A, R.string.question_2691B, R.string.question_2691C, R.string.question_2691D, R.string.answer_2691);
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
                timeLeftMilsec = 600000;
                final boolean[] newGame = {false};
                AlertDialog.Builder alert = new AlertDialog.Builder(Grade_8.this);
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
                        timeLeftMilsec = 600000;
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