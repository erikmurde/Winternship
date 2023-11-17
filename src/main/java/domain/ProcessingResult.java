package domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@AllArgsConstructor
public class ProcessingResult {

    private long hostBalanceChange;

    @NonNull
    private List<Player> players;
}
