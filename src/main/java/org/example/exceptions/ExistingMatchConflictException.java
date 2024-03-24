package org.example.exceptions;

public class ExistingMatchConflictException extends RuntimeException {

    private String homeTeam;
    private String awayTeam;
    public ExistingMatchConflictException(String homeTeam, String awayTeam) {
        super(String.format("Cannot insert the match since the match between %s and %s exists!", awayTeam, homeTeam));
    }
}
