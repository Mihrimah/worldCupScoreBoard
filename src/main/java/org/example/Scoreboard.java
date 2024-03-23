package org.example;

import org.example.exceptions.MatchIsNotStartedException;
import org.example.exceptions.MatchNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scoreboard {
    private final List<String> matches = new ArrayList<>();
    private final Map<String, Score> scores = new HashMap<>();

    public void insertMatch(String homeTeam, String awayTeam) {
        matches.add(homeTeam + "-" + awayTeam);
    }

    public boolean containsMatch(String homeTeam, String awayTeam) {
        return matches.contains(homeTeam + "-" + awayTeam);
    }

    public int matchCount() {
        return matches.size();
    }

    public void startMatch(String homeTeam, String awayTeam) {
        scores.put(homeTeam + "-" + awayTeam, new Score());
    }

    public String getScore(String homeTeam, String awayTeam) {
        int matchIndex = matches.indexOf(homeTeam + "-" + awayTeam);
        if (matchIndex >= 0) {
            Score matchScore = scores.get(homeTeam + "-" + awayTeam);
            if (matchScore == null) {
                throw new MatchIsNotStartedException(homeTeam, awayTeam);
            }
            return matchScore.homeScore + "-" + matchScore.awayScore;
        } else {
            throw new MatchNotFoundException(homeTeam, awayTeam);
        }

    }

    private static class Score {
        int homeScore = 0;
        int awayScore = 0;

        public void increaseHomeScore() {
            homeScore++;
        }

        public void decreaseHomeScore() {
            homeScore--;
        }

        public void increaseAwayScore() {
            awayScore++;
        }

        public void decreaseAwayScore() {
            awayScore--;
        }
    }
}
