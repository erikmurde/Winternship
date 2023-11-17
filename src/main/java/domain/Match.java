package domain;

import lombok.*;

import java.util.UUID;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Match {

    @NonNull
    private UUID id;

    @NonNull
    private String outcome;

    private double returnRateA;

    private double returnRateB;
}
