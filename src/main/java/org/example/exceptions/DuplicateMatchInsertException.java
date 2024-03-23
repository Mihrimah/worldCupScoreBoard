package org.example.exceptions;

public class DuplicateMatchInsertException extends RuntimeException {

    private String homeTeam;
    private String awayTeam;
    public DuplicateMatchInsertException(String homeTeam, String awayTeam) {
        super(String.format("Cannot insert the match since the match between %s and %s exists!", homeTeam, awayTeam));
    }
}
