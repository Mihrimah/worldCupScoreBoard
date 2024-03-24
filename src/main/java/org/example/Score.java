package org.example;

public class Score {
    private int homeScore = 0;
    private int awayScore = 0;

    public int getHomeScore() {
        return homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public void incrementHomeScore() {
        homeScore++;
    }

    public void incrementAwayScore() {
        awayScore++;
    }

    public void decrementHomeScore() {
        homeScore--;
    }

    public void decrementAwayScore() {
        awayScore--;
    }
}
