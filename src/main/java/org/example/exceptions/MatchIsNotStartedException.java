package org.example.exceptions;

public class MatchIsNotStartedException extends RuntimeException {
    private String homeTeam;
    private String awayTeam;

    public MatchIsNotStartedException(String homeTeam, String awayTeam) {
        super(String.format("The match(%s-%s) cannot be found!", homeTeam, awayTeam));
    }
}