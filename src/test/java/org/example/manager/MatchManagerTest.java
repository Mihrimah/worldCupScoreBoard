package org.example.manager;

import org.example.exceptions.ExistingMatchConflictException;
import org.example.exceptions.MatchAlreadyStartedException;
import org.example.exceptions.MatchNotFoundException;
import org.example.exceptions.TeamAlreadyInMatchException;
import org.example.model.Match;
import org.example.repository.InMemoryMatchRepository;
import org.example.repository.MatchRepository;
import org.example.util.MatchKeyGenerator;
import org.example.util.SimpleMatchKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class MatchManagerTest {

    private MatchRepository matchRepository;
    private MatchKeyGenerator matchKeyGenerator;
    private MatchManager matchManager;

    @BeforeEach
    public void setUp() {
        matchRepository = new InMemoryMatchRepository();
        matchKeyGenerator = new SimpleMatchKeyGenerator();
        matchManager = new MatchManager(matchRepository, matchKeyGenerator);
    }

    @Test
    @DisplayName("Given: A match. When: The match is inserted into scoreboard. Then: The match must exist in the scoreboard.")
    public void matchInsertionAndExistenceVerification() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        matchManager.startMatch(homeTeam, awayTeam);
        Match retrievedMatch = matchManager.findMatch(homeTeam, awayTeam);

        assertNotNull(retrievedMatch, "Retrieved match should not be null.");
        assertEquals(homeTeam, retrievedMatch.homeTeam(), "Home team should match.");
        assertEquals(awayTeam, retrievedMatch.awayTeam(), "Away team should match.");
    }

    @Test
    @DisplayName("Given: An invalid match setup. When: Attempting to start a match with the same team as both home and away. Then: IllegalArgumentException is thrown.")
    public void preventInvalidMatchSetup() {
        String homeTeam = "TeamA";
        assertThrows(IllegalArgumentException.class, () -> matchManager.startMatch(homeTeam, homeTeam));
    }

    @Test
    @DisplayName("Given: An attempt to start a match with null team names. When: Either the home team or away team name is null. Then: IllegalArgumentException is thrown.")
    public void testStartMatchWithNullTeamNames() {
        String homeTeam = "TeamA";

        // Test with null as the home team name
        IllegalArgumentException homeTeamException = assertThrows(IllegalArgumentException.class, () -> matchManager.startMatch(null, "TeamB"));
        assertEquals("Home team name cannot be null or empty", homeTeamException.getMessage());

        // Test with null as the away team name
        IllegalArgumentException awayTeamException = assertThrows(IllegalArgumentException.class, () -> matchManager.startMatch(homeTeam, null));
        assertEquals("Away team name cannot be null or empty", awayTeamException.getMessage());

        // Test with null for both team names
        IllegalArgumentException bothTeamsException = assertThrows(IllegalArgumentException.class, () -> matchManager.startMatch(null, null));
        assertEquals("Home team name cannot be null or empty", bothTeamsException.getMessage());
    }

    @Test
    @DisplayName("Given: An attempt to start a match with empty team names. When: Either the home team or away team name is an empty string. Then: IllegalArgumentException is thrown.")
    public void testStartMatchWithEmptyTeamNames() {
        String homeTeam = "TeamA";
        String emptyString = "";

        // Test with empty string as the home team name
        IllegalArgumentException homeTeamException = assertThrows(IllegalArgumentException.class, () -> matchManager.startMatch(emptyString, "TeamB"));
        assertEquals("Home team name cannot be null or empty", homeTeamException.getMessage());

        // Test with empty string as the away team name
        IllegalArgumentException awayTeamException = assertThrows(IllegalArgumentException.class, () -> matchManager.startMatch(homeTeam, emptyString));
        assertEquals("Away team name cannot be null or empty", awayTeamException.getMessage());

        // Test with empty strings for both team names
        IllegalArgumentException bothTeamsException = assertThrows(IllegalArgumentException.class, () -> matchManager.startMatch(emptyString, emptyString));
        assertEquals("Home team name cannot be null or empty", bothTeamsException.getMessage());
    }

    @Test
    @DisplayName("Given: A random number of matches. When: The all matches inserted into the scoreboard. Then: The match number in the scoreboard must be correct.")
    public void verifyCorrectMatchCountAfterInsertion() {
        String teamPrefix = "Team";
        int totalMatch = new Random().nextInt(1, 1000);
        
        for (int i = 0; i < totalMatch * 2; i = i + 2) {
            String homeTeam = teamPrefix + i;
            String awayTeam = teamPrefix + i + 1;

            matchManager.startMatch(homeTeam, awayTeam);
            assertTrue(matchRepository.containsMatch(matchKeyGenerator.generateKey(homeTeam, awayTeam)));
        }

        assertTrue(matchRepository.countMatches() > 0);
        assertEquals(matchRepository.countMatches(), totalMatch);
    }

    @Test
    @DisplayName("Given: A match that has already been started. When: Attempting to start the same match again. Then: MatchAlreadyStartedException is thrown.")
    public void preventDuplicateMatchStart(){
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        matchManager.startMatch(homeTeam, awayTeam);

        assertTrue(matchRepository.containsMatch(matchKeyGenerator.generateKey(homeTeam, awayTeam)));
        assertThrows(MatchAlreadyStartedException.class, () -> matchManager.startMatch(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: A match that has already been started. When: Attempting to start the match between the same team, but reversed. Then: ExistingMatchConflictException is thrown.")
    public void preventReversedTeamMatchStart(){
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        matchManager.startMatch(homeTeam, awayTeam);

        assertTrue(matchRepository.containsMatch(matchKeyGenerator.generateKey(homeTeam, awayTeam)));
        assertThrows(ExistingMatchConflictException.class, () -> matchManager.startMatch(awayTeam, homeTeam));
    }

    @Test
    @DisplayName("Given: A team is already in match. When: Attempting to start a match with the same team again. Then: TeamAlreadyInMatchException is thrown.")
    public void preventMatchStartWithTeamAlreadyInMatch() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        String newTeam = "TeamC";

        matchManager.startMatch(homeTeam, awayTeam);

        assertThrows(TeamAlreadyInMatchException.class, () -> matchManager.startMatch(homeTeam, newTeam));
        assertThrows(TeamAlreadyInMatchException.class, () -> matchManager.startMatch(newTeam, homeTeam));
        assertThrows(TeamAlreadyInMatchException.class, () -> matchManager.startMatch(newTeam, awayTeam));
        assertThrows(TeamAlreadyInMatchException.class, () -> matchManager.startMatch(awayTeam, newTeam));
    }

    @Test
    @DisplayName("Given: A non existing match. When: Attempting to finish the match. Then: MatchNotFoundException should be thrown.")
    public void attemptToFinishNonExistingMatch(){
        String homeTeam = "Mexico";
        String awayTeam = "Canada";
        assertThrows(MatchNotFoundException.class, () -> matchManager.finishMatch(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: Null or empty team names. When: Attempting to finish a match. Then: IllegalArgumentException should be thrown.")
    public void attemptToFinishMatchWithNullOrEmptyTeamNames() {
        String teamA = "TeamA";
        String teamB = "TeamB";

        // Test with null home team name
        assertThrows(IllegalArgumentException.class, () -> matchManager.finishMatch(null, teamB),
                "Attempting to finish a match with a null home team name should throw IllegalArgumentException.");

        // Test with null away team name
        assertThrows(IllegalArgumentException.class, () -> matchManager.finishMatch(teamA, null),
                "Attempting to finish a match with a null away team name should throw IllegalArgumentException.");

        // Test with empty home team name
        assertThrows(IllegalArgumentException.class, () -> matchManager.finishMatch("", teamB),
                "Attempting to finish a match with an empty home team name should throw IllegalArgumentException.");

        // Test with empty away team name
        assertThrows(IllegalArgumentException.class, () -> matchManager.finishMatch(teamA, ""),
                "Attempting to finish a match with an empty away team name should throw IllegalArgumentException.");
    }

}
