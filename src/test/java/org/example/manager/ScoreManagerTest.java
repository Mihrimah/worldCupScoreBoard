package org.example.manager;

import org.example.exceptions.MatchNotFoundException;
import org.example.model.TeamType;
import org.example.repository.InMemoryMatchRepository;
import org.example.repository.MatchRepository;
import org.example.util.MatchKeyGenerator;
import org.example.util.SimpleMatchKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScoreManagerTest {

    private MatchRepository matchRepository;
    private MatchManager matchManager;

    @BeforeEach
    public void setUp() {
        matchRepository = new InMemoryMatchRepository();
        MatchKeyGenerator matchKeyGenerator = new SimpleMatchKeyGenerator();
        matchManager = new MatchManager(matchRepository, matchKeyGenerator);
    }

    @Test
    @DisplayName("Given: A match. When: Get the score of the match. Then: The match cannot be found is thrown.")
    public void ensureMatchNotFoundExceptionForScoreRetrieval() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        ScoreManager scoreManager = new ScoreManager(matchManager);
        assertThrows(MatchNotFoundException.class, () -> scoreManager.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: A match. When: Get the score of the match between reversed teams. Then: The match cannot be found is thrown.")
    public void disallowReversedTeamsScoreRetrieval() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        assertThrows(MatchNotFoundException.class, () -> scoreManager.getScore(awayTeam, homeTeam));
    }

    @Test
    @DisplayName("Given: A match. When: Insert the match into the scoreboard and start it. Then: The match must start with score 0-0.")
    public void startMatchWithInitialZeroScore() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        matchManager.startMatch(homeTeam, awayTeam);

        String expectedInitialScore = "TeamA 0 - TeamB 0";

        ScoreManager scoreManager = new ScoreManager(matchManager);
        assertEquals(expectedInitialScore, scoreManager.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: An ongoing match. When: Update home team's score. Then: The score must be correctly updated.")
    public void updateAndVerifyHomeTeamScore() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM);

        String expectedScore = "TeamA 1 - TeamB 0";

        assertEquals(expectedScore, scoreManager.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: An ongoing match. When: Update away team's score. Then: The score must be correctly updated.")
    public void updateAndVerifyHomeAndAwayTeamScores(){
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        
        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        scoreManager.updateScore(homeTeam, awayTeam, TeamType.AWAY_TEAM);
        scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM);

        String expectedScore = "TeamA 1 - TeamB 1";

        assertEquals(expectedScore, scoreManager.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: An ongoing match. When: Attempting to adjust score with an invalid team type. Then: IllegalArgumentException should be thrown.")
    public void testAdjustScoreWithInvalidTeamType() {
        matchManager.startMatch("TeamA", "TeamB");

        ScoreManager scoreManager = new ScoreManager(matchManager);
        assertThrows(IllegalArgumentException.class, () -> scoreManager.updateScore("TeamA", "TeamB", null), "Invalid team type");
    }

    @Test
    @DisplayName("Given: An ongoing match. When: Attempting to update score with null for homeTeam or awayTeam. Then: IllegalArgumentException should be thrown.")
    public void testUpdateScoreWithNullTeamNames() {
        matchManager.startMatch("TeamA", "TeamB");

        ScoreManager scoreManager = new ScoreManager(matchManager);

        // Test with null as the homeTeam
        assertThrows(IllegalArgumentException.class, () -> scoreManager.updateScore(null, "TeamB", TeamType.HOME_TEAM), "Updating score with a null homeTeam should throw IllegalArgumentException.");

        // Test with null as the awayTeam
        assertThrows(IllegalArgumentException.class, () -> scoreManager.updateScore("TeamA", null, TeamType.AWAY_TEAM), "Updating score with a null awayTeam should throw IllegalArgumentException.");

        // Test with null for both homeTeam and awayTeam
        assertThrows(IllegalArgumentException.class, () -> scoreManager.updateScore(null, null, TeamType.HOME_TEAM), "Updating score with null for both homeTeam and awayTeam should throw IllegalArgumentException.");
    }

    @Test
    @DisplayName("Given: An ongoing match between TeamA and TeamB. When: Retrieving score with reversed teams. Then: MatchNotFoundException is thrown.")
    public void preventReversedTeamMatchScore(){
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        assertEquals("TeamA 0 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
        assertThrows(MatchNotFoundException.class, () -> scoreManager.getScore(awayTeam, homeTeam));
    }

    @Test
    @DisplayName("Given: A match is on going. When: Attempting to update the score. Then: Score should be updated.")
    public void updateAwayTeamScore(){
        String homeTeam = "Mexico";
        String awayTeam = "Canada";

        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        scoreManager.updateScore(homeTeam, awayTeam, TeamType.AWAY_TEAM);
        scoreManager.updateScore(homeTeam, awayTeam, TeamType.AWAY_TEAM);
        scoreManager.updateScore(homeTeam, awayTeam, TeamType.AWAY_TEAM);
        scoreManager.updateScore(homeTeam, awayTeam, TeamType.AWAY_TEAM);
        scoreManager.updateScore(homeTeam, awayTeam, TeamType.AWAY_TEAM);

        String expectedResult = "Mexico 0 - Canada 5";
        assertEquals(expectedResult, scoreManager.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: An on going match. When: Attempting to finish the match. Then: The match should not exist in scoreboard.")
    public void finishMatchAndVerifyRemovalFromScoreboard(){
        String homeTeam = "Mexico";
        String awayTeam = "Canada";

        ScoreManager scoreManager = new ScoreManager(matchManager);
        matchManager.startMatch(homeTeam, awayTeam);
        assertEquals("Mexico 0 - Canada 0", scoreManager.getScore(homeTeam, awayTeam));

        matchManager.finishMatch(homeTeam, awayTeam);
        assertEquals(0, matchRepository.countMatches());
    }

    @Test
    @DisplayName("Given: A non-existent match. When: Attempting to update the score. Then: MatchNotFoundException should be thrown.")
    public void testScoreUpdateForNonExistentMatch() {
        ScoreManager scoreManager = new ScoreManager(matchManager);
        assertThrows(MatchNotFoundException.class, () -> scoreManager.updateScore("GhostHome", "GhostAway", TeamType.HOME_TEAM), "Attempting to update the score for a non-existent match should throw MatchNotFoundException.");
    }

    @Test
    @DisplayName("Given: An ongoing match. When: A score has been recorded, but an infraction occurred such as offside. Then: The score should revert to its previous state.")
    public void revertScoreAfterInfractionInOngoingMatch(){
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM);
        assertEquals("TeamA 1 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));

        scoreManager.adjustScoreForInfraction(homeTeam, awayTeam, TeamType.HOME_TEAM);
        assertEquals("TeamA 0 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: An on going match. When: Attempting to adjust the initial score. Then: Initial score cannot be adjusted")
    public void attemptInitialScoreAdjustment(){
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        assertThrows(IllegalStateException.class, () -> scoreManager.adjustScoreForInfraction(homeTeam, awayTeam, TeamType.HOME_TEAM));
        assertEquals("TeamA 0 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given an ongoing match with a score. When adjusting the score for an infraction. Then the score should revert to its previous state.")
    public void adjustScoreForInfraction() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        // Initially update the score
        scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM);
        assertEquals("TeamA 1 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));

        // Adjust the score for an infraction
        assertThrows(IllegalStateException.class, () -> scoreManager.adjustScoreForInfraction(homeTeam, awayTeam, TeamType.AWAY_TEAM));
        assertEquals("TeamA 1 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: A finished match. When: Attempting to update the score. Then: Should throw MatchNotFoundException.")
    public void testScoreUpdateAfterMatchFinish() {
        String homeTeam = "TeamFinal";
        String awayTeam = "TeamLast";
        matchManager.startMatch(homeTeam, awayTeam);
        matchManager.finishMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);

        assertThrows(MatchNotFoundException.class, () -> scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM), "Updating score after match finish should throw MatchNotFoundException.");
    }

}
