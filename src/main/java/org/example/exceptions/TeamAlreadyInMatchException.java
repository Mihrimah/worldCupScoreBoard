package org.example.exceptions;

public class TeamAlreadyInMatchException extends RuntimeException {

    private String homeTeam;
    private String awayTeam;
    public TeamAlreadyInMatchException(String homeTeam, String awayTeam) {
        super(String.format("Please ensure that both teams are not involved in any ongoing matches before attempting to start or insert a new one!", homeTeam, awayTeam));
    }
}
