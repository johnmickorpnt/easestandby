package com.test.easestandby;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
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

public class Grade_10 extends level {

    private TextView optionA,optionB,optionC,optionD;
    private TextView questionnumber,question,score;
    private TextView chechkout1,checkout2;
    int currentIndex;
    int mscore=0;
    int qn=1;
    ProgressBar progressBar;

    int CurrentQuestion,CurrentOptionA,CurrentOptionB,CurrentOptionC,CurrentOptionD;
    List<Integer> usedNumbers = new ArrayList<Integer>();
    private answerclass[] questionBank = new answerclass[10];

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

        question = findViewById(R.id.question);
        score=findViewById(R.id.score);
        questionnumber=findViewById(R.id.QuestionNumber);

        chechkout1=findViewById(R.id.selectoption);
        checkout2=findViewById(R.id.CorrectAnswer);
        progressBar=findViewById(R.id.progress_bar);

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
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Incorrect",Toast.LENGTH_SHORT).show();
        }




    }

    @SuppressLint("SetTextI18n")
    private void updateQuestion() {
        final boolean[] newGame = {false};
        currentIndex=(currentIndex+1)%questionBank.length;

        if(currentIndex==0)
        {
            generateQuestions();
            AlertDialog.Builder alert=new AlertDialog.Builder(this);
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

    private void save(){
        Map<String, Object> scores = new HashMap<>();
        int finScore = mscore;
        Log.d("tagZ", String.valueOf(mscore));
        DocumentReference documentReference = fStore.collection("users").document(UserID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String username = documentSnapshot.getString("email");
                documentSnapshot.getString("email");
                store(username, scores, finScore);
            }
        });

    }

    public void store(String username, Map scoreList, int finScore){
        scoreList.put("score", finScore);
        scoreList.put("grade", grade);
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

    private void generateQuestions(){
        usedNumbers.clear();
        for(int x = 1; x <= 10;){
            int currIndex = getRandomNumber(1, 10);
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
}