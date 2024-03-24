package org.example;

import java.time.LocalDateTime;

public record Match(String homeTeam, String awayTeam, Score score, LocalDateTime startTime) {

    @Override
    public String toString() {
        return String.format("%s %d - %s %d", homeTeam, score.getHomeScore(), awayTeam, score.getAwayScore());
    }
}
