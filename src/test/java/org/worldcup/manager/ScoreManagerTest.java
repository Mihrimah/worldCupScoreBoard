package org.worldcup.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.worldcup.exceptions.MatchNotFoundException;
import org.worldcup.model.TeamType;
import org.worldcup.repository.InMemoryMatchRepository;
import org.worldcup.repository.MatchRepository;
import org.worldcup.util.MatchKeyGenerator;
import org.worldcup.util.SimpleMatchKeyGenerator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ScoreManagerTest {

    private MatchRepository matchRepository;
    private MatchManager matchManager;
    private ScoreManager scoreManager;

    @BeforeEach
    void setUp() {
        matchRepository = new InMemoryMatchRepository();
        MatchKeyGenerator matchKeyGenerator = new SimpleMatchKeyGenerator();
        matchManager = new MatchManager(matchRepository, matchKeyGenerator);
        scoreManager = new ScoreManager(matchManager);
    }

    private void startMatch(String homeTeam, String awayTeam) {
        matchManager.startMatch(homeTeam, awayTeam);
    }

    @Nested
    @DisplayName("Match Lifecycle Tests")
    class MatchLifecycleTests {
        @Test
        @DisplayName("Starting a match and getting initial score")
        void startMatchWithInitialZeroScore() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            startMatch(homeTeam, awayTeam);
            assertEquals("TeamA 0 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
        }

        @Test
        @DisplayName("Finishing a match removes it from the scoreboard")
        void finishMatchAndVerifyRemovalFromScoreboard() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            startMatch(homeTeam, awayTeam);
            matchManager.finishMatch(homeTeam, awayTeam);
            assertEquals(0, matchRepository.countMatches());
        }
    }

    @Nested
    @DisplayName("Score Update Tests")
    class ScoreUpdateTests {
        @Test
        @DisplayName("Updating scores for home and away teams")
        void updateAndVerifyHomeAndAwayTeamScores() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            startMatch(homeTeam, awayTeam);
            scoreManager.updateScore(homeTeam, awayTeam, TeamType.AWAY_TEAM);
            scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM);
            assertEquals("TeamA 1 - TeamB 1", scoreManager.getScore(homeTeam, awayTeam));
        }

        @Test
        @DisplayName("Ensure thread safety for concurrent score updates")
        void ensureThreadSafetyForConcurrentScoreUpdates() throws InterruptedException {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            startMatch(homeTeam, awayTeam);

            ExecutorService service = Executors.newFixedThreadPool(10);
            for (int i = 0; i < 10; i++) {
                service.submit(() -> scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM));
            }
            service.shutdown();
            assertTrue(service.awaitTermination(1, TimeUnit.SECONDS));
            assertEquals("TeamA 10 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
        }

    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        @Test
        @DisplayName("Given: A non-existent match. When: Attempting to get score for non-existent match Then: throws MatchNotFoundException")
        void ensureMatchNotFoundExceptionForScoreRetrieval() {
            assertThrows(MatchNotFoundException.class, () -> scoreManager.getScore("TeamA", "TeamB"));
        }

        @Test
        @DisplayName("Given: An ongoing match between TeamA and TeamB. When: Retrieving score with reversed teams. Then: MatchNotFoundException is thrown.")
        void preventGetReversedTeamMatchScore(){
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            startMatch(homeTeam, awayTeam);
            scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM);

            assertEquals("TeamA 1 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
            assertThrows(MatchNotFoundException.class, () -> scoreManager.getScore(awayTeam, homeTeam));
        }

        @Test
        @DisplayName("Updating score with null team names throws IllegalArgumentException")
        void updateScoreWithInvalidTeamNames() {
            startMatch("TeamA", "TeamB");
            assertThrows(IllegalArgumentException.class, () -> scoreManager.updateScore(null, "TeamB", TeamType.HOME_TEAM));
            assertThrows(IllegalArgumentException.class, () -> scoreManager.updateScore("TeamA", null, TeamType.AWAY_TEAM));
            assertThrows(IllegalArgumentException.class, () -> scoreManager.updateScore("", "", TeamType.AWAY_TEAM));
        }

        @Test
        @DisplayName("Given: An ongoing match. When: Attempting to adjust score with an invalid team type. Then: IllegalArgumentException should be thrown.")
        void testAdjustScoreWithInvalidTeamType() {
            startMatch("TeamA", "TeamB");
            assertThrows(IllegalArgumentException.class, () -> scoreManager.updateScore("TeamA", "TeamB", null), "Invalid team type");
        }

        @Test
        @DisplayName("Given: A finished match. When: Attempting to update the score. Then: Should throw MatchNotFoundException.")
        void attemptToUpdateScoresInFinishedMatch() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            startMatch(homeTeam, awayTeam);
            matchManager.finishMatch(homeTeam, awayTeam);

            assertThrows(MatchNotFoundException.class, () -> scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM));
        }
    }

    @Nested
    @DisplayName("Infraction Handling Tests")
    class InfractionHandlingTests {

        @Test
        @DisplayName("Reverting score after an infraction")
        void revertScoreAfterInfractionInOngoingMatch() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            startMatch(homeTeam, awayTeam);
            scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM);
            scoreManager.adjustScoreForInfraction(homeTeam, awayTeam, TeamType.HOME_TEAM);
            assertEquals("TeamA 0 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
        }

        @Test
        @DisplayName("Given: An on going match. When: Attempting to adjust the initial score. Then: Initial score cannot be adjusted")
        void attemptInitialScoreAdjustment() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            startMatch(homeTeam, awayTeam);

            assertThrows(IllegalStateException.class, () -> scoreManager.adjustScoreForInfraction(homeTeam, awayTeam, TeamType.HOME_TEAM));
            assertEquals("TeamA 0 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
        }

        @Test
        @DisplayName("Given: An ongoing match with a score. When: Adjusting the score for an infraction. Then: The score should revert to its previous state.")
        void adjustScoreForInfraction() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            startMatch(homeTeam, awayTeam);

            // Initially update the score
            scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM);
            assertEquals("TeamA 1 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));

            // Adjust the score for an infraction
            assertThrows(IllegalStateException.class, () -> scoreManager.adjustScoreForInfraction(homeTeam, awayTeam, TeamType.AWAY_TEAM));
            assertEquals("TeamA 1 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
        }

        @Test
        @DisplayName("Given: An ongoing match. When: Two infractions, removing two goals. Then: Score should correctly adjust for multiple infractions")
        void adjustScoresMultipleTimesForInfractions() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            startMatch(homeTeam, awayTeam);
            scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM); // 1-0
            scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM); // 2-0

            // Two infractions, removing two goals
            scoreManager.adjustScoreForInfraction(homeTeam, awayTeam, TeamType.HOME_TEAM);
            scoreManager.adjustScoreForInfraction(homeTeam, awayTeam, TeamType.HOME_TEAM);

            assertEquals("TeamA 0 - TeamB 0", scoreManager.getScore(homeTeam, awayTeam));
        }

    }
}
