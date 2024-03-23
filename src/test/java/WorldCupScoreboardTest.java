import org.example.Scoreboard;
import org.example.TeamType;
import org.example.exceptions.DuplicateMatchInsertException;
import org.example.exceptions.MatchNotStartedException;
import org.example.exceptions.MatchNotFoundException;
import org.example.exceptions.OngoingMatchStartException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class WorldCupScoreboardTest {

    @Test
    @DisplayName("Given: A match. When: The match is inserted into scoreboard. Then: The match must exist in the scoreboard.")
    public void test1() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        Scoreboard scoreboard = new Scoreboard();
        scoreboard.insertMatch(homeTeam, awayTeam);

        assertTrue(scoreboard.containsMatch(homeTeam, awayTeam));
        assertFalse(scoreboard.containsMatch("UnknownTeamA", "UnknownTeamB"));
    }

    @Test
    @DisplayName("Given: A random number of matches. When: The all matches inserted into the scoreboard. Then: The match number in the scoreboard must be correct.")
    public void test2() {
        String teamPrefix = "Team";
        int totalMatch = new Random().nextInt(1, 1000);

        Scoreboard scoreboard = new Scoreboard();
        for (int i = 0; i < totalMatch * 2; i = i + 2) {
            String homeTeam = teamPrefix + i;
            String awayTeam = teamPrefix + i + 1;

            scoreboard.insertMatch(homeTeam, awayTeam);
            assertTrue(scoreboard.containsMatch(homeTeam, awayTeam));
        }

        assertTrue(scoreboard.matchCount() > 0);
        assertEquals(scoreboard.matchCount(), totalMatch);
    }

    @Test
    @DisplayName("Given: A match. When: Get the score of the match. Then: The match cannot be found is thrown.")
    public void test3() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        Scoreboard scoreboard = new Scoreboard();
        assertThrows(MatchNotFoundException.class, () -> scoreboard.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: A match. When: Insert the match into the scoreboard and get its score. Then: The match exist but not started is thrown.")
    public void test4() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        Scoreboard scoreboard = new Scoreboard();
        scoreboard.insertMatch(homeTeam, awayTeam);

        assertThrows(MatchNotStartedException.class, () -> scoreboard.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: A match. When: Insert the match into the scoreboard and start it. Then: The match must start with score 0-0.")
    public void test5() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        Scoreboard scoreboard = new Scoreboard();
        scoreboard.insertMatch(homeTeam, awayTeam);
        scoreboard.startMatch(homeTeam, awayTeam);

        String expectedInitialScore = "TeamA 0 - TeamB 0";

        assertEquals(expectedInitialScore, scoreboard.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: An ongoing match. When: Update home team's score. Then: The score must be correctly updated.")
    public void test6() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        Scoreboard scoreboard = new Scoreboard();
        scoreboard.insertMatch(homeTeam, awayTeam);
        scoreboard.startMatch(homeTeam, awayTeam);

        scoreboard.updateScore(homeTeam, awayTeam, TeamType.homeTeam);

        String expectedScore = "TeamA 1 - TeamB 0";

        assertEquals(expectedScore, scoreboard.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: An ongoing match. When: Update away team's score. Then: The score must be correctly updated.")
    public void test7(){
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        Scoreboard scoreboard = new Scoreboard();
        scoreboard.insertMatch(homeTeam, awayTeam);
        scoreboard.startMatch(homeTeam, awayTeam);

        scoreboard.updateScore(homeTeam, awayTeam, TeamType.awayTeam);
        scoreboard.updateScore(homeTeam, awayTeam, TeamType.awayTeam);

        String expectedScore = "TeamA 0 - TeamB 2";

        assertEquals(expectedScore, scoreboard.getScore(homeTeam, awayTeam));
    }

    @Test
    @DisplayName("Given: An inserted and started match between TeamA and TeamB. When: Retrieving score with reversed teams. Then: MatchNotFoundException is thrown.")
    public void test8(){
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        Scoreboard scoreboard = new Scoreboard();
        scoreboard.insertMatch(homeTeam, awayTeam);
        scoreboard.startMatch(homeTeam, awayTeam);

        assertEquals("TeamA 0 - TeamB 0", scoreboard.getScore(homeTeam, awayTeam));
        assertThrows(MatchNotFoundException.class, () -> scoreboard.getScore(awayTeam, homeTeam));
    }

    @Test
    @DisplayName("Given: A match that has already been inserted. When: Attempting to insert the same match again. Then: DuplicateMatchInsertException is thrown.")
    public void test9(){
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        Scoreboard scoreboard = new Scoreboard();
        scoreboard.insertMatch(homeTeam, awayTeam);

        assertTrue(scoreboard.containsMatch(homeTeam, awayTeam));
        assertThrows(DuplicateMatchInsertException.class, () -> scoreboard.insertMatch(homeTeam, awayTeam));

    }

    @Test
    @DisplayName("Given: A match that has already been started. When: Attempting to start the same match again. Then: OngoingMatchStartException is thrown.")
    public void test10(){
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";
        Scoreboard scoreboard = new Scoreboard();
        scoreboard.insertMatch(homeTeam, awayTeam);
        scoreboard.startMatch(homeTeam, awayTeam);
        assertThrows(OngoingMatchStartException.class, () -> scoreboard.startMatch(homeTeam, awayTeam));
    }

}
