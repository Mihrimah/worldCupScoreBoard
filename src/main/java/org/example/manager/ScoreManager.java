package org.example.manager;

import org.example.model.Match;
import org.example.model.TeamType;

/**
 * This class manages the scores of matches.
 * It allows the score of a match to be retrieved and updated.
 */
public class ScoreManager {
    private final MatchManager matchManager;

    public ScoreManager(MatchManager matchManager) {
        this.matchManager = matchManager;
    }
    public String getScore(String homeTeam, String awayTeam) {
        Match match = matchManager.findMatch(homeTeam, awayTeam);
        return match.toString();
    }

    /**
     * Updates the score of a match.
     * The score of the home team will be incremented by 1 if the home team scores.
     * The score of the away team will be incremented by 1 if the away team scores.
     *
     * @param homeTeam the name of the home team
     * @param awayTeam the name of the away team
     * @param teamType the team that scored
     * @throws IllegalArgumentException if the team type is null
     */
    public synchronized void updateScore(String homeTeam, String awayTeam, TeamType teamType) {
        matchManager.validateTeams(homeTeam, awayTeam);
        if (teamType == null) {
            throw new IllegalArgumentException("Invalid team type");
        }
        Match match = matchManager.findMatch(homeTeam, awayTeam);
        switch (teamType) {
            case HOME_TEAM -> match.score().incrementHomeScore();
            case AWAY_TEAM -> match.score().incrementAwayScore();
            default -> throw new IllegalArgumentException("Invalid team type.");
        }
    }

    /**
     * Adjusts the score of a match for an infraction. Since the score may be wrong due to an infraction such as a foul, offside, or handball, this method allows the score to be adjusted.
     * The score of the team that committed the infraction will be decremented by 1 if the score is greater than 0.
     *
     * @param homeTeam the name of the home team
     * @param awayTeam the name of the away team
     * @param teamType the team that committed the infraction
     * @throws IllegalArgumentException if the team type is null
     * @throws IllegalStateException if the score of the home team or away team is already at the minimum
     */
    public void adjustScoreForInfraction(String homeTeam, String awayTeam, TeamType teamType) {
        if (teamType == null) {
            throw new IllegalArgumentException("Team type cannot be null");
        }
        Match match = matchManager.findMatch(homeTeam, awayTeam);
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

}
