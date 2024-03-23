import org.example.Scoreboard;
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
        int totalMatch = new Random().nextInt(0, 1000);

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
}
