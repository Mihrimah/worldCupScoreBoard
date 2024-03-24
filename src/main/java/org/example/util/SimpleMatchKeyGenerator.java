package org.example.util;

public class SimpleMatchKeyGenerator implements MatchKeyGenerator{
    @Override
    public String generateKey(String homeTeam, String awayTeam) {
        return homeTeam + " vs " + awayTeam;
    }
}
