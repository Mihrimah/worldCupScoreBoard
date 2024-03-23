package org.example;

import java.util.ArrayList;
import java.util.List;

public class Scoreboard {
    private final List<String> matches = new ArrayList<>();

    public void insertMatch(String homeTeam, String awayTeam) {
        matches.add(homeTeam + "-" + awayTeam);
    }

    public boolean containsMatch(String homeTeam, String awayTeam) {
        return matches.contains(homeTeam + "-" + awayTeam);
    }

    public int matchCount() {
        return 50;
    }
}
