package org.worldcup.model;

/**
 * This class represents a match between two teams.
 * It contains the home team, the away team, the score, and the start time of the match.
 */
public record Match(String homeTeam, String awayTeam, Score score) {

    @Override
    public String toString() {
        return String.format("%s %d - %s %d", homeTeam, score.getHomeScore(), awayTeam, score.getAwayScore());
    }
}
