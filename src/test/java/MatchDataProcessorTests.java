import domain.Match;
import processor.MatchDataProcessor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static constants.TestConstants.*;

public class MatchDataProcessorTests {

    @Test
    public void testReturnsCorrectMatch() {
        String match = "%s,1.45,0.75,A".formatted(MATCH_ID);

        Match expected = new Match(
                UUID.fromString(MATCH_ID), "A", 1.45, 0.75);

        List<Match> result = MatchDataProcessor.processMatchData(match);

        assertEquals(1, result.size());
        assertEquals(result.get(0), expected);
    }

    @Test
    public void testIgnoresEmptyLines() {
        String match = "\n\n%s,1.45,0.75,A\n".formatted(MATCH_ID);

        List<Match> result = MatchDataProcessor.processMatchData(match);

        assertEquals(1, result.size());
    }

    @Test
    public void testThrowsExceptionWhenInvalidOutcome() {
        String match = "%s,1.45,0.75,INVALID".formatted(MATCH_ID);

        assertThrows(
                IllegalArgumentException.class,
                () -> MatchDataProcessor.processMatchData(match));
    }
}
