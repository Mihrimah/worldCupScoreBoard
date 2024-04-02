package org.worldcup.manager;

import org.junit.jupiter.api.Nested;
import org.worldcup.exceptions.ExistingMatchConflictException;
import org.worldcup.exceptions.MatchAlreadyStartedException;
import org.worldcup.exceptions.MatchNotFoundException;
import org.worldcup.exceptions.TeamAlreadyInMatchException;
import org.worldcup.model.Match;
import org.worldcup.repository.InMemoryMatchRepository;
import org.worldcup.repository.MatchRepository;
import org.worldcup.util.MatchKeyGenerator;
import org.worldcup.util.SimpleMatchKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MatchManagerTest {

    private MatchRepository matchRepository;
    private MatchKeyGenerator matchKeyGenerator;
    private MatchManager matchManager;

    @BeforeEach
    void setUp() {
        matchRepository = new InMemoryMatchRepository();
        matchKeyGenerator = new SimpleMatchKeyGenerator();
        matchManager = new MatchManager(matchRepository, matchKeyGenerator);
    }

    @Nested
    @DisplayName("Match Insertion Tests")
    class MatchInsertionTests {
        @Test
        @DisplayName("Given: A match. When: The match is inserted into scoreboard. Then: The match must exist in the scoreboard.")
        void matchInsertionAndExistence() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            matchManager.startMatch(homeTeam, awayTeam);

            assertTrue(matchRepository.containsMatch(matchKeyGenerator.generateKey(homeTeam, awayTeam)));
        }

        @Test
        @DisplayName("Given: A match that has been inserted into scoreboard. When: Attempting to retrieve the match. Then: The match must be found.")
        void matchExistenceVerification() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            matchManager.startMatch(homeTeam, awayTeam);

            Match retrievedMatch = matchManager.findMatch(homeTeam, awayTeam);

            assertNotNull(retrievedMatch, "Retrieved match should not be null.");
            assertEquals(homeTeam, retrievedMatch.homeTeam(), "Home team should match.");
            assertEquals(awayTeam, retrievedMatch.awayTeam(), "Away team should match.");
        }

        @Test
        @DisplayName("Given: A match with team names in different cases. When: Inserting and retrieving the match. Then: Match should be found.")
        void matchInsertionAndExistenceWithDifferentCase() {
            String homeTeam = "TeamA";
            String awayTeam = "teamB"; // TeamB in different case

            // Act
            matchManager.startMatch(homeTeam, awayTeam);
            Match retrievedMatch = matchManager.findMatch(homeTeam.toUpperCase(), awayTeam.toUpperCase());

            // Assert
            assertNotNull(retrievedMatch, "Retrieved match should not be null.");
            assertEquals(homeTeam, retrievedMatch.homeTeam(), "Home team should match.");
            assertEquals(awayTeam, retrievedMatch.awayTeam(), "Away team should match.");
        }

        @Test
        @DisplayName("Given: A match with very long team names. When: Inserting and retrieving the match. Then: Match should be found.")
        void matchInsertionAndExistenceWithLongTeamNames() {
            String homeTeam = "A".repeat(100); // Very long team name
            String awayTeam = "B".repeat(100); // Very long team name

            // Act
            matchManager.startMatch(homeTeam, awayTeam);
            Match retrievedMatch = matchManager.findMatch(homeTeam, awayTeam);

            // Assert
            assertNotNull(retrievedMatch, "Retrieved match should not be null.");
            assertEquals(homeTeam, retrievedMatch.homeTeam(), "Home team should match.");
            assertEquals(awayTeam, retrievedMatch.awayTeam(), "Away team should match.");
        }

        @Test
        @DisplayName("Given: A match with special characters in team names. When: Inserting and retrieving the match. Then: Match should be found.")
        void matchInsertionAndExistenceWithSpecialCharacters() {
            String homeTeam = "Team_A"; // Team name with underscore
            String awayTeam = "Team-B"; // Team name with hyphen

            // Act
            matchManager.startMatch(homeTeam, awayTeam);
            Match retrievedMatch = matchManager.findMatch(homeTeam, awayTeam);

            // Assert
            assertNotNull(retrievedMatch, "Retrieved match should not be null.");
            assertEquals(homeTeam, retrievedMatch.homeTeam(), "Home team should match.");
            assertEquals(awayTeam, retrievedMatch.awayTeam(), "Away team should match.");
        }
    }

    @Nested
    @DisplayName("Validate Teams Tests")
    class ValidateTeamsTests {

        @Test
        @DisplayName("Given: Null home team. When: Validating teams. Then: IllegalArgumentException is thrown.")
        void validateNullHomeTeam() {
            assertThrows(IllegalArgumentException.class, () -> matchManager.validateTeams(null, "TeamB"));
        }

        @Test
        @DisplayName("Given: Empty home team. When: Validating teams. Then: IllegalArgumentException is thrown.")
        void validateEmptyHomeTeam() {
            assertThrows(IllegalArgumentException.class, () -> matchManager.validateTeams("", "TeamB"));
        }

        @Test
        @DisplayName("Given: Null away team. When: Validating teams. Then: IllegalArgumentException is thrown.")
        void validateNullAwayTeam() {
            assertThrows(IllegalArgumentException.class, () -> matchManager.validateTeams("TeamA", null));
        }

        @Test
        @DisplayName("Given: Empty away team. When: Validating teams. Then: IllegalArgumentException is thrown.")
        void validateEmptyAwayTeam() {
            assertThrows(IllegalArgumentException.class, () -> matchManager.validateTeams("TeamA", ""));
        }

        @Test
        @DisplayName("Given: Same home and away teams. When: Validating teams. Then: IllegalArgumentException is thrown.")
        void validateSameTeams() {
            assertThrows(IllegalArgumentException.class, () -> matchManager.validateTeams("TeamA", "TeamA"));
        }

    }

    @Nested
    @DisplayName("Match Start Tests")
    class MatchStartTests {

        @Test
        @DisplayName("Given: A match that has already been started. When: Attempting to start the same match again. Then: MatchAlreadyStartedException is thrown.")
        void validateDuplicateMatchStart() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            matchManager.startMatch(homeTeam, awayTeam);

            assertTrue(matchRepository.containsMatch(matchKeyGenerator.generateKey(homeTeam, awayTeam)));
            assertThrows(MatchAlreadyStartedException.class, () -> matchManager.startMatch(homeTeam, awayTeam));
        }

        @Test
        @DisplayName("Given: A match that has already been started. When: Attempting to start the match between the same team, but reversed. Then: ExistingMatchConflictException is thrown.")
        void validateReversedTeamNamesMatchStart() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            matchManager.startMatch(homeTeam, awayTeam);

            assertTrue(matchRepository.containsMatch(matchKeyGenerator.generateKey(homeTeam, awayTeam)));
            assertThrows(ExistingMatchConflictException.class, () -> matchManager.startMatch(awayTeam, homeTeam));
        }

        @Test
        @DisplayName("Given: A random number of matches. When: The all matches inserted into the scoreboard. Then: The match number in the scoreboard must be correct.")
        void verifyCorrectMatchCountAfterInsertion() {
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

    }

    @Nested
    @DisplayName("Prevent Match Start With Team Already In Match Tests")
    class PreventMatchStartWithTeamAlreadyInMatchTests {

        @Test
        @DisplayName("Given: Home team is already in a match. When: Attempting to start a match with the same home team again. Then: TeamAlreadyInMatchException is thrown.")
        void preventMatchStartWithHomeTeamAlreadyInMatch() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            String newTeam = "TeamC";
            matchManager.startMatch(homeTeam, awayTeam);
            assertThrows(TeamAlreadyInMatchException.class, () -> matchManager.startMatch(homeTeam, newTeam));
        }

        @Test
        @DisplayName("Given: Away team is already in a match. When: Attempting to start a match with the same away team again. Then: TeamAlreadyInMatchException is thrown.")
        void preventMatchStartWithAwayTeamAlreadyInMatch() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            String newTeam = "TeamC";
            matchManager.startMatch(homeTeam, awayTeam);
            assertThrows(TeamAlreadyInMatchException.class, () -> matchManager.startMatch(newTeam, awayTeam));
        }

        @Test
        @DisplayName("Given: A team is already in a match. When: Attempting to start a match with the same team again (home and away). Then: TeamAlreadyInMatchException is thrown.")
        void preventMatchStartWithBothTeamsAlreadyInMatch() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            matchManager.startMatch(homeTeam, "TeamC");
            matchManager.startMatch("TeamD", awayTeam);
            assertThrows(TeamAlreadyInMatchException.class, () -> matchManager.startMatch(homeTeam, awayTeam));
        }

        @Test
        @DisplayName("Given: A team is already in a match. When: Attempting to start a match with the same team again (away and home). Then: ExistingMatchConflictException is thrown.")
        void preventMatchStartWithTeamsReversedExistingMatchConflict() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            matchManager.startMatch(homeTeam, awayTeam);
            assertThrows(ExistingMatchConflictException.class, () -> matchManager.startMatch(awayTeam, homeTeam));
        }

    }

    @Nested
    @DisplayName("Match Finish Tests")
    class MatchFinishTests {

        @Test
        @DisplayName("Given: An existing match. When: Attempting to finish the match. Then: Match should be finished.")
        void attemptToFinishExistingMatch() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            matchManager.startMatch(homeTeam, awayTeam);

            assertDoesNotThrow(() -> matchManager.finishMatch(homeTeam, awayTeam));
        }

        @Test
        @DisplayName("Given: Valid team names. When: Attempting to finish a match. Then: Match should be finished.")
        void attemptToFinishMatchWithValidTeamNames() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";
            matchManager.startMatch(homeTeam, awayTeam);

            assertDoesNotThrow(() -> matchManager.finishMatch(homeTeam, awayTeam));
        }

        @Test
        @DisplayName("Given: A match that has not been started. When: Attempting to finish the match. Then: MatchNotFoundException should be thrown.")
        void attemptToFinishNonExistingMatch() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";

            assertThrows(MatchNotFoundException.class, () -> matchManager.finishMatch(homeTeam, awayTeam));
        }

        @Test
        @DisplayName("Given: A match that has not been started. When: Attempting to finish the match with reversed team names. Then: MatchNotFoundException should be thrown.")
        void attemptToFinishNonExistingMatchWithReversedTeamNames() {
            String homeTeam = "TeamA";
            String awayTeam = "TeamB";

            assertThrows(MatchNotFoundException.class, () -> matchManager.finishMatch(awayTeam, homeTeam));
        }

        @Test
        @DisplayName("Given: A match that has not been started. When: Attempting to finish the match with different case team names. Then: MatchNotFoundException should be thrown.")
        void attemptToFinishMatchWithDifferentCaseTeamNames() {
            String homeTeam = "Team-a";
            String awayTeam = "Team-b";

            matchManager.startMatch(homeTeam, awayTeam);

            assertDoesNotThrow(() -> matchManager.finishMatch(homeTeam.toUpperCase(), awayTeam.toUpperCase()));
        }

        @Test
        @DisplayName("Given: Null home team name. When: Attempting to finish a match. Then: IllegalArgumentException should be thrown.")
        void attemptToFinishMatchWithNullHomeTeamName() {
            assertThrows(IllegalArgumentException.class, () -> matchManager.finishMatch(null, "TeamB"));
        }

        @Test
        @DisplayName("Given: Null away team name. When: Attempting to finish a match. Then: IllegalArgumentException should be thrown.")
        void attemptToFinishMatchWithNullAwayTeamName() {
            assertThrows(IllegalArgumentException.class, () -> matchManager.finishMatch("TeamA", null));
        }

        @Test
        @DisplayName("Given: Empty home team name. When: Attempting to finish a match. Then: IllegalArgumentException should be thrown.")
        void attemptToFinishMatchWithEmptyHomeTeamName() {
            assertThrows(IllegalArgumentException.class, () -> matchManager.finishMatch("", "TeamB"));
        }

        @Test
        @DisplayName("Given: Empty away team name. When: Attempting to finish a match. Then: IllegalArgumentException should be thrown.")
        void attemptToFinishMatchWithEmptyAwayTeamName() {
            assertThrows(IllegalArgumentException.class, () -> matchManager.finishMatch("TeamA", ""));
        }
    }

}
