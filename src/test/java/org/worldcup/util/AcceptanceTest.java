package org.worldcup.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.worldcup.manager.MatchManager;
import org.worldcup.manager.ScoreManager;
import org.worldcup.model.TeamType;
import org.worldcup.repository.InMemoryMatchRepository;
import org.worldcup.repository.MatchRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AcceptanceTest {

    private MatchManager matchManager;
    private MatchRepository matchRepository;
    private final SimpleMatchKeyGenerator matchKeyGenerator = new SimpleMatchKeyGenerator();

    @BeforeEach
    public void setUp() {
        matchRepository = new InMemoryMatchRepository();
        matchManager = new MatchManager(matchRepository, matchKeyGenerator);
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

    // Helper method to update the score n times for the specified team
    private void updateScoreNTimes(ScoreManager scoreManager, String homeTeam, String awayTeam, TeamType teamType, int times) {
        for (int i = 0; i < times; i++) {
            scoreManager.updateScore(homeTeam, awayTeam, teamType);
        }
    }

}
