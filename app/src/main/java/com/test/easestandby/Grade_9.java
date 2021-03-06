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

public class Grade_9 extends level {

    //Initiating the variables
    private TextView optionA,optionB,optionC,optionD;
    private TextView questionnumber,question,score;
    private TextView chechkout1,checkout2;
    public TextView Timer;
    private CountDownTimer countDownTimer;
    private  long timeLeftMilsec = 900000;
    int currentIndex;
    int mscore=0;
    int qn=1;
    private boolean[] history = new boolean[15];

    // This is needed for the quiz in order to track the correct answer of the user
    ProgressBar progressBar;
    int CurrentQuestion,CurrentOptionA,CurrentOptionB,CurrentOptionC,CurrentOptionD;
    List<Integer> usedNumbers = new ArrayList<Integer>();
    // The constructor for the questions
    private answerclass[] questionBank = new answerclass[15];

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

        //Calling for the startTimer function
        startTimer();
        //The Question of the quiz
        CurrentQuestion=questionBank[currentIndex].getQuestionid();
        question.setText(CurrentQuestion);
        //Choices of the quiz
        CurrentOptionA=questionBank[currentIndex].getOptionA();
        optionA.setText(CurrentOptionA);
        CurrentOptionB=questionBank[currentIndex].getOptionB();
        optionB.setText(CurrentOptionB);
        CurrentOptionC=questionBank[currentIndex].getOptionC();
        optionC.setText(CurrentOptionC);
        CurrentOptionD=questionBank[currentIndex].getOptionD();
        optionD.setText(CurrentOptionD);

        //Checking of the choices in order to update
       optionA.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               checkAnswer(CurrentOptionA);
               updateQuestion();

           }
       });

        //Checking of the choices in order to update
        optionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(CurrentOptionB);
                updateQuestion();


            }
        });

        //Checking of the choices in order to update
        optionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(CurrentOptionC);
                updateQuestion();
            }
        });

        //Checking of the choices in order to update
        optionD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(CurrentOptionD);
                updateQuestion();
            }
        });


    }

    // Is used for checking the answer if it's correct
    private void checkAnswer(int userSelection) {

        int correctanswer=questionBank[currentIndex].getAnswerid();

        chechkout1.setText(userSelection);
        checkout2.setText(correctanswer);

        String m= chechkout1.getText().toString().trim();
        String n=checkout2.getText().toString().trim();

        if(m.equals(n))
        { // This will prompt/notify the user if they get the answer correct
            Toast.makeText(getApplicationContext(),"Correct",Toast.LENGTH_SHORT).show();
            mscore=mscore+1;
            history[currentIndex] = true;
        }
        else
        { // This will prompt/notify the user if they get the answer correct
            Toast.makeText(getApplicationContext(),"Incorrect",Toast.LENGTH_SHORT).show();
            history[currentIndex] = false;
        }
    }

    //Used for storing the score in the firebase
    private void save(){
        Map<String, Object> scores = new HashMap<>();
        int finScore = mscore;
        float pct =  ((float) mscore / 15) * (float) 100;
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

    // This is used to check whether the user met the passing the score in order to proceed to other level
    private void passed(Map<String, Boolean> levels){
        DocumentReference user = fStore.collection("users").document(UserID);
        levels.put("is_level3_clear", true);
        user.update("levels", levels);
    }

    // This function is used to store in the leaderboard
    public void store(String username, Map scoreList, int finScore){
        fStore.collection("leaderboards")
                .whereEqualTo("username", username)
                .whereEqualTo("grade", "Hard")
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

    //It will get stored information of the score,grade,and username from the firebase and will be added in the leaderboards
    public void newScore(String username, Map scoreList, int finScore){
        scoreList.put("score", finScore);
        scoreList.put("grade", "Average");
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

    //Overwrites the score when it is higher than the previous one
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
    //Used to update the questions of the classes
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

            scoreText.setText(String.valueOf(mscore) + "\\15");
            qHistoryText.setText(qHistory);
            alert.setView(dialogView);
            alert.setCancelable(false);

            // It will display the "Back" once answered all the questions of the quiz
            alert.setPositiveButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    save();
                    finish();
                }
            });

            // It wil display the "Try Again" once answered all the questions of the quiz
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

        //Getting the question from the questionBank
        CurrentQuestion=questionBank[currentIndex].getQuestionid();
        question.setText(CurrentQuestion);
        //Getting the choices from the questionBank
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


    // This function is used for generating randomize questions
    private void generateQuestions(){
        usedNumbers.clear();
        //Set the quiz to 15 only questions
        for(int x = 1; x <= 15;){
            //Will get random number between 1 and 30
            int currIndex = getRandomNumber(1,30);
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

            else if(currIndex == 21 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_282, R.string.question282_A, R.string.question282_B, R.string.question282_C, R.string.question282_D, R.string.answer_282);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 22 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_283, R.string.question283_A, R.string.question283_B, R.string.question283_C, R.string.question283_D, R.string.answer_283);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 23 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_284, R.string.question284_A, R.string.question284_B, R.string.question284_C, R.string.question284_D, R.string.answer_284);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 24 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_285, R.string.question285_A, R.string.question285_B, R.string.question285_C, R.string.question285_D, R.string.answer_285);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 25 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_286, R.string.question286_A, R.string.question286_B, R.string.question286_C, R.string.question286_D, R.string.answer_286);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 26 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_287, R.string.question287_A, R.string.question287_B, R.string.question287_C, R.string.question287_D, R.string.answer_287);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 27 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_288, R.string.question288_A, R.string.question288_B, R.string.question288_C, R.string.question288_D, R.string.answer_288);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 28 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_289, R.string.question289_A, R.string.question289_B, R.string.question289_C, R.string.question289_D, R.string.answer_289);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 29 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_290, R.string.question290_A, R.string.question290_B, R.string.question290_C, R.string.question290_D, R.string.answer_290);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 30 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_281, R.string.question281_A, R.string.question281_B, R.string.question281_C, R.string.question281_D, R.string.answer_281);
                usedNumbers.add(currIndex);
                x++;
            }
        }
        Log.i("to_string",toString(usedNumbers));
    }

    //This is the function in order to randomize the number
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
            // Once  the user finish all the questions in the quiz, it will display the score and points on the correct answer
            public void onFinish() {
                currentIndex = 0;
                countDownTimer.cancel();
                timeLeftMilsec = 900000;
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

                //Once the timer runs out and not yet done with the question it will display "Try Again" along with the score
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
                        timeLeftMilsec = 900000;
                        countDownTimer.start();
                    }
                });
                alert.show();
            }
        }.start();

    }

    //This is the function updating for the timer
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