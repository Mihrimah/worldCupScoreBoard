package org.worldcup.util;

import org.worldcup.model.Match;
import org.worldcup.repository.MatchRepository;

import java.util.List;

/**
 * This class generates a summary of all matches.
 */
public class MatchSummaryGenerator {

    private final MatchRepository matchRepository;

    public MatchSummaryGenerator(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }
    
    /**
     * Gets the summary of all matches.
     * The summary is sorted by the total score of the match in descending order.
     * If two matches have the same total score, the match that started later will be placed first.
     *
     * @return a list of strings representing the summary of all matches
     */
    public List<String> getSummary(){
        return matchRepository.getAllMatches().stream()
                .sorted(this::compareMatches)
                .map(Match::toString)
                .toList();
    }

    /**
     * Compares two matches based on their total score and start time.
     *
     * @param match1 the first match
     * @param match2 the second match
     * @return a negative integer, zero, or a positive integer if the first match is less than, equal to, or greater than the second match
     */
    private int compareMatches(Match match1, Match match2) {
        int totalScore1 = match1.score().getHomeScore() + match1.score().getAwayScore();
        int totalScore2 = match2.score().getHomeScore() + match2.score().getAwayScore();
        int scoreComparison = Integer.compare(totalScore2, totalScore1);
        if (scoreComparison != 0) {
            // Return the comparison based on total score if they're not equal
            return scoreComparison;
        } else {
            return 1; // Return 1 if the total score is equal to prioritize the match that started later
        }
    }

}
