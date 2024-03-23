package org.example.exceptions;

public class DuplicateMatchStartException extends RuntimeException {

    private String homeTeam;
    private String awayTeam;
    public DuplicateMatchStartException(String homeTeam, String awayTeam) {
        super(String.format("Cannot start the match since the match between %s and %s has already been started!", homeTeam, awayTeam));
    }
}
