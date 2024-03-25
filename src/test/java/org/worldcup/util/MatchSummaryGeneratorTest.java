package org.worldcup.util;

import org.worldcup.manager.MatchManager;
import org.worldcup.manager.ScoreManager;
import org.worldcup.model.TeamType;
import org.worldcup.repository.InMemoryMatchRepository;
import org.worldcup.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchSummaryGeneratorTest {

    private MatchRepository matchRepository;
    private MatchManager matchManager;

    @BeforeEach
    public void setUp() {
        matchRepository = new InMemoryMatchRepository();
        MatchKeyGenerator matchKeyGenerator = new SimpleMatchKeyGenerator();
        matchManager = new MatchManager(matchRepository, matchKeyGenerator);
    }

    @Test
    @DisplayName("Given: No matches have been started. When: Retrieving the match summary. Then: The summary should be empty.")
    public void summaryWithNoMatches() {
        MatchSummaryGenerator summaryGenerator = new MatchSummaryGenerator(matchRepository);
        assertTrue(summaryGenerator.getSummary().isEmpty(), "Match summary should be empty when no matches have been started.");
    }
    @Test
    @DisplayName("Given: Multiple started matches with various scores. When: Retrieving the detailed match summary. Then: The summary should include all matches with correct scores.")
    public void detailedMatchSummary() {
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
    public void summaryExcludesFinishedMatches() {
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
    public void summarySortsByMostRecentMatch(){
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

}
