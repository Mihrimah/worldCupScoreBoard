package org.example;

import org.example.exceptions.ExistingMatchConflictException;
import org.example.exceptions.MatchAlreadyStartedException;
import org.example.exceptions.MatchNotFoundException;
import org.example.exceptions.TeamAlreadyInMatchException;

import java.time.LocalDateTime;

public class MatchManager {
    private final MatchRepository matchRepository;
    private final MatchKeyGenerator matchKeyGenerator;

    public MatchManager(MatchRepository matchRepository, MatchKeyGenerator matchKeyGenerator) {
        this.matchRepository = matchRepository;
        this.matchKeyGenerator = matchKeyGenerator;
    }

    /**
     * Starts a match between two teams.
     *
     * @param homeTeam the name of the home team
     * @param awayTeam the name of the away team
     * @throws IllegalArgumentException if the home team name or away team name is null or empty
     * @throws IllegalArgumentException if the home team name is the same as the away team name
     * @throws ExistingMatchConflictException if the match between the home team and away team is already started
     * @throws TeamAlreadyInMatchException if either the home team or away team is already in a match
     */
    public synchronized void startMatch(String homeTeam, String awayTeam) {
        validateTeams(homeTeam, awayTeam);
        if (matchRepository.containsMatch(generateKey(homeTeam, awayTeam))) {
            throw new MatchAlreadyStartedException(homeTeam, awayTeam);
        } else if (matchRepository.isTeamInAnyMatch(homeTeam) || matchRepository.isTeamInAnyMatch(awayTeam)){
            throw new TeamAlreadyInMatchException(homeTeam, awayTeam);
        }
        Match match = new Match(homeTeam, awayTeam, new Score(), LocalDateTime.now());
        matchRepository.addMatch(generateKey(homeTeam, awayTeam), match);
    }

    /**
     * Finishes a match between two teams.
     *
     * @param homeTeam the name of the home team
     * @param awayTeam the name of the away team
     * @throws IllegalArgumentException if the home team name or away team name is null or empty
     * @throws MatchNotFoundException if the match between the home team and away team is not found
     */
    public synchronized void finishMatch(String homeTeam, String awayTeam) {
        validateTeams(homeTeam, awayTeam);
        if (matchRepository.containsMatch(generateKey(homeTeam, awayTeam))){
            matchRepository.removeMatch(generateKey(homeTeam, awayTeam));
        } else {
            throw new MatchNotFoundException(homeTeam, awayTeam);
        }
    }

    /**
     * Checks the home team and away team.
     *
     * @param homeTeam the name of the home team
     * @param awayTeam the name of the away team
     * @throws IllegalArgumentException if the home team or away team is null or empty
     * @throws IllegalArgumentException if the home team is the same as the away team
     * @throws ExistingMatchConflictException if the match between the home team and away team is already started in the scoreboard but reversed.
     */
    public void validateTeams(String homeTeam, String awayTeam) {
        if (homeTeam == null || homeTeam.isEmpty()) {
            throw new IllegalArgumentException("Home team name cannot be null or empty");
        } else if (awayTeam == null || awayTeam.isEmpty()) {
            throw new IllegalArgumentException("Away team name cannot be null or empty");
        } else if (homeTeam.equals(awayTeam)) {
            throw new IllegalArgumentException("Home team and away team cannot be the same");
        } else if (matchRepository.containsMatch(generateKey(awayTeam, homeTeam))) {
            throw new ExistingMatchConflictException(homeTeam, awayTeam);
        }
    }

    /**
     * Finds a match between two teams.
     *
     * @param homeTeam the name of the home team
     * @param awayTeam the name of the away team
     * @return the match between the home team and away team
     * @throws MatchNotFoundException if the match between the home team and away team is not found
     */
    public Match findMatch(String homeTeam, String awayTeam) {
        Match match = matchRepository.getMatch(generateKey(homeTeam, awayTeam));
        if (match == null){
            throw new MatchNotFoundException(homeTeam, awayTeam);
        }
        return match;
    }

    /**
     * Generates a key for a match between two teams.
     *
     * @param homeTeam the name of the home team
     * @param awayTeam the name of the away team
     * @return the key for the match
     */
    private String generateKey(String homeTeam, String awayTeam) {
        return matchKeyGenerator.generateKey(homeTeam, awayTeam);
    }

}
