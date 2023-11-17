import domain.Match;
import domain.Player;
import domain.PlayerAction;
import domain.ProcessingResult;
import org.junit.jupiter.api.Test;
import processor.BettingDataProcessor;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static factory.TestDataFactory.*;
import static constants.TestConstants.*;

public class BettingDataProcessorTests {

    private static final String P1_DEPOSIT = "%s,DEPOSIT,,2000,".formatted(PLAYER1_ID);
    private static final String P2_DEPOSIT = "%s,DEPOSIT,,2000,".formatted(PLAYER2_ID);

    @Test
    public void testDeposit() {
        Player player = new Player(UUID.fromString(PLAYER1_ID));

        addDeposit(player, 0);

        List<Player> result = getResult(P1_DEPOSIT).getPlayers();

        assertEquals(1, result.size());
        assertEquals("", comparePlayerToExpected(player, result.get(0)));
    }

    @Test
    public void testLegalWithdraw() {
        Player player = new Player(UUID.fromString(PLAYER1_ID));

        addDeposit(player, 0);
        addWithdraw(player, true);

        String withdraw = "%s,WITHDRAW,,1000,".formatted(PLAYER1_ID);

        List<Player> result = getResult(
                join(P1_DEPOSIT, withdraw)).getPlayers();

        assertEquals(1, result.size());
        assertEquals("", comparePlayerToExpected(player, result.get(0)));
    }

    @Test
    public void testIllegalWithdraw() {
        Player player = new Player(UUID.fromString(PLAYER1_ID));

        addDeposit(player, 0);
        addWithdraw(player, false);

        String withdraw = "%s,WITHDRAW,,5000,".formatted(PLAYER1_ID);

        List<Player> result = getResult(
                join(P1_DEPOSIT, withdraw)).getPlayers();

        assertEquals(1, result.size());
        assertEquals("", comparePlayerToExpected(player, result.get(0)));
    }

    @Test
    public void testLegalBetWin() {
        Player player = new Player(UUID.fromString(PLAYER1_ID));
        Match match = createMatch("A");

        addDeposit(player, 0);
        addBet(player, match, "A", true);

        String bet = "%s,BET,%s,1,A".formatted(PLAYER1_ID, MATCH_ID);

        List<Player> result = getResult(
                join(P1_DEPOSIT, bet), List.of(match)).getPlayers();

        assertEquals(1, result.size());
        assertEquals("", comparePlayerToExpected(player, result.get(0)));
    }

    @Test
    public void testLegalBetLose() {
        Player player = new Player(UUID.fromString(PLAYER1_ID));
        Match match = createMatch("A");

        addDeposit(player, 0);
        addBet(player, match, "B", true);

        String bet = "%s,BET,%s,1,B".formatted(PLAYER1_ID, MATCH_ID);

        List<Player> result = getResult(
                join(P1_DEPOSIT, bet), List.of(match)).getPlayers();

        assertEquals(1, result.size());
        assertEquals("", comparePlayerToExpected(player, result.get(0)));
    }

    @Test
    public void testLegalBetDraw() {
        Player player = new Player(UUID.fromString(PLAYER1_ID));
        Match match = createMatch("DRAW");

        addDeposit(player, 0);
        addBet(player, match, "A", true);

        String bet = "%s,BET,%s,1,A".formatted(PLAYER1_ID, MATCH_ID);

        List<Player> result = getResult(
                join(P1_DEPOSIT, bet), List.of(match)).getPlayers();

        assertEquals(1, result.size());
        assertEquals("", comparePlayerToExpected(player, result.get(0)));
    }

    @Test
    public void testIllegalBet() {
        Player player = new Player(UUID.fromString(PLAYER1_ID));
        Match match = createMatch("A");

        addDeposit(player, 0);
        addBet(player, match, "A", false);

        String bet = "%s,BET,%s,5000,A".formatted(PLAYER1_ID, MATCH_ID);

        List<Player> result = getResult(
                join(P1_DEPOSIT, bet), List.of(match)).getPlayers();

        assertEquals(1, result.size());
        assertEquals("", comparePlayerToExpected(player, result.get(0)));
    }

    @Test
    public void testReturnsAllPlayers() {
        Player p1 = new Player(UUID.fromString(PLAYER1_ID));
        Player p2 = new Player(UUID.fromString(PLAYER2_ID));

        addDeposit(p1, 0);
        addDeposit(p2, 1);

        List<Player> result = getResult(join(P1_DEPOSIT, P2_DEPOSIT))
                .getPlayers().stream()
                .sorted(Comparator.comparing(Player::getId))
                .toList();

        assertEquals(2, result.size());

        assertEquals("", comparePlayerToExpected(p1, result.get(0)));
        assertEquals("", comparePlayerToExpected(p2, result.get(1)));
    }

    @Test
    public void testAddsAllActionsToPlayer() {
        Player player = new Player(UUID.fromString(PLAYER1_ID));

        addDeposit(player, 0);
        addDeposit(player, 1);
        addDeposit(player, 2);

        List<Player> result = getResult(
                join(P1_DEPOSIT, P1_DEPOSIT, P1_DEPOSIT)).getPlayers();

        assertEquals(1, result.size());
        assertEquals("", comparePlayerToExpected(player, result.get(0)));
    }

