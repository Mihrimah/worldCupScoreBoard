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
}
