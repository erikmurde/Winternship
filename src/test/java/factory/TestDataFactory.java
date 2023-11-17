package factory;

import domain.Match;
import domain.Player;
import domain.PlayerAction;

import java.util.UUID;

/**
 * Class for creating test data with mostly hardcoded values
 */
public class TestDataFactory {

    public static void addDeposit(Player player, int queueNr) {
        var deposit = new PlayerAction(queueNr, "DEPOSIT", 2000);

        deposit.setPlayerBalanceChange(2000);

        player.getPlayerActions().add(deposit);
        player.setBalance(player.getBalance() + 2000);
    }

    public static void addWithdraw(Player player, boolean isLegal) {
        var withdraw = new PlayerAction(1, "WITHDRAW", 0);

        if (isLegal) {
            withdraw.setCoinAmount(1000);
            withdraw.setPlayerBalanceChange(-1000);

            player.setBalance(player.getBalance() - 1000);
        } else {
            withdraw.setCoinAmount(5000);
            withdraw.setLegalAction(false);

            player.setLegitimate(false);
        }
        player.getPlayerActions().add(withdraw);
    }

    public static void addBet(Player player, Match match, String betSide, boolean isLegal) {
        var bet = new PlayerAction(1, "BET", 0);

        bet.setMatchId(match.getId());
        bet.setBetSide(betSide);

        if (isLegal) {
            addLegalBet(player, bet, match);
        } else {
            addIllegalBet(player, bet);
        }
    }

    private static void addLegalBet(Player player, PlayerAction bet, Match match) {
        bet.setCoinAmount(1);

        if (!match.getOutcome().equals("DRAW")) {
            bet.setBetWon(match.getOutcome().equals(bet.getBetSide()));

            int balanceChange = bet.isBetWon() ? 3 : -1;

            bet.setPlayerBalanceChange(balanceChange);
            player.setBalance(player.getBalance() + balanceChange);
        }
        player.getPlayerActions().add(bet);
    }

    private static void addIllegalBet(Player player, PlayerAction bet) {
        bet.setCoinAmount(5000);
        bet.setLegalAction(false);

        player.setLegitimate(false);
        player.getPlayerActions().add(bet);
    }

    public static Match createMatch(String outcome) {
        var match = new Match(UUID.fromString("abae2255-4255-4304-8589-737cdff61640"), outcome);

        match.setReturnRateA(3.9);
        match.setReturnRateB(5);

        return match;
    }
}
