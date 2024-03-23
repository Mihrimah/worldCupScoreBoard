import org.example.Scoreboard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorldCupScoreboardTest {

    @Test
    public void test1() {
        String homeTeam = "TeamA";
        String awayTeam = "TeamB";

        Scoreboard scoreboard = new Scoreboard();
        scoreboard.insertMatch(homeTeam, awayTeam);

        assertTrue(scoreboard.containsMatch(homeTeam, awayTeam));
        assertFalse(scoreboard.containsMatch("UnknownTeamA", "UnknownTeamB"));
    }

    @Test
    public void test2() {
        String teamPrefix = "Team";
        int totalMatch = 50;

        Scoreboard scoreboard = new Scoreboard();
        for (int i = 0; i < totalMatch * 2; i = i + 2) {
            String homeTeam = teamPrefix + i;
            String awayTeam = teamPrefix + i + 1;

            scoreboard.insertMatch(homeTeam, awayTeam);
            assertTrue(scoreboard.containsMatch(homeTeam, awayTeam));
        }

        assertTrue(scoreboard.matchCount() == totalMatch);
    }
}
