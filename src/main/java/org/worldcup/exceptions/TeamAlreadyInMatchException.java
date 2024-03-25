package org.worldcup.exceptions;

public class TeamAlreadyInMatchException extends RuntimeException {

    public TeamAlreadyInMatchException(String homeTeam, String awayTeam) {
        super(String.format("Cannot start the match since the team %s or %s is already in a match!", homeTeam, awayTeam));
    }
}
