package processor;

import domain.Match;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchDataProcessor {

    public static List<Match> processMatchData(String data) {
        List<Match> matches = new ArrayList<>();

        for (String line : data.split("\n")) {
            if (line.isEmpty()) {
                continue;
            }
            matches.add(createMatch(line));
        }
        return matches;
    }

    private static Match createMatch(String line) {
        String[] values = line.split(",");

        UUID id = UUID.fromString(values[0]);
        String outcome = values[3];
        double returnRateA = Double.parseDouble(values[1]);
        double returnRateB = Double.parseDouble(values[2]);

        return validateMatch(new Match(id, outcome, returnRateA, returnRateB));
    }

    private static Match validateMatch(Match match) {
        if (!List.of("A", "B", "DRAW").contains(match.getOutcome())) {
            throw new IllegalArgumentException("%s is not a valid match outcome!"
                    .formatted(match.getOutcome()));
        }
        return match;
    }
}
