package com.e.journal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.internal.RegisterListenerMethod;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class createAccount extends AppCompatActivity {
    private FirebaseAuth firebaseauth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //FireStore Connection

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    private EditText emailEdit;
    private EditText passwordEdit;
    private EditText usernameEdit;
    private ProgressBar progressBar;
    private Button createAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseauth = FirebaseAuth.getInstance();

        createAccount = findViewById(R.id.Register);
        emailEdit = findViewById(R.id.emailAccount);
        passwordEdit = findViewById(R.id.passwordAccount);
        progressBar = findViewById(R.id.CreateAccount);
        usernameEdit = findViewById(R.id.Username);

authStateListener = new FirebaseAuth.AuthStateListener() {
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        currentUser = firebaseAuth.getCurrentUser();

        if(currentUser !=  null){
            //user is already logged
        } else{
            //nouser
        }
    }
};

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           if(!TextUtils.isEmpty(emailEdit.getText().toString())
           &&!TextUtils.isEmpty(passwordEdit.getText().toString())
           && !TextUtils.isEmpty(usernameEdit.getText().toString())){
               String email = emailEdit.getText().toString().trim();
               String password = passwordEdit.getText().toString().trim();
               String username = usernameEdit.getText().toString().trim();


               createEmailUserAccount(email, password, username);

                } else{
               Toast.makeText(createAccount.this,
               "Empty text fields",Toast.LENGTH_LONG).show();
           }
            }
        });

    }

    private void createEmailUserAccount(String email, String password, final String username){
      if (!TextUtils.isEmpty(email)
      &&  !TextUtils.isEmpty(password)
      &&  !TextUtils.isEmpty(username)){
          progressBar.setVisibility(View.VISIBLE);

          firebaseauth.createUserWithEmailAndPassword(email, password)
                  .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                      @Override
                      public void onComplete(@NonNull Task<AuthResult> task) {
                          if(task.isSuccessful()){
                              currentUser = firebaseauth.getCurrentUser();
                              assert currentUser != null;
                              final String currentUserId = currentUser.getUid();

                              Map<String, String> userObj = new HashMap<>();
                              userObj.put("userId", currentUserId);
                              userObj.put("username", username);
                              collectionReference.add(userObj)
                                      .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                          @Override
                                          public void onSuccess(DocumentReference documentReference) {
                                              documentReference.get()
                                                      .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                          @Override

                                                          public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                              if((task.getResult()).exists()){
                                                                  progressBar.setVisibility(View.INVISIBLE);
                                                                  String name = task.getResult()
                                                                          .getString("username");
                                                                  Intent intent = new Intent(createAccount.this,
                                                                          PostJournalActivity.class);
                                                                  intent.putExtra("username", name);
                                                                  intent.putExtra("userId", currentUserId);
                                                                  startActivity(intent);

                                                              }
                                                          }
                                                      });
                                          }
                                      })
                                      .addOnFailureListener(new OnFailureListener() {
                                          @Override
                                          public void onFailure(@NonNull Exception e) {

                                          }
                                      });

                          }else{

                          }
                      }
                  })
                  .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {

                      }
                  });
      }else{

      }
    }

    @Override
    protected void onStart() {
        super.onStart();

         currentUser = firebaseauth.getCurrentUser();
        firebaseauth.addAuthStateListener(authStateListener);
    }
}
