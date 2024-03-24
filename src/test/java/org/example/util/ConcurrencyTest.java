package org.example.util;

import org.example.exceptions.MatchAlreadyStartedException;
import org.example.manager.MatchManager;
import org.example.manager.ScoreManager;
import org.example.model.TeamType;
import org.example.repository.InMemoryMatchRepository;
import org.example.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrencyTest {

    private MatchManager matchManager;
    private MatchRepository matchRepository;
    private final SimpleMatchKeyGenerator matchKeyGenerator = new SimpleMatchKeyGenerator();

    @BeforeEach
    public void setUp() {
        matchRepository = new InMemoryMatchRepository();
        matchManager = new MatchManager(matchRepository, matchKeyGenerator);
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
        boolean termination = service.awaitTermination(1, TimeUnit.MINUTES);
        assertTrue(termination, "Executor service didn't finish in the expected time.");

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
