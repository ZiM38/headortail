package com.example.headortail;

public class Match {
    private String matchId;
    private String player1;
    private String player2;

    public Match(String matchId, String player1, String player2) {
        this.matchId = matchId;
        this.player1 = player1;
        this.player2 = player2;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getPlayer1() {
        return player1;
    }

    public String getPlayer2() {
        return player2;
    }
}