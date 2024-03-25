package org.worldcup.repository;

import org.worldcup.model.Match;

import java.util.Collection;

/**
 * Represents a repository of matches.
 * It contains methods to add, remove, get, and check the existence of a match.
 */
public interface MatchRepository {
    void addMatch(String key, Match match);
    void removeMatch(String key);
    Match getMatch(String key);
    boolean containsMatch(String key);
    int countMatches();
    boolean isTeamInAnyMatch(String teamName);
    Collection<Match> getAllMatches();
}
