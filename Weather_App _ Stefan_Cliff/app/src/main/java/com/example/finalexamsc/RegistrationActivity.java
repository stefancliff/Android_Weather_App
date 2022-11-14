package com.example.finalexamsc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText, passwordEditText, passwordConfirmEditText;
    private Button registrationButton;
    private ProgressBar loadingProgressBar;
    private FirebaseAuth mAuth;
    private TextView alreadyRegisteredInTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        usernameEditText            =          findViewById(R.id.idEditUsername);
        passwordEditText            =          findViewById(R.id.idEditPassword);
        passwordConfirmEditText     =          findViewById(R.id.idEditConfirmPassword);
        registrationButton          =          findViewById(R.id.idButtonRegister);
        loadingProgressBar          =          findViewById(R.id.idProgressBarLoading);
        alreadyRegisteredInTextView =          findViewById(R.id.idTextViewAlreadyRegistered);
        mAuth                       =          FirebaseAuth.getInstance();

        alreadyRegisteredInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // loadingProgressBar.setVisibility(View.VISIBLE);
                String username         = usernameEditText.getText().toString();
                String password         = passwordEditText.getText().toString();
                String confirmPassword  = passwordConfirmEditText.getText().toString();

                if(!password.equals(confirmPassword)){
                    Toast.makeText(RegistrationActivity.this, "Please make sure the passwords match!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(username) && TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmPassword) ){
                    Toast.makeText(RegistrationActivity.this, "All fields must be filled in!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //loadingProgressBar.setVisibility(View.GONE);
                                Toast.makeText(RegistrationActivity.this, "You have successfully registered!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                //loadingProgressBar.setVisibility(View.GONE);
                                Toast.makeText(RegistrationActivity.this, "There was an error in registering...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}