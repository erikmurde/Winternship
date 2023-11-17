import domain.Match;
import domain.Player;
import domain.ProcessingResult;
import formatter.BettingDataFormatter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static factory.TestDataFactory.*;
import static constants.TestConstants.*;

public class BettingDataFormatterTests {

    @Test
    public void testHasOnlyHostBalanceChange() {
        String expected = "\n\n\n\n0";

        String result = BettingDataFormatter
                .formatData(new ProcessingResult(0, new ArrayList<>()));

        assertEquals(expected, result);
    }

    @Test
    public void testHasOnlyLegitimatePlayers() {
        String expected = "%s 2000 0,00\n\n\n\n0".formatted(PLAYER1_ID);

        Player player = getLegitimatePlayer(PLAYER1_ID);

        String result = BettingDataFormatter
                .formatData(new ProcessingResult(0, List.of(player)));

        assertEquals(expected, result);
    }

    @Test
    public void testHasOnlyIllegitimatePlayers() {
        String expected = "\n\n%s BET %s 5000 A\n\n0".formatted(PLAYER2_ID, MATCH_ID);

        Player player = getIllegitimatePlayer(PLAYER2_ID);

        String result = BettingDataFormatter
                .formatData(new ProcessingResult(0, List.of(player)));

        assertEquals(expected, result);
    }

    @Test
    public void testHasAllGroups() {
        String expected = "%s 2000 0,00\n\n%s BET %s 5000 A\n\n0"
                .formatted(PLAYER1_ID, PLAYER2_ID, MATCH_ID);

        Player p1 = getLegitimatePlayer(PLAYER1_ID);
        Player p2 = getIllegitimatePlayer(PLAYER2_ID);

        String result = BettingDataFormatter
                .formatData(new ProcessingResult(0, List.of(p1, p2)));

        assertEquals(expected, result);
    }

    @Test
    public void testAddsNullWhenValueIsMissing() {
        String expected = "\n\n%s WITHDRAW null 5000 null\n\n0".formatted(PLAYER2_ID);

        Player player = new Player(UUID.fromString(PLAYER2_ID));

        addWithdraw(player, false);

        String result = BettingDataFormatter
                .formatData(new ProcessingResult(0, List.of(player)));

        assertEquals(expected, result);
    }

    @Test
    public void testShowsOnlyFirstIllegalAction() {
        String expected = "\n\n%s WITHDRAW null 5000 null\n\n0".formatted(PLAYER2_ID);

        Player player = getIllegitimatePlayer(PLAYER2_ID);
        player.getPlayerActions().get(0).setQueueNr(2);

        addWithdraw(player, false);

        String result = BettingDataFormatter.formatData(
                new ProcessingResult(0, List.of(player)));

        assertEquals(expected, result);
    }

    @Test
    public void testLegitimatePlayersAreSortedById() {
        String expected = "%s 2000 0,00\n%s 2000 0,00\n\n\n\n0".formatted(PLAYER1_ID, PLAYER2_ID);

        Player p1 = getLegitimatePlayer(PLAYER1_ID);
        Player p2 = getLegitimatePlayer(PLAYER2_ID);

        String result = BettingDataFormatter
                .formatData(new ProcessingResult(0, List.of(p1, p2)));

        assertEquals(expected, result);
    }

    @Test
    public void testIllegitimatePlayersAreSortedById() {
        String expected = "\n\n%s BET %s 5000 A\n%s BET %s 5000 A\n\n0"
                .formatted(PLAYER1_ID, MATCH_ID, PLAYER2_ID, MATCH_ID);

        Player p1 = getIllegitimatePlayer(PLAYER1_ID);
        Player p2 = getIllegitimatePlayer(PLAYER2_ID);

        String result = BettingDataFormatter
                .formatData(new ProcessingResult(0, List.of(p1, p2)));

        assertEquals(expected, result);
    }

    private Player getLegitimatePlayer(String id) {
        Player player = new Player(UUID.fromString(id));

        addDeposit(player, 0);

        return player;
    }

    private Player getIllegitimatePlayer(String id) {
        Player player = new Player(UUID.fromString(id));
        Match match = createMatch("A");

        addBet(player, match, "A", false);

        return player;
    }
}
