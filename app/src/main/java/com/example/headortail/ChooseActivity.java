package com.example.headortail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChooseActivity extends AppCompatActivity {

    Button btnBat, btnBall;
    TextView tvTossResult, tvChoosePrompt;
    int totalOvers = 1;

    // Firebase Database reference
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        btnBat = findViewById(R.id.btnBat);
        btnBall = findViewById(R.id.btnBall);
        tvTossResult = findViewById(R.id.tvTossResult);
        tvChoosePrompt = findViewById(R.id.tvChoosePrompt); // ✅ নতুন TextView আইডি লিঙ্ক

        // TossActivity থেকে overs এবং কে জিতেছে সেটা আনো
        totalOvers = getIntent().getIntExtra("totalOvers", 1);
        boolean userWonToss = getIntent().getBooleanExtra("userWonToss", true);

        // Firebase DB initialize
        dbRef = FirebaseDatabase.getInstance().getReference("matches");

        if (userWonToss) {
            // ✅ User won toss
            tvTossResult.setText("🎉 You have won the Toss!");
            btnBat.setVisibility(View.VISIBLE);
            btnBall.setVisibility(View.VISIBLE);
            tvChoosePrompt.setVisibility(View.VISIBLE); // ✅ নির্দেশনা দেখাও

            btnBat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startMatch(true);
                }
            });

            btnBall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startMatch(false);
                }
            });

        } else {
            // ❌ User lost toss → AI chooses randomly
            boolean aiBatFirst = Math.random() < 0.5;
            String aiDecision = aiBatFirst ? "AI chose to bat first." : "AI chose to bowl first.";
            tvTossResult.setText("😢 You have lost the Toss!\n" + aiDecision);

            // Hide buttons and prompt
            btnBat.setVisibility(View.GONE);
            btnBall.setVisibility(View.GONE);
            tvChoosePrompt.setVisibility(View.GONE);

            // Delay and start the match
            tvTossResult.postDelayed(() -> {
                startMatch(!aiBatFirst); // If AI bats first, user bats second
            }, 2000); // delay 2 seconds
        }
    }

    private void startMatch(boolean userBatFirst) {
        String matchId = dbRef.push().getKey(); // Unique ID

        Match match = new Match(matchId, String.valueOf(userBatFirst), String.valueOf(totalOvers));
        dbRef.child(matchId).setValue(match); // Save to Firebase

        Intent intent = new Intent(ChooseActivity.this, ResultActivity.class);
        intent.putExtra("userBatsFirst", userBatFirst);
        intent.putExtra("totalOvers", totalOvers);
        intent.putExtra("matchId", matchId);
        startActivity(intent);
        finish();
    }
}