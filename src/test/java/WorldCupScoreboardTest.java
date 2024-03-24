import org.example.*;
import org.example.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class WorldCupScoreboardTest {

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
    @DisplayName("Given: A match. When: Get the score of the match. Then: The match cannot be found is thrown.")
    public void ensureMatchNotFoundExceptionForScoreRetrieval() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        // Initialize ScoreManager with the empty map.
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
    @DisplayName("Given: No matches have been started. When: Retrieving the match summary. Then: The summary should be empty.")
    public void testMatchSummaryWithNoMatches() {
        MatchSummaryGenerator summaryGenerator = new MatchSummaryGenerator(matchRepository);
        assertTrue(summaryGenerator.getSummary().isEmpty(), "Match summary should be empty when no matches have been started.");
    }
    @Test
    @DisplayName("Given: Multiple started matches with various scores. When: Retrieving the detailed match summary. Then: The summary should include all matches with correct scores.")
    public void testDetailedMatchSummary() {
        matchManager.startMatch("TeamA", "TeamB");
        ScoreManager scoreManager = new ScoreManager(matchManager);
        scoreManager.updateScore("TeamA", "TeamB", TeamType.HOME_TEAM); // TeamA 1 - TeamB 0
        matchManager.startMatch("TeamC", "TeamD"); // No score update for TeamC vs TeamD, remains 0 - 0

        MatchSummaryGenerator summaryGenerator = new MatchSummaryGenerator(matchRepository);
        List<String> summary = summaryGenerator.getSummary();
        assertEquals(2, summary.size(), "Summary should include exactly two matches.");
        assertTrue(summary.stream().anyMatch(s -> s.contains("TeamA 1 - TeamB 0")), "Summary should include the match between TeamA and TeamB with the score 1 - 0.");
        assertTrue(summary.stream().anyMatch(s -> s.contains("TeamC 0 - TeamD 0")), "Summary should include the match between TeamC and TeamD with the score 0 - 0.");
    }

    @Test
    @DisplayName("Given: Ongoing and finished matches. When: Retrieving the match summary. Then: Only ongoing matches should be included.")
    public void testMatchSummaryExcludesFinishedMatches() {
        matchManager.startMatch("TeamA", "TeamB");
        matchManager.startMatch("TeamC", "TeamD");
        matchManager.finishMatch("TeamA", "TeamB");

        MatchSummaryGenerator summaryGenerator = new MatchSummaryGenerator(matchRepository);
        List<String> summary = summaryGenerator.getSummary();
        assertFalse(summary.stream().anyMatch(s -> s.contains("TeamA 0 - TeamB 0")), "Finished matches should not appear in the match summary.");
        assertTrue(summary.stream().anyMatch(s -> s.contains("TeamC 0 - TeamD 0")), "Ongoing matches should appear in the match summary.");
    }

    @Test
    @DisplayName("Given: Multiple ongoing matches. When: Retrieving summary. Then: Get a summary of matches in progress ordered by their total score.")
    public void summarizeOngoingMatchesOrderedByTotalScore(){
        String homeTeamA = "TeamA";
        String awayTeamB = "TeamB";
        String homeTeamC = "TeamC";
        String awayTeamD = "TeamD";
        
        matchManager.startMatch(homeTeamA, awayTeamB);
        matchManager.startMatch(homeTeamC, awayTeamD);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        scoreManager.updateScore(homeTeamC, awayTeamD, TeamType.HOME_TEAM);

        MatchSummaryGenerator summaryGenerator = new MatchSummaryGenerator(matchRepository);
        assertFalse(summaryGenerator.getSummary().isEmpty());
        assertEquals(scoreManager.getScore(homeTeamC, awayTeamD), summaryGenerator.getSummary().get(0));
        assertEquals(scoreManager.getScore(homeTeamA, awayTeamB), summaryGenerator.getSummary().get(1));
    }

    @Test
    @DisplayName("Given: Tied matches in summary. When: Sorting. Then: The matches with the same total score will be returned ordered by the most recently started match in the scoreboard.")
    public void summarySortsTiedMatchesByMostRecentStartTime(){
        String homeTeamA = "TeamA";
        String awayTeamB = "TeamB";
        String homeTeamC = "TeamC";
        String awayTeamD = "TeamD";
        matchManager.startMatch(homeTeamA, awayTeamB);
        matchManager.startMatch(homeTeamC, awayTeamD);

        MatchSummaryGenerator summaryGenerator = new MatchSummaryGenerator(matchRepository);
        assertFalse(summaryGenerator.getSummary().isEmpty());
        assertEquals(2, summaryGenerator.getSummary().size());

        ScoreManager scoreManager = new ScoreManager(matchManager);
        assertEquals(scoreManager.getScore(homeTeamC, awayTeamD), summaryGenerator.getSummary().get(0));
    }

    @Test
    @DisplayName("Given: Matches with same total scores. When summarizing. Then: Sort by most recent start.")
    public void test19(){
        String homeTeamA = "TeamA";
        String awayTeamB = "TeamB";
        String homeTeamC = "TeamC";
        String awayTeamD = "TeamD";
        String homeTeamE = "TeamE";
        String awayTeamF = "TeamF";

        matchManager.startMatch(homeTeamA, awayTeamB);
        matchManager.startMatch(homeTeamC, awayTeamD);
        matchManager.startMatch(homeTeamE, awayTeamF);

        MatchSummaryGenerator summaryGenerator = new MatchSummaryGenerator(matchRepository);
        assertFalse(summaryGenerator.getSummary().isEmpty());
        assertEquals(3, summaryGenerator.getSummary().size());

        ScoreManager scoreManager = new ScoreManager(matchManager);
        assertEquals(scoreManager.getScore(homeTeamE, awayTeamF), summaryGenerator.getSummary().get(0));
        assertEquals(scoreManager.getScore(homeTeamC, awayTeamD), summaryGenerator.getSummary().get(1));
        assertEquals(scoreManager.getScore(homeTeamA, awayTeamB), summaryGenerator.getSummary().get(2));
    }

    @Test
    @DisplayName("Given: Multiple matches with updated scores. When: Retrieved from the scoreboard. Then: They should be ordered by total score and start time for ties.")
    public void testMatchesOrderedByScoreAndStartTime() {
        ScoreManager scoreManager = new ScoreManager(matchManager);
        MatchSummaryGenerator summaryGenerator = new MatchSummaryGenerator(matchRepository);

        // Start matches
        matchManager.startMatch("Mexico", "Canada");
        matchManager.startMatch("Spain", "Brazil");
        matchManager.startMatch("Germany", "France");
        matchManager.startMatch("Uruguay", "Italy");
        matchManager.startMatch("Argentina", "Australia");

        // Update scores as per the example
        updateScoreNTimes(scoreManager, "Mexico", "Canada", TeamType.AWAY_TEAM, 5);
        updateScoreNTimes(scoreManager, "Spain", "Brazil", TeamType.HOME_TEAM, 10);
        updateScoreNTimes(scoreManager, "Spain", "Brazil", TeamType.AWAY_TEAM, 2);
        updateScoreNTimes(scoreManager, "Germany", "France", TeamType.HOME_TEAM, 2);
        updateScoreNTimes(scoreManager, "Germany", "France", TeamType.AWAY_TEAM, 2);
        updateScoreNTimes(scoreManager, "Uruguay", "Italy", TeamType.HOME_TEAM, 6);
        updateScoreNTimes(scoreManager, "Uruguay", "Italy", TeamType.AWAY_TEAM, 6);
        updateScoreNTimes(scoreManager, "Argentina", "Australia", TeamType.HOME_TEAM, 3);
        updateScoreNTimes(scoreManager, "Argentina", "Australia", TeamType.AWAY_TEAM, 1);

        // Retrieve the summary and assert the order
        List<String> summary = summaryGenerator.getSummary();

        assertTrue(summary.get(0).contains("Uruguay 6 - Italy 6"), "Uruguay vs. Italy should be first due to the tie and more recent star.");
        assertTrue(summary.get(1).contains("Spain 10 - Brazil 2"), "Spain vs. Brazil should be second.");
        assertTrue(summary.get(2).contains("Mexico 0 - Canada 5"), "Mexico vs. Canada should be third.");
        assertTrue(summary.get(3).contains("Argentina 3 - Australia 1"), "Argentina vs. Australia should be fourth.");
        assertTrue(summary.get(4).contains("Germany 2 - France 2"), "Germany vs. France should be last due to the tie and earlier start.");
    }

    @RepeatedTest(100)
    @DisplayName("Given: Concurrent attempts to start matches. When: Multiple threads start matches. Then: All matches are started without interference.")
    public void testConcurrentMatchStarts() throws InterruptedException { // Concurrent map
        ExecutorService service = Executors.newFixedThreadPool(10); // Using 10 threads for example

        for (int i = 0; i < 20; i += 2) { // Start 10 matches
            String homeTeam = "Team" + i;
            String awayTeam = "Team" + (i + 1);
            service.submit(() -> {
                matchManager.startMatch(homeTeam, awayTeam);
                assertTrue(matchRepository.containsMatch(matchKeyGenerator.generateKey(homeTeam, awayTeam)));
            });
        }

        service.shutdown();
        service.awaitTermination(1, TimeUnit.MINUTES);

        assertEquals(10, matchRepository.countMatches());
    }

    @RepeatedTest(100)
    @DisplayName("Given: Concurrent attempts to start the same match. When: The same match is started by multiple threads. Then: Only one attempt should succeed.")
    public void testRaceConditionForTheSameMatchStart() throws InterruptedException { // synchronized startMatch
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger successfulStarts = new AtomicInteger(0);
        String homeTeam = "HomeTeam";
        String awayTeam = "AwayTeam";

        // Attempt to start the same match 100 times in parallel
        IntStream.range(0, 100).forEach(i -> executorService.submit(() -> {
            try {
                matchManager.startMatch(homeTeam, awayTeam);
                successfulStarts.incrementAndGet(); // If startMatch doesn't throw, count as successful start
            } catch (MatchAlreadyStartedException e) {
                // Expected for all but one thread, ignore or log as needed
            }
        }));

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(1, TimeUnit.MINUTES);

        assertTrue(finished, "Executor service didn't finish in the expected time.");
        assertEquals(1, successfulStarts.get(), "Only one match start should have succeeded.");

        // Optionally, verify the match is in the scoreboard and no additional matches were started
        assertTrue(matchRepository.containsMatch(matchKeyGenerator.generateKey(homeTeam, awayTeam)), "The match should exist in the scoreboard.");
        assertEquals(1, matchRepository.countMatches(), "There should only be one match in the scoreboard.");
    }

    @RepeatedTest(100)
    @DisplayName("Given: An ongoing match. When: Multiple threads update the score concurrently. Then: The final score is accurate.")
    public void testConcurrentScoreUpdates() throws InterruptedException {
        String homeTeam = "TeamConcurrency";
        String awayTeam = "TeamParallel";
        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);

        int updatesPerTeam = 100; // Number of score updates per team
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        IntStream.range(0, updatesPerTeam).forEach(i -> {
            executorService.submit(() -> scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM));
            executorService.submit(() -> scoreManager.updateScore(homeTeam, awayTeam, TeamType.AWAY_TEAM));
        });

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(1, TimeUnit.MINUTES);

        assertTrue(finished, "Executor service didn't finish in the expected time.");
        String finalScore = scoreManager.getScore(homeTeam, awayTeam);
        assertTrue(finalScore.contains(homeTeam + " " + updatesPerTeam + " - " + awayTeam + " " + updatesPerTeam), "The final score should accurately reflect all updates.");
    }

    @RepeatedTest(100)
    @DisplayName("Given: An ongoing match. When: Multiple threads attempt to finish the match concurrently. Then: Only one attempt should succeed and the match is finished.")
    public void testConcurrentMatchFinish() throws InterruptedException {
        String homeTeam = "TeamFinish";
        String awayTeam = "TeamEnd";
        matchManager.startMatch(homeTeam, awayTeam);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger finishAttempts = new AtomicInteger();

        IntStream.range(0, 10).forEach(i -> executorService.submit(() -> {
            try {
                matchManager.finishMatch(homeTeam, awayTeam);
                finishAttempts.incrementAndGet();
            } catch (Exception e) {
                // This could catch MatchNotFoundException if the match is already finished by another thread
            }
        }));

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(1, TimeUnit.MINUTES);

        assertTrue(finished, "Executor service didn't finish in the expected time.");
        assertEquals(1, finishAttempts.get(), "Only one finish attempt should have succeeded.");
        assertEquals(0, matchRepository.countMatches(), "The match should be removed from the scoreboard.");
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

    @RepeatedTest(100)
    @DisplayName("Given: An ongoing match. When: Scores are updated concurrently as the match finishes. Then: Ensure no exceptions and correct final state.")
    public void testConcurrentScoreUpdatesAndMatchFinish() throws InterruptedException {
        String homeTeam = "TeamEdge";
        String awayTeam = "TeamCase";

        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.submit(() -> IntStream.range(0, 50).forEach(i -> scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM)));
        executorService.submit(() -> matchManager.finishMatch(homeTeam, awayTeam));

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(1, TimeUnit.MINUTES);

        assertTrue(finished, "Executor service didn't finish in the expected time.");
        assertTrue(matchRepository.countMatches() <= 1, "Match should be finished and not accessible for score updates.");
    }

    @Test
    @DisplayName("Given: A high load scenario. When: Starting a large number of matches concurrently. Then: All matches start without error.")
    public void testHighLoadMatchStarts() throws InterruptedException {
        int numberOfMatches = 1000;
        ExecutorService executorService = Executors.newCachedThreadPool();
        IntStream.range(0, numberOfMatches).parallel().forEach(i -> {
            String homeTeam = "Home" + i;
            String awayTeam = "Away" + i;
            executorService.submit(() -> matchManager.startMatch(homeTeam, awayTeam));
        });

        executorService.shutdown();
        boolean finished = executorService.awaitTermination(2, TimeUnit.MINUTES);

        assertTrue(finished, "Executor service didn't finish in the expected time.");
        assertEquals(numberOfMatches, matchRepository.countMatches(), "All matches should have been started successfully.");
    }

    // Helper method to update the score n times for the specified team
    private void updateScoreNTimes(ScoreManager scoreManager, String homeTeam, String awayTeam, TeamType teamType, int times) {
        for (int i = 0; i < times; i++) {
            scoreManager.updateScore(homeTeam, awayTeam, teamType);
        }
    }

}
