package org.example.server.dto.stockgame;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewGameRequest {

    private double startingBalance;     // начален баланс
    private int durationDays;           // продължителност на играта
    private String difficulty;          // ниво на трудност (EASY / MEDIUM / HARD)

}
