package domain;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Data
@RequiredArgsConstructor
public class Player {

    @NonNull
    private UUID id;

    private long balance;

    private boolean isLegitimate = true;

    private List<PlayerAction> playerActions = new ArrayList<>();

    public boolean hasBetOnMatch(UUID matchId) {
        return getPlayerActions().stream()
                .anyMatch(a -> matchId.equals(a.getMatchId()));
    }

    public BigDecimal getWinRate() {
        int betsPlaced = getActionCount(a -> a.getActionName().equals("BET"));
        int gamesWon = getActionCount(a -> a.isBetWon());

        return BigDecimal
                .valueOf(betsPlaced > 0 ? (double) gamesWon / betsPlaced : 0)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private int getActionCount(Predicate<PlayerAction> filter) {
        return playerActions.stream()
                .filter(filter.and(a -> a.isLegalAction()))
                .toList()
                .size();
    }
}
