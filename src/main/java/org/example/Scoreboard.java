package org.example;

import org.example.exceptions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Scoreboard {
    private final Map<String, Match> matches = new ConcurrentHashMap<>();

    public synchronized void startMatch(String homeTeam, String awayTeam) {
        checkTeams(homeTeam, awayTeam);
        if (containsMatch(homeTeam, awayTeam)) {
            throw new MatchAlreadyStartedException(homeTeam, awayTeam);
        } else if (matches.values().stream()
                .anyMatch(match -> match.homeTeam().equals(homeTeam) || match.homeTeam().equals(awayTeam)
                        || match.awayTeam().equals(homeTeam) || match.awayTeam().equals(awayTeam))) {
            throw new TeamAlreadyInMatchException(homeTeam, awayTeam);
        }
        matches.put(generateKey(homeTeam, awayTeam), new Match(homeTeam, awayTeam, new Score(), LocalDateTime.now()));
    }


    public boolean containsMatch(String homeTeam, String awayTeam) {
        return matches.containsKey(generateKey(homeTeam, awayTeam));
    }

    public int matchCount() {
        return matches.size();
    }

    public String getScore(String homeTeam, String awayTeam) {
        Match match = findMatch(homeTeam, awayTeam);
        return match.toString();
    }

    public synchronized void updateScore(String homeTeam, String awayTeam, TeamType teamType) {
        checkTeams(homeTeam, awayTeam);
        if (teamType == null) {
            throw new IllegalArgumentException("Invalid team type");
        }
        Match match = findMatch(homeTeam, awayTeam);
        switch (teamType) {
            case HOME_TEAM -> match.score().incrementHomeScore();
            case AWAY_TEAM -> match.score().incrementAwayScore();
            default -> throw new IllegalArgumentException("Invalid team type.");
        }
    }

    public void adjustScoreForInfraction(String homeTeam, String awayTeam, TeamType teamType) {
        if (teamType == null) {
            throw new IllegalArgumentException("Team type cannot be null");
        }
        Match match = findMatch(homeTeam, awayTeam);
        switch (teamType) {
            case HOME_TEAM -> {
                if (match.score().getHomeScore() > 0) {
                    match.score().decrementHomeScore();
                } else {
                    throw new IllegalStateException("Cannot adjust score for infraction: Home team score is already at minimum.");
                }
            }
            case AWAY_TEAM -> {
                if (match.score().getAwayScore() > 0) {
                    match.score().decrementAwayScore();
                } else {
                    throw new IllegalStateException("Cannot adjust score for infraction: Away team score is already at minimum.");
                }
            }
            default -> throw new IllegalArgumentException("Invalid team type");
        }
    }

    public synchronized void finishMatch(String homeTeam, String awayTeam) {
        checkTeams(homeTeam, awayTeam);
        if (containsMatch(homeTeam, awayTeam)){
            matches.remove(generateKey(homeTeam, awayTeam));
        } else {
            throw new MatchNotFoundException(homeTeam, awayTeam);
        }
    }

    public List<String> getSummary(){
        return matches.values().stream()
                .sorted((match1, match2) -> {
                    int totalScore1 = match1.score().getHomeScore() + match1.score().getAwayScore();
                    int totalScore2 = match2.score().getHomeScore() + match2.score().getAwayScore();
                    int scoreComparison = Integer.compare(totalScore2, totalScore1);
                    if (scoreComparison != 0) {
                        // Return the comparison based on total score if they're not equal
                        return scoreComparison;
                    } else {
                        return match2.startTime().compareTo(match1.startTime());
                    }
                })
                .map(Match::toString)
                .collect(Collectors.toList());
    }

    private void checkTeams(String homeTeam, String awayTeam) {
        if (homeTeam == null || homeTeam.isEmpty()) {
            throw new IllegalArgumentException("Home team name cannot be null or empty");
        } else if (awayTeam == null || awayTeam.isEmpty()) {
            throw new IllegalArgumentException("Away team name cannot be null or empty");
        } else if (homeTeam.equals(awayTeam)) {
            throw new IllegalArgumentException("Home team and away team cannot be the same");
        } else if (containsMatch(awayTeam, homeTeam)) {
            throw new ExistingMatchConflictException(homeTeam, awayTeam);
        }
    }

    private Match findMatch(String homeTeam, String awayTeam) {
        Match match = matches.get(generateKey(homeTeam, awayTeam));
        if (match == null){
            throw new MatchNotFoundException(homeTeam, awayTeam);
        }
        return match;
    }

    private String generateKey(String homeTeam, String awayTeam) {
        return homeTeam + "-" + awayTeam;
    }

}
