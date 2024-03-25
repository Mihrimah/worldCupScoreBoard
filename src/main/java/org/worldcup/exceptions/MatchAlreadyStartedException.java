package org.worldcup.exceptions;

public class MatchAlreadyStartedException extends RuntimeException {

    public MatchAlreadyStartedException(String homeTeam, String awayTeam) {
        super(String.format("Cannot start the match since the match between %s and %s has already been started!", homeTeam, awayTeam));
    }
}
