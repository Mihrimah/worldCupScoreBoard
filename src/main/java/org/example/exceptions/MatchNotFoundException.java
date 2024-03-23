package org.example.exceptions;

public class MatchNotFoundException extends RuntimeException {
    private String homeTeam;
    private String awayTeam;

    public MatchNotFoundException(String homeTeam, String awayTeam) {
        super(String.format("The match(%s-%s) cannot be found!", homeTeam, awayTeam));
    }
}
