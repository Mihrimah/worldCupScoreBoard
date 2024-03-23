package org.example;

import org.example.exceptions.DuplicateMatchInsertException;
import org.example.exceptions.MatchNotStartedException;
import org.example.exceptions.MatchNotFoundException;
import org.example.exceptions.OngoingMatchStartException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scoreboard {
    private final List<String> matches = new ArrayList<>();
    private final Map<String, Score> scores = new HashMap<>();

    public void insertMatch(String homeTeam, String awayTeam) {
        if (matches.contains(homeTeam + "-" + awayTeam) || matches.contains(awayTeam + "-" + homeTeam)) {
            throw new DuplicateMatchInsertException(homeTeam, awayTeam);
        }
        matches.add(homeTeam + "-" + awayTeam);
    }

    public boolean containsMatch(String homeTeam, String awayTeam) {
        return matches.contains(homeTeam + "-" + awayTeam);
    }

    public int matchCount() {
        return matches.size();
    }

    public void startMatch(String homeTeam, String awayTeam) {
        if (scores.containsKey(homeTeam + "-" + awayTeam) || scores.containsKey(awayTeam + "-" + homeTeam)){
            throw new OngoingMatchStartException(homeTeam, awayTeam);
        }
        scores.put(homeTeam + "-" + awayTeam, new Score());
    }

    public String getScore(String homeTeam, String awayTeam) {
        int matchIndex = matches.indexOf(homeTeam + "-" + awayTeam);
        if (matchIndex >= 0) {
            Score matchScore = scores.get(homeTeam + "-" + awayTeam);
            if (matchScore == null) {
                throw new MatchNotStartedException(homeTeam, awayTeam);
            }
            return homeTeam + " " + matchScore.homeScore + " - " + awayTeam + " " + matchScore.awayScore;
        } else {
            throw new MatchNotFoundException(homeTeam, awayTeam);
        }

    }

    public void updateScore(String homeTeam, String awayTeam, TeamType teamType) {
        int matchIndex = matches.indexOf(homeTeam + "-" + awayTeam);
        if (matchIndex >= 0) {
            Score matchScore = scores.get(homeTeam + "-" + awayTeam);
            if (matchScore == null) {
                throw new MatchNotStartedException(homeTeam, awayTeam);
            }
            switch (teamType) {
                case homeTeam -> matchScore.incrementHomeScore();
                case awayTeam -> matchScore.incrementAwayScore();
            }
        } else {
            throw new MatchNotFoundException(homeTeam, awayTeam);
        }
    }

    private static class Score {
        int homeScore = 0;
        int awayScore = 0;

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
}
