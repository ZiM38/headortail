package com.example.headortail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class TossActivity extends AppCompatActivity {

    Button btnHead, btnTell, btnToss;
    String userChoice = "";
    int totalOvers = 1;

    // ✅ Firebase Database reference
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toss_activity);

        // overs  (OverSelectActivity )
        totalOvers = getIntent().getIntExtra("totalOvers", 1);

        // ✅ Firebase DB reference initialize
        dbRef = FirebaseDatabase.getInstance().getReference("matches");

        btnHead = findViewById(R.id.btnHead);
        btnTell = findViewById(R.id.btnTell);
        btnToss = findViewById(R.id.btnToss);

        btnHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userChoice = "HEAD";
            }
        });

        btnTell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userChoice = "TELL";
            }
        });

        btnToss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userChoice.isEmpty()) {
                    // Randomly toss HEAD or TELL
                    String[] options = {"HEAD", "TELL"};
                    String tossResult = options[new Random().nextInt(2)];

                    // ✅ Save toss result & user choice to Firebase
                    dbRef.child("toss").child("userChoice").setValue(userChoice);
                    dbRef.child("toss").child("tossResult").setValue(tossResult);
                    dbRef.child("toss").child("totalOvers").setValue(totalOvers);

                    if (userChoice.equals(tossResult)) {
                        // ✅ User wins toss → go to ChooseActivity
                        Intent intent = new Intent(TossActivity.this, ChooseActivity.class);
                        intent.putExtra("totalOvers", totalOvers);
                        intent.putExtra("userWonToss", true);  // ✅ Added this line
                        startActivity(intent);
                    } else {
                        // ✅ AI wins toss → directly go to ResultActivity, user will bowl
                        Intent intent = new Intent(TossActivity.this, ResultActivity.class);
                        intent.putExtra("userBatsFirst", false); // AI bats first
                        intent.putExtra("totalOvers", totalOvers);
                        intent.putExtra("userWonToss", false);  // ✅ Added this line
                        startActivity(intent);
                    }
                    finish();
                }
            }
        });
    }
}