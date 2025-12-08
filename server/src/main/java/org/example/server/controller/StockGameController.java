package org.example.server.controller;

import org.example.server.dto.stockgame.NewGameResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/games/stock-prediction")
public class StockGameController {

    @GetMapping("/new")
    public NewGameResponse createNewGame() {

        NewGameResponse response = new NewGameResponse();
        response.setSymbol("AAPL"); // примерна акция
        response.setPredictionDate("2025-01-01"); // примерна дата
        response.setHistory(new double[]{120.5, 121.0, 119.8, 122.3}); // примерни данни

        return response;
    }
}
