package processor;

import domain.Match;
import domain.Player;
import domain.PlayerAction;
import domain.ProcessingResult;

import java.util.*;

public class BettingDataProcessor {

    private final Map<UUID, Player> players = new HashMap<>();
    private List<Match> matches = new ArrayList<>();
    private int actionQueueNr;

    public ProcessingResult processBettingData(String playerData, List<Match> matches) {
        this.matches = matches;

        for (String line : playerData.split("\n")) {
            if (line.isEmpty()) {
                continue;
            }
            String[] values = line.split(",", -1);

            Player player = getPlayer(UUID.fromString(values[0]));

            processPlayerAction(player, values);
        }
        return new ProcessingResult(
                calculateHostBalanceChange(),
                players.values().stream().toList());
    }

    private void processPlayerAction(Player player, String[] values) {
        var action = new PlayerAction(actionQueueNr++, values[1], Integer.parseInt(values[3]));

        switch (action.getActionName()) {
            case "DEPOSIT":
                action.setPlayerBalanceChange(action.getCoinAmount());
                player.setBalance(player.getBalance() + action.getCoinAmount());
                break;
            case "WITHDRAW":
                if (validatePlayerAction(player, action)) {
                    action.setPlayerBalanceChange(action.getCoinAmount() * -1);
                    player.setBalance(player.getBalance() - action.getCoinAmount());
                }
                break;
            case "BET":
                processBetAction(action, player, values);
                break;
            default:
                throw new IllegalArgumentException("%s is not a valid action!".formatted(action.getActionName()));
        }
        player.getPlayerActions().add(action);
    }

    private void processBetAction(PlayerAction action, Player player, String[] values) {
        Match match = findMatchById(UUID.fromString(values[2]));
        String betSide = values[4];

        action.setMatchId(match.getId());
        action.setBetSide(betSide);

        if (!validatePlayerAction(player, action) || match.getOutcome().equals("DRAW")) {
            return;

        } else if (player.hasBetOnMatch(match.getId())) {
            throw new IllegalArgumentException("Player %s already bet on match %s!"
                    .formatted(player.getId(), match.getId()));
        }

        action.setBetWon(match.getOutcome().equals(betSide));

        int balanceChange = calculatePlayerBalanceChange(match, action);

        action.setPlayerBalanceChange(balanceChange);
        player.setBalance(player.getBalance() + balanceChange);
    }

    private boolean validatePlayerAction(Player player, PlayerAction action) {
        if (player.getBalance() >= action.getCoinAmount()) {
            return true;
        } else {
            action.setLegalAction(false);
            player.setLegitimate(false);
            return false;
        }
    }

    private int calculateHostBalanceChange() {
        int balanceChange = 0;

        for (Player player : players.values()) {
            if (!player.isLegitimate()) {
                continue;
            }
            balanceChange += player.getPlayerActions().stream()
                    .filter(a -> a.isLegalAction() && a.getActionName().equals("BET"))
                    .reduce(0, (sum, a) -> sum - a.getPlayerBalanceChange(), Integer::sum);
        }
        return balanceChange;
    }

    private int calculatePlayerBalanceChange(Match match, PlayerAction action) {
        double returnRate = match.getOutcome().equals("A")
                ? match.getReturnRateA()
                : match.getReturnRateB();

        return action.isBetWon()
                ? (int) Math.floor(action.getCoinAmount() * returnRate)
                : action.getCoinAmount() * -1;
    }

    private Player getPlayer(UUID id) {
        if (players.containsKey(id)) {
            return players.get(id);
        } else {
            var player = new Player(id);
            players.put(id, player);

            return player;
        }
    }

    private Match findMatchById(UUID id) {
        return matches.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Match with id %s not found!"
                        .formatted(id)));
    }
}
