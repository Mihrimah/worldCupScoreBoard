package org.example;

/**
 * Represents the score of a match.
 * The score is represented by two integers: homeScore and awayScore.
 */
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
        if (this.homeScore > 0) {
            this.homeScore--;
        }
    }

    public void decrementAwayScore() {
        if (this.awayScore > 0) {
            this.awayScore--;
        }
    }

}
