package com.test.easestandby;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.grpc.Context;

public class EditProfile extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText editName, editSurname, editEmail, editNumber;
    ImageView editPic;
    Button Save;
    Uri imageuri;


    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    StorageReference storageReference;

    private Button GoBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);



        Intent data = getIntent();
        final String fname = data.getStringExtra("fname");
        String surname = data.getStringExtra("surname");
        String email = data.getStringExtra("email");
        String number = data.getStringExtra("number");

        editName = findViewById(R.id.editName);
        editSurname = findViewById(R.id.editSurname);
        editEmail = findViewById(R.id.editEmail);
        editNumber = findViewById(R.id.editNumber);
        editPic = findViewById(R.id.editPic);
        Save = findViewById(R.id.Save);


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        GoBack = findViewById(R.id.GoBack);


        ActivityResultLauncher<String> mgetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>(){
                public void onActivityResult(Uri result){

                    if (result != null){
                        editPic.setImageURI(result);
                        imageuri = result;
                    }
                }
            });



        StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {



            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(editPic);
            }
        });

        //Go Back
        GoBack = findViewById(R.id.GoBack);
        GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        editPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgetContent.launch("image/*");
            }
        });



        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editName.getText().toString().isEmpty() || editSurname.getText().toString().isEmpty() || editEmail.getText().toString().isEmpty() || editNumber.getText().toString().isEmpty())
                {
                    Toast.makeText(EditProfile.this, "Fields can not be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String email = editEmail.getText().toString();
                user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference documentReference =fStore.collection("users").document(user.getUid());
                        Map<String, Object> edited = new HashMap<>();
                        edited.put("email", email);
                        edited.put("fname", editName.getText().toString());
                        edited.put("surname", editSurname.getText().toString());
                        edited.put("number", editNumber.getText().toString());
                        documentReference.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfile.this, "Profile Changed Succesfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),profile.class));
                                finish();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                uploadImagetoFirebase();
            }
        });


        editName.setText(fname);
        editSurname.setText(surname);
        editEmail.setText(email);
        editNumber.setText(number);

        Log.d(TAG, "onCreate" + fname + " " + surname + " " + email + " " + number);

    }


    private void uploadImagetoFirebase() {
        //upload image to firebase
        if (imageuri != null){
            StorageReference fileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
            fileRef.putFile(imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Picasso.get().load(imageuri).into(editPic);
                    if (task.isSuccessful()){
                        Toast.makeText(EditProfile.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(EditProfile.this, "Please select a proper image", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }



}