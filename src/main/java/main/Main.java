package main;

import domain.Match;
import domain.ProcessingResult;
import formatter.BettingDataFormatter;
import processor.BettingDataProcessor;
import processor.MatchDataProcessor;
import util.FileUtil;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        String matchData = FileUtil.readResourceFile("match_data.txt");
        String playerData = FileUtil.readResourceFile("player_data.txt");

        // Create list of matches
        List<Match> matches = MatchDataProcessor
                .processMatchData(matchData);

        // Create list of players with actions and calculate host balance change
        ProcessingResult result = new BettingDataProcessor()
                .processBettingData(playerData, matches);

        // Create string to write to file
        String resultText = BettingDataFormatter.formatData(result);

        FileUtil.writeToFile(resultText, "result.txt");
    }
}
