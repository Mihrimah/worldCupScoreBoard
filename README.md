## Football World Cup Scoreboard Library
**Author:** Mihrimah Sultan Yıldırım

### Description
This Java library manages a live score board for Football World Cup matches. It allows users to start new matches, update scores, finish matches, and get a summary of ongoing matches.

### Requirements
**Start a new match:** Assuming initial score 0 – 0 and adding it the scoreboard. This should capture following parameters:
*Home team* and *Away team*

**Update score:** This should receive a pair of absolute scores:
*home team score* and *away team score*

**Finish match currently in progress:** This removes a match from the scoreboard.

**Get a summary of matches in progress ordered by their total score:** The matches with the same total score will be returned ordered by the most recently started match in the scoreboard.

### Setup
- **Java Version:** Java 17 or higher
- **Build Tool:** Gradle
- **Dependencies:** JUnit 5 (for testing)
- **IDE:** IntelliJ IDEA (recommended)

### Implementation
#### Test-Driven Development (TDD)
Start with writing tests for each functionality before implementing them. This includes testing match addition, score updates, match completion, and the summary logic.

#### Design Patterns
- **Strategy Pattern:** Different strategies for generating match keys could be defined and swapped dynamically at runtime based on specific criteria or configurations.

### Notes

- **Match Identification:**
  - Each match is uniquely identified by its home and away team names.
  - Starting a match with the same team as an existing match will result in an exception.
- **Concurrency Considerations:**
  - This library is designed with thread safety in mind for concurrent operations. However, users should ensure that external synchronization is applied when accessing scoreboard operations from multiple threads to prevent race conditions or data inconsistencies.
- **Score Update Rules:**
  - Scores can only be incremented by one goal per update to ensure accuracy in real-time score changes.
  - Scores cannot be negative. Attempts to decrement scores below zero will be ignored and the score remains unchanged.
  - Score updates are not permitted for finished matches. Attempting to update a finished match's score will result in a MatchNotFoundException.
  - There is currently no upper limit enforced on score values. It is assumed that scores will remain within reasonable bounds for a football match.
  - The score of a match can be adjusted by the football referee or other authorized personnel due to various reasons (e.g., referee error, video assistant referee review, offside call, handball, etc.).
- **Match Summary Behavior:**
  - Only ongoing matches are included in the match summary. Finished matches are excluded.
  - The summary is sorted by total score, with ties broken by the most recent match. This ensures that the most competitive and recent matches are listed first.
- **Error Handling:**
  - The library uses specific exceptions to signal various error conditions (e.g., MatchAlreadyStartedException, MatchNotFoundException). It is crucial for callers to handle these exceptions appropriately.

---

Thank you for checking out the Football World Cup Scoreboard Library!

I hope you find it useful and enjoy exploring its features.

Please feel free to provide feedback or suggestions for improvement.