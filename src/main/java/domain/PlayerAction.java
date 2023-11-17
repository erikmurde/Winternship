package domain;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PlayerAction {

    private int queueNr;

    private String actionName;

    private int coinAmount;

    private int playerBalanceChange;

    private boolean isLegalAction = true;

    // Following values are used if the player bet on a match

    private UUID matchId;

    private String betSide;

    private boolean betWon;

    public PlayerAction(int queueNr, String actionName, int coinAmount) {
        this.queueNr = queueNr;
        this.actionName = actionName;
        this.coinAmount = coinAmount;
    }
}
