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

public class Grade_10 extends level {

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
    int grade = 10;
    final int PROGRESS_BAR = (int) Math.ceil(100/questionBank.length);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz4);
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
        {   // This will prompt/notify the user if they get the answer correct
            Toast.makeText(getApplicationContext(),"Correct",Toast.LENGTH_SHORT).show();
            mscore=mscore+1;
            history[currentIndex] = true;
        }
        else
        {   // This will prompt/notify the user if they get the answer correct
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
        levels.put("is_level4_clear", true);
        user.update("levels", levels);
    }

    // This function is used to store in the leaderboard
    public void store(String username, Map scoreList, int finScore){
        fStore.collection("leaderboards")
                .whereEqualTo("username", username)
                .whereEqualTo("grade", "Expert")
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
        scoreList.put("grade", "Expert");
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
            AlertDialog.Builder alert=new AlertDialog.Builder(Grade_10.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_view4, null);
            TextView qHistoryText = (TextView) dialogView.findViewById(R.id.qHistory8);
            TextView scoreText = (TextView) dialogView.findViewById(R.id.scoreText3);

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
                    timeLeftMilsec = 401000;
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
            //Will get random number between 1 and 35
            int currIndex = getRandomNumber(1, 35);
            Log.i("current_index_random",Integer.toString(currIndex));
            if(currIndex == 1 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_31, R.string.question31_A, R.string.question31_B, R.string.question31_C, R.string.question31_D, R.string.answer_31);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 2 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_32, R.string.question_32A, R.string.question_32B, R.string.question_32C, R.string.question_32D, R.string.answer_32);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 3 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_33, R.string.question_33A, R.string.question_33B, R.string.question_33C, R.string.question_33D, R.string.answer_33A);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 4 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_34, R.string.question_34A, R.string.question_34B, R.string.question_34C, R.string.question_34D, R.string.answer_34D);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 5 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_35, R.string.question_35A, R.string.question_35B, R.string.question_35C, R.string.question_35D, R.string.answer_35);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 6 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_36, R.string.question_36A, R.string.question_36B, R.string.question_36C, R.string.question_36D, R.string.answer_36B);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 7 && !usedNumbers.contains(currIndex)){
                questionBank[x-1] = new answerclass(R.string.question_37, R.string.question_37A, R.string.question_37B, R.string.question_37C, R.string.question_37D, R.string.answer_37);
                x++;
                usedNumbers.add(currIndex);
            }
            else if(currIndex == 8 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_38, R.string.question_38A, R.string.question_38B, R.string.question_38C, R.string.question_38D, R.string.answer_38);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 9 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_39, R.string.question_39A, R.string.question_39B, R.string.question_39C, R.string.question_39D, R.string.answer_39C);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 10 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_40, R.string.question_40A, R.string.question_40B, R.string.question_40C, R.string.question_40D, R.string.answer_40);
                usedNumbers.add(currIndex);
                x++;
            }


            else if(currIndex == 11 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_481, R.string.question_481A, R.string.question_481B, R.string.question_481C, R.string.question_481D, R.string.answer_481);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 12 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_482, R.string.question_482A, R.string.question_482B, R.string.question_482C, R.string.question_482D, R.string.answer_482);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 13 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_483, R.string.question_483A, R.string.question_483B, R.string.question_483C, R.string.question_483D, R.string.answer_483);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 14 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_484, R.string.question_484A, R.string.question_484B, R.string.question_484C, R.string.question_484D, R.string.answer_484);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 15 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_485, R.string.question_485A, R.string.question_485B, R.string.question_485C, R.string.question_485D, R.string.answer_485);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 16 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_486, R.string.question_486A, R.string.question_486B, R.string.question_486C, R.string.question_486D, R.string.answer_486);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 17 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_487, R.string.question_487A, R.string.question_487B, R.string.question_487C, R.string.question_487D, R.string.answer_487);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 18 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_488, R.string.question_488A, R.string.question_488B, R.string.question_488C, R.string.question_488D, R.string.answer_488);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 19 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_489, R.string.question_489A, R.string.question_489B, R.string.question_489C, R.string.question_489D, R.string.answer_489);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 20 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_490, R.string.question_490A, R.string.question_490B, R.string.question_490C, R.string.question_490D, R.string.answer_490);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 21 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_491, R.string.question_491A, R.string.question_491B, R.string.question_491C, R.string.question_491D, R.string.answer_491);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 22 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_492, R.string.question_492A, R.string.question_492B, R.string.question_492C, R.string.question_492D, R.string.answer_492);
                usedNumbers.add(currIndex);
                x++;
            }


            else if(currIndex == 23 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_493, R.string.question_493A, R.string.question_493B, R.string.question_493C, R.string.question_493D, R.string.answer_493);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 24 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_494, R.string.question_494A, R.string.question_494B, R.string.question_494C, R.string.question_494D, R.string.answer_494);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 25 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_495, R.string.question_495A, R.string.question_495B, R.string.question_495C, R.string.question_495D, R.string.answer_495);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 26 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_496, R.string.question_496A, R.string.question_496B, R.string.question_496C, R.string.question_496D, R.string.answer_496);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 27 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_497, R.string.question_497A, R.string.question_497B, R.string.question_497C, R.string.question_497D, R.string.answer_497);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 28 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_498, R.string.question_498A, R.string.question_498B, R.string.question_498C, R.string.question_498D, R.string.answer_498);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 29 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_499, R.string.question_499A, R.string.question_499B, R.string.question_499C, R.string.question_499D, R.string.answer_499);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 30 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_500, R.string.question_500A, R.string.question_500B, R.string.question_500C, R.string.question_500D, R.string.answer_500);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 31 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_501, R.string.question_501A, R.string.question_501B, R.string.question_501C, R.string.question_501D, R.string.answer_501);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 32 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_502, R.string.question_502A, R.string.question_502B, R.string.question_502C, R.string.question_502D, R.string.answer_502);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 33 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_503, R.string.question_503A, R.string.question_503B, R.string.question_503C, R.string.question_503D, R.string.answer_503);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 34 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_504, R.string.question_504A, R.string.question_504B, R.string.question_504C, R.string.question_504D, R.string.answer_504);
                usedNumbers.add(currIndex);
                x++;
            }

            else if(currIndex == 35 && !usedNumbers.contains(currIndex)) {
                questionBank[x-1] = new answerclass(R.string.question_505, R.string.question_505A, R.string.question_505B, R.string.question_505C, R.string.question_505D, R.string.answer_505);
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
                AlertDialog.Builder alert = new AlertDialog.Builder(Grade_10.this);
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