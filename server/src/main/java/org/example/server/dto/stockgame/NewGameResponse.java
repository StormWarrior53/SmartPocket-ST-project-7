package org.example.server.dto.stockgame;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewGameResponse {

    private String symbol;         // символ на акцията (пример: AAPL)
    private String predictionDate; // дата, за която детето ще прогнозира
    private double[] history;      // исторически данни за последните N дни

}
