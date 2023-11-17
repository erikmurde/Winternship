package formatter;

import domain.Player;
import domain.PlayerAction;
import domain.ProcessingResult;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class BettingDataFormatter {

    public static String formatData(ProcessingResult data) {
        List<Player> players = data.getPlayers();
        var builder = new StringBuilder();

        formatLegitimatePlayers(builder, players);
        formatIllegitimatePlayers(builder, players);

        builder.append(data.getHostBalanceChange());

        return builder.toString();
    }

    private static void formatLegitimatePlayers(StringBuilder builder, List<Player> players) {
        List<Player> legitimatePlayers = filterAndSortPlayers(players, p -> p.isLegitimate());

        if (legitimatePlayers.isEmpty()) {
            builder.append("\n");
        }

        var df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.GERMANY));

        for (Player player : legitimatePlayers) {
            builder.append("%s %d %s\n"
                    .formatted(player.getId(), player.getBalance(), df.format(player.getWinRate())));
        }
        builder.append("\n");
    }

    private static void formatIllegitimatePlayers(StringBuilder builder, List<Player> players) {
        List<Player> illegitimatePlayers = filterAndSortPlayers(players, p -> !p.isLegitimate());

        if (illegitimatePlayers.isEmpty()) {
            builder.append("\n");
        }

        for (Player player : illegitimatePlayers) {
            PlayerAction action = getEarliestIllegalAction(player);

            builder.append("%s %s %s %d %s\n"
                    .formatted(player.getId(), action.getActionName(), action.getMatchId(),
                            action.getCoinAmount(), action.getBetSide()));
        }
        builder.append("\n");
    }

    private static List<Player> filterAndSortPlayers(List<Player> players, Predicate<Player> filter) {
        return players.stream()
                .filter(filter)
                .sorted(Comparator.comparing(Player::getId))
                .toList();
    }

    private static PlayerAction getEarliestIllegalAction(Player player) {
        return player.getPlayerActions().stream()
                .filter(a -> !a.isLegalAction())
                .min(Comparator.comparing(PlayerAction::getQueueNr))
                .orElseThrow(() -> new RuntimeException(
                        "Illegitimate player %s has no illegal actions!".formatted(player.getId())));
    }
}
