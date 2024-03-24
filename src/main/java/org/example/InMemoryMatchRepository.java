package org.example;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an in-memory repository of matches.
 * Because it uses a ConcurrentHashMap, it is thread-safe.
 */
public class InMemoryMatchRepository implements MatchRepository {
    private final ConcurrentHashMap<String, Match> matches = new ConcurrentHashMap<>();

    @Override
    public void addMatch(String key, Match match) {
        matches.put(key, match);
    }

    @Override
    public void removeMatch(String key) {
        matches.remove(key);
    }

    @Override
    public Match getMatch(String key) {
        return matches.get(key);
    }

    @Override
    public boolean containsMatch(String key) {
        return matches.containsKey(key);
    }

    @Override
    public int countMatches() {
        return matches.size();
    }

    @Override
    public boolean isTeamInAnyMatch(String teamName) {
        return matches.values().stream()
                .anyMatch(match -> match.homeTeam().equals(teamName) || match.awayTeam().equals(teamName));
    }

    @Override
    public Collection<Match> getAllMatches() {
        return matches.values();
    }
}
