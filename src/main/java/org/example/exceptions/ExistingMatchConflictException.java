package org.example.exceptions;

public class ExistingMatchConflictException extends RuntimeException {

    public ExistingMatchConflictException(String homeTeam, String awayTeam) {
        super(String.format("There is already a match with the same teams: %s vs %s", homeTeam, awayTeam));
    }
}
