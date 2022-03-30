package com.test.easestandby;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.security.KeyStore;

public class profile extends AppCompatActivity {

    private static final int GALLERY_INTENT_CODE = 1023 ;
    TextView ProfileName, ProfileSurname, ProfileEmail, ProfileNumber,verifytext;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String UserID;
    Button verifybtn, changepassword, editProfile;
    ImageView Profilepic;
    FirebaseUser user;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ProfileName = findViewById(R.id.ProfileName);
        ProfileSurname = findViewById(R.id.ProfileSurname);
        ProfileEmail = findViewById(R.id.ProfileEmail);
        ProfileNumber = findViewById(R.id.ProfileNumber);
        changepassword = findViewById(R.id.changepassword);

        Profilepic = findViewById(R.id.Profilepic);
        editProfile = findViewById(R.id.editProfile);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();



        StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(Profilepic);
            }
        });

        verifybtn = findViewById(R.id.verifybtn);
        verifytext = findViewById(R.id.verifytext);

        UserID = fAuth.getCurrentUser().getUid();
        user = fAuth.getCurrentUser();

        if (!user.isEmailVerified()) {
            verifybtn.setVisibility(View.VISIBLE);
            verifytext.setVisibility(View.VISIBLE);

            verifybtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(v.getContext(),"Verification Email Has been Sent.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag","Verification failed" + e.getMessage());
                        }
                    });
                }
            });
    }

        DocumentReference documentReference = fStore.collection("users").document(UserID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e){
                        if (documentSnapshot.exists()){
                            ProfileName.setText(documentSnapshot.getString("fname"));
                            ProfileSurname.setText(documentSnapshot.getString("surname"));
                            ProfileEmail.setText(documentSnapshot.getString("email"));
                            ProfileNumber.setText(documentSnapshot.getString("number"));
                        }
                        else{
                            Log.d("tag","Document do not exists");
                        }
                    }
                });




        changepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText changepassword = new EditText(v.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Change Password?");
                passwordResetDialog.setMessage("Enter your new Password?");
                passwordResetDialog.setView(changepassword);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract email and reset password
                        String newpassword = changepassword.getText().toString();
                        user.updatePassword(newpassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(profile.this, "Password Changed Successfuly!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(profile.this, "Password Change Failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close the dialog
                    }
                });
                passwordResetDialog.create().show();
            }
        });
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //opens gallery

                Intent i = new Intent(v.getContext(),EditProfile.class);
                i.putExtra("fname", ProfileName.getText().toString());
                i.putExtra("surname",ProfileSurname.getText().toString());
                i.putExtra("email", ProfileEmail.getText().toString());
                i.putExtra("number",ProfileNumber.getText().toString());
                startActivity(i);

            }
        });


            }

    public void logout(android.view.View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),login.class));
        finish();
    }
}