package org.example.exceptions;

public class MatchNotFoundException extends RuntimeException {

    public MatchNotFoundException(String homeTeam, String awayTeam) {
        super(String.format("The match between %s and %s cannot be found!", homeTeam, awayTeam));
    }
}
