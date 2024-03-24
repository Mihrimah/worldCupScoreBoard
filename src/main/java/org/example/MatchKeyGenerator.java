package org.example;

/**
 * Generates a key for a match between two teams.
 * The key is used to identify a match uniquely.
 */
public interface MatchKeyGenerator {
    String generateKey(String homeTeam, String awayTeam);
}
