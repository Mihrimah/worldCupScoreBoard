package org.example.exceptions;

public class MatchNotStartedException extends RuntimeException {
    private String homeTeam;
    private String awayTeam;

    public MatchNotStartedException(String homeTeam, String awayTeam) {
        super(String.format("The match between %s and %s is not started!", homeTeam, awayTeam));
    }
}