    @Test
    public void testIgnoresEmptyLines() {
        Player player = new Player(UUID.fromString(PLAYER1_ID));

        addDeposit(player, 0);

        String input = "\n\n%s\n".formatted(P1_DEPOSIT);

        List<Player> result = getResult(input).getPlayers();

        assertEquals(1, result.size());
        assertEquals("", comparePlayerToExpected(player, result.get(0)));
    }

    @Test
    public void testThrowsExceptionWhenMatchNotFound() {
        String bet = "%s,BET,NOTFOUND,1,A".formatted(PLAYER1_ID);

        assertThrows(
                IllegalArgumentException.class,
                () -> getResult(bet));
    }

    @Test
    public void testThrowsExceptionWhenBettingOnSameMatchTwice() {
        String bet = "%s,BET,%s,1,A".formatted(PLAYER1_ID, MATCH_ID);

        assertThrows(
                IllegalArgumentException.class,
                () -> getResult(join(bet, bet)));
    }

    @Test
    public void testThrowsExceptionWhenInvalidActionName() {
        String invalid = "%s,INVALID,,1000,".formatted(PLAYER1_ID);

        assertThrows(
                IllegalArgumentException.class,
                () -> getResult(invalid));
    }

    @Test
    public void testOnlyBetsAffectHostBalanceChange() {
        Match match = createMatch("A");

        String bet1 = "%s,BET,%s,10,A".formatted(PLAYER1_ID, MATCH_ID);
        String withdraw = "%s,WITHDRAW,,1000,".formatted(PLAYER1_ID);
        String bet2 = "%s,BET,%s,100,B".formatted(PLAYER2_ID, MATCH_ID);

        long result = getResult(
                join(P1_DEPOSIT, P2_DEPOSIT, bet1, withdraw, bet2), List.of(match)).getHostBalanceChange();

        assertEquals(61, result);
    }

    @Test
    public void testIllegalActionsDoNotEffectHostBalanceChange() {
        Match match = createMatch("A");

        String bet = "%s,BET,%s,5000,A".formatted(PLAYER1_ID, MATCH_ID);

        long result = getResult(
                join(P1_DEPOSIT, bet), List.of(match)).getHostBalanceChange();

        assertEquals(0, result);
    }

    @Test
    public void testIllegitimatePlayerDoesNotEffectHostBalanceChange() {
        Match match = createMatch("A");

        String bet1 = "%s,BET,%s,10,A".formatted(PLAYER1_ID, MATCH_ID);
        String bet2 = "%s,BET,%s,100,A".formatted(PLAYER2_ID, MATCH_ID);
        String bet3 = "%s,BET,%s,5000,A".formatted(PLAYER2_ID, MATCH_ID);

        long result = getResult(
                join(P1_DEPOSIT, P2_DEPOSIT, bet1, bet2, bet3), List.of(match)).getHostBalanceChange();

        assertEquals(-39, result);
    }

    private String comparePlayerToExpected(Player expected, Player actual) {
        if (!expected.getId().equals(actual.getId())) {
            return "Id should be %s, but was %s"
                    .formatted(expected.getId(), actual.getId());

        } else if (expected.getBalance() != actual.getBalance()) {
            return "Balance should be %d, but was %d"
                    .formatted(expected.getBalance(), actual.getBalance());

        } else if (expected.isLegitimate() != actual.isLegitimate()) {
            return "IsLegitimate should be %b, but was %b"
                    .formatted(expected.isLegitimate(), actual.isLegitimate());

        } else if (expected.getPlayerActions().size() != actual.getPlayerActions().size()) {
            return "Size of actions should be %d, but was %d"
                    .formatted(expected.getPlayerActions().size(), actual.getPlayerActions().size());
        }
        for (int i = 0; i < expected.getPlayerActions().size(); i++) {
            String result = comparePlayerActionToExpected(
                    expected.getPlayerActions().get(i), actual.getPlayerActions().get(i));

            if (!result.isEmpty()) {
                return result;
            }
        }
        return "";
    }

    private String comparePlayerActionToExpected(PlayerAction expected, PlayerAction actual) {
        try {
            for (Field field : PlayerAction.class.getDeclaredFields()) {
                field.setAccessible(true);

                String expectedValue = field.get(expected) == null ? "" : field.get(expected).toString();
                String actualValue = field.get(actual) == null ? "" : field.get(expected).toString();

                if (!expectedValue.equals(actualValue)) {
                    return "%s should be %s, but was %s"
                            .formatted(field.getName(), expectedValue, actualValue);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    private ProcessingResult getResult(String input) {
        return getResult(input, new ArrayList<>());
    }

    private ProcessingResult getResult(String input, List<Match> matches) {
        return new BettingDataProcessor()
                .processBettingData(input, matches);
    }

    private String join(String... args) {
        return String.join("\n", args);
    }
}
