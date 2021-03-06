package com.test.easestandby;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Grade_7 extends level {

    // Initiating the variables
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

    private String docId;


    //This is needed for the quiz in order to track the correct answer of the user
    ProgressBar progressBar;

    int CurrentQuestion,CurrentOptionA,CurrentOptionB,CurrentOptionC,CurrentOptionD;
    List<Integer> usedNumbers = new ArrayList<Integer>();

    // The constructor for the questions
    private answerclass[] questionBank = new answerclass[10];
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    String UserID;
    StorageReference storageReference;
    int grade = 7;


    final int PROGRESS_BAR = (int) Math.ceil(100/questionBank.length);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        generateQuestions();
        optionA=findViewById(R.id.optionA);
        optionB=findViewById(R.id.optionB);
        optionC=findViewById(R.id.optionC);
        optionD=findViewById(R.id.optionD);

        Timer = findViewById(R.id.TimerTextView);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        UserID = fAuth.getCurrentUser().getUid();
        user = fAuth.getCurrentUser();

        question = findViewById(R.id.question);
        score=findViewById(R.id.score);
        questionnumber=findViewById(R.id.QuestionNumber);
        chechkout1=findViewById(R.id.selectoption);
        checkout2=findViewById(R.id.CorrectAnswer);
        progressBar=findViewById(R.id.progress_bar);

        // Calling for the startTimer function
        startTimer();
        //The Question of the quiz
        CurrentQuestion=questionBank[currentIndex].getQuestionid();
        question.setText(CurrentQuestion);
        //Choices of the Quiz
        CurrentOptionA=questionBank[currentIndex].getOptionA();
        optionA.setText(CurrentOptionA);
        CurrentOptionB=questionBank[currentIndex].getOptionB();
        optionB.setText(CurrentOptionB);
        CurrentOptionC=questionBank[currentIndex].getOptionC();
        optionC.setText(CurrentOptionC);
        CurrentOptionD=questionBank[currentIndex].getOptionD();
        optionD.setText(CurrentOptionD);

        // Checking of the choices in order to update
        optionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(CurrentOptionA);
                updateQuestion();
            }
        });

        // Checking of the choices in order to update
        optionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(CurrentOptionB);
                updateQuestion();
            }
        });

        // Checking of the choices in order to update
        optionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(CurrentOptionC);
                updateQuestion();
            }
        });

        // Checking of the choices in order to update
        optionD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(CurrentOptionD);
                updateQuestion();
            }
        });
    }

    // This function is use for checking the answer if it's correct
    private void checkAnswer(int userSelection) {
        int correctanswer=questionBank[currentIndex].getAnswerid();

        chechkout1.setText(userSelection);
        checkout2.setText(correctanswer);

        String m= chechkout1.getText().toString().trim();
        String n= checkout2.getText().toString().trim();

        if(m.equals(n))
        {   // This will prompt/notify the user if they get the answer correct
            Toast.makeText(getApplicationContext(),"Correct",Toast.LENGTH_SHORT).show();
            mscore=mscore+1;
            history[currentIndex] = true;
        }
        else
        {   // This will prompt/notify the user if they get the answer incorrect
            Toast.makeText(getApplicationContext(),"Incorrect",Toast.LENGTH_SHORT).show();
            history[currentIndex] = false;
        }
    }

    //  Used for storing the score in the firebase
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

    //  This is to check whether the user met the passing score in order to proceed to the other level
    private void passed(Map<String, Boolean> levels){
        DocumentReference user = fStore.collection("users").document(UserID);
        levels.put("is_level1_clear", true);
        user.update("levels", levels);
    }

    // This function is used to store in the leaderboard
    public void store(String username, Map scoreList, int finScore){
        fStore.collection("leaderboards")
                .whereEqualTo("username", username)
                .whereEqualTo("grade", "Easy")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String id = new String();
                            String oldScore = new String();
                            task.getResult().size();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                id = document.getId();
                                oldScore = document.get("score").toString();
                            }

                            if(!id.isEmpty() && Integer.parseInt(oldScore) < finScore) overwrite(id, scoreList, finScore);
                            else if (id.isEmpty() && oldScore.isEmpty()) newScore(username, scoreList, finScore);
                        } else {
                            Log.d("SHESH", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    // It will get stored information of  score,grade and username  from the firebase and will be added in the leaderboards
    public void newScore(String username, Map scoreList, int finScore){
        scoreList.put("score", finScore);
        scoreList.put("grade", "Easy");
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

    //  overwrites the score when it is higher than the previous one
    public void overwrite(String id, Map scoreList, int finScore){

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

    //Used to update the questions of the classes
    private void updateQuestion() {
        final boolean[] newGame = {false};
        currentIndex=(currentIndex+1)%questionBank.length;
        String qHistory = "";
        int qNum = 0;
        if(currentIndex==0)
        {
            generateQuestions();
            countDownTimer.cancel();
            AlertDialog.Builder alert=new AlertDialog.Builder(Grade_7.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_view, null);
            TextView qHistoryText = (TextView) dialogView.findViewById(R.id.qHistory2);
            TextView scoreText = (TextView) dialogView.findViewById(R.id.scoreText);
            for(boolean v: history){
                Log.v("curr_index", Boolean.toString(v));
                qHistory += ((qNum == 0) ? ("\n") : ("")) + ((v) ? (" Correct") : (" Incorrect")) + "\n";
                qNum++;
            }
            scoreText.setText(String.valueOf(mscore) + "\\10");
            qHistoryText.setText(qHistory);
            alert.setView(dialogView);
            alert.setCancelable(false);
            Log.v("curr_index", qHistory);

            //It will display the "Back" once answered all the questions of the quiz
            alert.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    save();
                    finish();
                }
            });

            //It will display the "Try Again" once answered all the questions of the quiz
            alert.setNegativeButton("Try Again",  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newGame[0] = true;
                    save();
                    mscore=0;
                    qn=1;
                    progressBar.setProgress(0);
                    score.setText("Score" + mscore +"/" +questionBank.length);
                    questionnumber.setText(qn + "/" + questionBank.length +"Question");
                    timeLeftMilsec = 60000;
                    countDownTimer.start();
                }
            });
            alert.show();
        }

        // Getting the question from the questionBank
        CurrentQuestion=questionBank[currentIndex].getQuestionid();
        question.setText(CurrentQuestion);

        //Getting  the choices from the questionBank
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

    // This  function is used for generating randomize questions
    private void generateQuestions(){
        usedNumbers.clear();
        for(int x = 1; x <= 10;){ //   Set the quiz to 10 only questions
            int currIndex = getRandomNumber(1,20); // Will get random number between 1 and 20.
            Log.i("current_index_random",Integer.toString(currIndex));
            if(currIndex == 1 && !usedNumbers.contains(currIndex) ){
                questionBank[x-1] = new answerclass (R.string.question_1, R.string.question1_A, R.string.question1_B, R.string.question1_C, R.string.question1_D, R.string.answer_1);
                x++;
                usedNumbers.add(currIndex);

            }
            else if(currIndex == 2 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_2, R.string.question_2A, R.string.question_2B, R.string.question_2C, R.string.question_2D, R.string.answer_2);
                x++;
                usedNumbers.add(currIndex);


            }
            else if(currIndex == 3 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_3, R.string.question_3A, R.string.question_3B, R.string.question_3C, R.string.question_3D, R.string.answer_3);
                x++;
                usedNumbers.add(currIndex);



            }
            else if(currIndex == 4 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_4, R.string.question_4A, R.string.question_4B, R.string.question_4C, R.string.question_4D, R.string.answer_4);
                x++;
                usedNumbers.add(currIndex);


            }
            else if(currIndex == 5 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_5, R.string.question_5A, R.string.question_5B, R.string.question_5C, R.string.question_5D, R.string.answer_5);
                x++;
                usedNumbers.add(currIndex);

            }
            else if(currIndex == 6 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_6, R.string.question_6A, R.string.question_6B, R.string.question_6C, R.string.question_6D, R.string.answer_6);
                x++;
                usedNumbers.add(currIndex);



            }
            else if(currIndex == 7 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_7, R.string.question_7A, R.string.question_7B, R.string.question_7C, R.string.question_7D, R.string.answer_7);
                x++;
                usedNumbers.add(currIndex);



            }
            else if(currIndex == 8 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_8, R.string.question_8A, R.string.question_8B, R.string.question_8C, R.string.question_8D, R.string.answer_8);
                usedNumbers.add(currIndex);
                x++;

            }

            else if(currIndex == 9 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_9, R.string.question_9A, R.string.question_9B, R.string.question_9C, R.string.question_9D, R.string.answer_9);
                usedNumbers.add(currIndex);
                x++;

            }

            else if(currIndex == 10 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10, R.string.question_10A, R.string.question_10B, R.string.question_10C, R.string.question_10D, R.string.answer_10);
                usedNumbers.add(currIndex);
                x++;

            }

            else if(currIndex == 11 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10_1, R.string.question_10A_1, R.string.question_10B_1, R.string.question_10C_1, R.string.question_10D_1, R.string.answer_10_1);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 12 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10_2, R.string.question_10A_2, R.string.question_10B_2, R.string.question_10C_2, R.string.question_10D_2, R.string.answer_10_2);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 13 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10_3, R.string.question_10A_3, R.string.question_10B_3, R.string.question_10C_3, R.string.question_10D_3, R.string.answer_10_3);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 14 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10_4, R.string.question_10A_4, R.string.question_10B_4, R.string.question_10C_4, R.string.question_10D_4, R.string.answer_10_4);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 15 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10_5, R.string.question_10A_5, R.string.question_10B_5, R.string.question_10C_5, R.string.question_10D_5, R.string.answer_10_5);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 16 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10_6, R.string.question_10A_6, R.string.question_10B_6, R.string.question_10C_6, R.string.question_10D_6, R.string.answer_10_6);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 17 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10_7, R.string.question_10A_7, R.string.question_10B_7, R.string.question_10C_7, R.string.question_10D_7, R.string.answer_10_7);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 18 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10_8, R.string.question_10A_8, R.string.question_10B_8, R.string.question_10C_8, R.string.question_10D_8, R.string.answer_10_8);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 19 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10_9, R.string.question_10A_9, R.string.question_10B_9, R.string.question_10C_9, R.string.question_10D_9, R.string.answer_10_9);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 20 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_10_10, R.string.question_10A_10, R.string.question_10B_10, R.string.question_10C_10, R.string.question_10D_10, R.string.answer_10_10);
                usedNumbers.add(currIndex);
                x++;
            }
        }
        Log.i("to_string",toString(usedNumbers));
    }

    // This is the function in order to randomize the number
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
    public void startTimer() { // Function will automatically start once the quiz start
        countDownTimer = new CountDownTimer(timeLeftMilsec,1000) { // 1 sec countdown interval
            @Override
            public void onTick(long l) { // The clock automatically starts and ticks down with an interval of 1 sec
                timeLeftMilsec = l;
                updateTimer();
            }

            @Override
            // Once the user finish all the questions in the quiz, it will display the score and points on the correct answer
            public void onFinish() {
                currentIndex = 0;
                countDownTimer.cancel();
                timeLeftMilsec = 610000;
                final boolean[] newGame = {false};
                AlertDialog.Builder alert = new AlertDialog.Builder(Grade_7.this);
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

                //Once the timer runs out and not yet done with the question it will display "Try Again" along with the score
                alert.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // A function that restarts the quiz application
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

//                alert.setNegativeButton("Try Again", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
////                    generateQuestions();
//                        newGame[0] = true;
//                        save();
//                        mscore=0;
//                        qn=1;
//                        progressBar.setProgress(0);
//                        score.setText("Score" + mscore +"/" +questionBank.length);
//                        questionnumber.setText(qn + "/" + questionBank.length +"Question");
//                        timeLeftMilsec = 600000;
//                        countDownTimer.start();
//                    }
//                });
                alert.show();
            }
        }.start();

    }
    // This is the function to update the timer
    public void updateTimer() {
        int minutes = (int) timeLeftMilsec / 60000;
        int seconds =  (int) timeLeftMilsec % 60000 / 1000;

        String timeLeftText;

        timeLeftText = " " + minutes;
        timeLeftText += ":";
        if (seconds < 10) timeLeftText += "0";
        timeLeftText += seconds;
//        Log.d("timer",timeLeftText);

        Timer.setText(timeLeftText);

    }

}
