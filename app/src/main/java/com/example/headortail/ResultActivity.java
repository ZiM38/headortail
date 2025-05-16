package com.example.headortail;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Random;

public class ResultActivity extends AppCompatActivity {

    TextView tvScoreBoard, tvDiceResult;
    ImageView diceA, diceB;
    Button[] btnBalls = new Button[7];

    int userScore = 0, aiScore = 0, targetScore = 0;
    int totalOvers = 2;
    int currentBalls = 0;
    boolean isFirstInnings = true;
    boolean isInningsOver = false;
    boolean userBatsFirst = true;

    MediaPlayer tapSound, runSound, outSound;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);

        tvScoreBoard = findViewById(R.id.tvScoreBoard);
        tvDiceResult = findViewById(R.id.tvDiceResult);
        diceA = findViewById(R.id.diceA);
        diceB = findViewById(R.id.diceB);

        btnBalls[0] = findViewById(R.id.btnDot);
        btnBalls[1] = findViewById(R.id.btn1);
        btnBalls[2] = findViewById(R.id.btn2);
        btnBalls[3] = findViewById(R.id.btn3);
        btnBalls[4] = findViewById(R.id.btn4);
        btnBalls[5] = findViewById(R.id.btn5);
        btnBalls[6] = findViewById(R.id.btn6);

        userBatsFirst = getIntent().getBooleanExtra("userBatsFirst", true);
        totalOvers = getIntent().getIntExtra("selectedOvers", 2);

        dbRef = FirebaseDatabase.getInstance().getReference("match");

        tapSound = MediaPlayer.create(this, R.raw.tap_click);
        runSound = MediaPlayer.create(this, R.raw.run_scored);
        outSound = MediaPlayer.create(this, R.raw.player_out);

        for (int i = 0; i < btnBalls.length; i++) {
            int guess = (i == 0) ? 0 : i;
            btnBalls[i].setText(i == 0 ? "." : String.valueOf(guess));
            btnBalls[i].setOnClickListener(v -> {
                playSound(R.raw.tap_click);
                rollDice(guess);
            });
        }

        updateScoreBoard();
    }

    private void rollDice(int userGuess) {
        if (isInningsOver) return;

        Random random = new Random();
        int aiGuess = random.nextInt(7);

        int userDiceRes = getResources().getIdentifier("dice" + userGuess, "drawable", getPackageName());
        int aiDiceRes = getResources().getIdentifier("dice" + aiGuess, "drawable", getPackageName());

        diceA.setImageResource(userDiceRes);
        diceB.setImageResource(aiDiceRes);

        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        diceA.startAnimation(rotate);
        diceB.startAnimation(rotate);

        int displayNumber = (userBatsFirst == isFirstInnings) ? userGuess : aiGuess;
        tvDiceResult.setText(String.valueOf(displayNumber));

        boolean isOut = (userGuess == aiGuess);
        if (userGuess == 0 && aiGuess == 0) isOut = true;

        if (isFirstInnings) {
            if (userBatsFirst) {
                if (isOut) {
                    playSound(R.raw.player_out);
                    showOutMessage();
                    targetScore = userScore + 1;
                    dbRef.child("userScore").setValue(userScore);
                    dbRef.child("targetScore").setValue(targetScore);
                    showInningsOver("OUT!\nTarget for AI: " + targetScore);
                    return;
                } else {
                    if (userGuess != 0) {
                        playSound(R.raw.run_scored);
                        userScore += userGuess;
                    }
                    currentBalls++;
                }
            } else {
                if (isOut) {
                    playSound(R.raw.player_out);
                    showOutMessage();
                    targetScore = aiScore + 1;
                    dbRef.child("aiScore").setValue(aiScore);
                    dbRef.child("targetScore").setValue(targetScore);
                    showInningsOver("OUT!\nTarget for You: " + targetScore);
                    return;
                } else {
                    if (aiGuess != 0) {
                        playSound(R.raw.run_scored);
                        aiScore += aiGuess;
                    }
                    currentBalls++;
                }
            }

            if (currentBalls >= totalOvers * 6) {
                targetScore = userBatsFirst ? userScore + 1 : aiScore + 1;
                dbRef.child("targetScore").setValue(targetScore);
                showInningsOver("Innings Over\nTarget: " + targetScore);
                return;
            }

        } else {
            if (userBatsFirst) {
                if (isOut) {
                    playSound(R.raw.player_out);
                    showOutMessage();
                    dbRef.child("finalResult").setValue("User Wins");
                    endGame("Team A");
                    return;
                } else {
                    if (aiGuess != 0) {
                        playSound(R.raw.run_scored);
                        aiScore += aiGuess;
                    }
                    currentBalls++;
                    if (aiScore >= targetScore) {
                        dbRef.child("finalResult").setValue("AI Wins");
                        endGame("Team B");
                        return;
                    }
                }
            } else {
                if (isOut) {
                    playSound(R.raw.player_out);
                    showOutMessage();
                    dbRef.child("finalResult").setValue("AI Wins");
                    endGame("Team B");
                    return;
                } else {
                    if (userGuess != 0) {
                        playSound(R.raw.run_scored);
                        userScore += userGuess;
                    }
                    currentBalls++;
                    if (userScore >= targetScore) {
                        dbRef.child("finalResult").setValue("User Wins");
                        endGame("Team A");
                        return;
                    }
                }
            }

            if (currentBalls >= totalOvers * 6) {
                if (userBatsFirst) {
                    dbRef.child("finalResult").setValue(aiScore >= targetScore ? "AI Wins" : "User Wins");
                    endGame(aiScore >= targetScore ? "Team B" : "Team A");
                } else {
                    dbRef.child("finalResult").setValue(userScore >= targetScore ? "User Wins" : "AI Wins");
                    endGame(userScore >= targetScore ? "Team A" : "Team B");
                }
                return;
            }
        }

        updateScoreBoard();
    }

    private void showInningsOver(String message) {
        isInningsOver = true;
        tvScoreBoard.setText(message);
        new Handler().postDelayed(() -> {
            isFirstInnings = false;
            isInningsOver = false;
            currentBalls = 0;
            updateScoreBoard();
        }, 2000); // 2 second delay
    }

    private void updateScoreBoard() {
        int overs = currentBalls / 6;
        int balls = currentBalls % 6;
        String overDisplay = overs + "." + balls;

        String status;
        if (isFirstInnings) {
            if (userBatsFirst) {
                status = "Team A Batting\nRuns: " + userScore + "\nOvers: " + overDisplay;
            } else {
                status = "Team B Batting\nRuns: " + aiScore + "\nOvers: " + overDisplay;
            }
        } else {
            if (userBatsFirst) {
                status = "Team B Batting\nRuns: " + aiScore + "\nTarget: " + targetScore + "\nOvers: " + overDisplay;
            } else {
                status = "Team A Batting\nRuns: " + userScore + "\nTarget: " + targetScore + "\nOvers: " + overDisplay;
            }
        }
        tvScoreBoard.setText(status);
    }

    private void endGame(String winner) {
        isInningsOver = true;
        Intent intent = new Intent(this, FinalResultActivity.class);
        intent.putExtra("winner", winner);
        startActivity(intent);
        finish();
    }

    private void showOutMessage() {
        tvDiceResult.setText("OUT!");
    }

    private void playSound(int soundResId) {
        MediaPlayer mp = MediaPlayer.create(this, soundResId);
        mp.start();
        mp.setOnCompletionListener(MediaPlayer::release);
    }
}