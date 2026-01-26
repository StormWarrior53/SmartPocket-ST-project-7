import { render, screen } from "@testing-library/react";
import TradePredictionGame from "../components/games/TradePredictionGame";

describe("TradePredictionGame", () => {
    test("renders game title", () => {
        render(<TradePredictionGame />);
        expect(
            screen.getByText(/Trade Prediction Game/i)
        ).toBeInTheDocument();
    });

    test("renders prediction buttons", () => {
        render(<TradePredictionGame />);
        expect(screen.getByText("📈 UP")).toBeInTheDocument();
        expect(screen.getByText("📉 DOWN")).toBeInTheDocument();
    });
});
