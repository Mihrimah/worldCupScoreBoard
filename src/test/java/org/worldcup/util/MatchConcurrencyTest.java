package org.worldcup.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.worldcup.exceptions.MatchAlreadyStartedException;
import org.worldcup.manager.MatchManager;
import org.worldcup.manager.ScoreManager;
import org.worldcup.model.TeamType;
import org.worldcup.repository.InMemoryMatchRepository;
import org.worldcup.repository.MatchRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MatchConcurrencyTest {

    private MatchManager matchManager;
    private MatchRepository matchRepository;
    private final SimpleMatchKeyGenerator matchKeyGenerator = new SimpleMatchKeyGenerator();

    @BeforeEach
    public void setUp() {
        matchRepository = new InMemoryMatchRepository();
        matchManager = new MatchManager(matchRepository, matchKeyGenerator);
    }

    @Test
    @DisplayName("Concurrent match starts")
    public void concurrentMatchStarts() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);

        IntStream.range(0, 20).forEach(i -> service.submit(() -> {
            try {
                matchManager.startMatch("Team" + i, "Team" + (i + 1));
            } catch (MatchAlreadyStartedException ignored) {
            }
        }));

        service.shutdown();
        assertTrue(service.awaitTermination(1, TimeUnit.MINUTES));
        assertEquals(10, matchRepository.countMatches());
    }

    @Test
    @DisplayName("Race condition for the same match start")
    public void raceConditionForSameMatchStart() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger successfulStarts = new AtomicInteger(0);

        IntStream.range(0, 100).forEach(i -> executorService.submit(() -> {
            try {
                matchManager.startMatch("HomeTeam", "AwayTeam");
                successfulStarts.incrementAndGet();
            } catch (MatchAlreadyStartedException ignored) {
            }
        }));

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));
        assertEquals(1, successfulStarts.get());
        assertEquals(1, matchRepository.countMatches());
    }

    @Test
    @DisplayName("Concurrent score updates")
    public void concurrentScoreUpdates() throws InterruptedException {
        String homeTeam = "TeamConcurrency";
        String awayTeam = "TeamParallel";
        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        IntStream.range(0, 100).forEach(i -> {
            executorService.submit(() -> scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM));
            executorService.submit(() -> scoreManager.updateScore(homeTeam, awayTeam, TeamType.AWAY_TEAM));
        });

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));
        assertEquals("TeamConcurrency 100 - TeamParallel 100", scoreManager.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Concurrent match finish")
    public void concurrentMatchFinish() throws InterruptedException {
        String homeTeam = "TeamFinish";
        String awayTeam = "TeamEnd";
        matchManager.startMatch(homeTeam, awayTeam);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        AtomicInteger finishAttempts = new AtomicInteger();

        IntStream.range(0, 10).forEach(i -> executorService.submit(() -> {
            try {
                matchManager.finishMatch(homeTeam, awayTeam);
                finishAttempts.incrementAndGet();
            } catch (Exception ignored) {
            }
        }));

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));
        assertEquals(1, finishAttempts.get());
        assertEquals(0, matchRepository.countMatches());
    }

    @Test
    @DisplayName("Concurrent score updates and match finish")
    public void concurrentScoreUpdatesAndMatchFinish() throws InterruptedException {
        String homeTeam = "TeamEdge";
        String awayTeam = "TeamCase";

        matchManager.startMatch(homeTeam, awayTeam);

        ScoreManager scoreManager = new ScoreManager(matchManager);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.submit(() -> IntStream.range(0, 50).forEach(i -> scoreManager.updateScore(homeTeam, awayTeam, TeamType.HOME_TEAM)));
        executorService.submit(() -> matchManager.finishMatch(homeTeam, awayTeam));

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));
        assertTrue(matchRepository.countMatches() <= 1);
    }

    @Test
    @DisplayName("High load scenario for match starts")
    public void highLoadMatchStarts() throws InterruptedException {
        int numberOfMatches = 1000;
        ExecutorService executorService = Executors.newCachedThreadPool();
        IntStream.range(0, numberOfMatches).parallel().forEach(i -> {
            String homeTeam = "Home" + i;
            String awayTeam = "Away" + i;
            executorService.submit(() -> matchManager.startMatch(homeTeam, awayTeam));
        });

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(2, TimeUnit.MINUTES));
        assertEquals(numberOfMatches, matchRepository.countMatches());
    }
}
