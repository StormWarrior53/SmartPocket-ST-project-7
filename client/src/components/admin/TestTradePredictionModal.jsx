import TradePredictionGame from '../games/TradePredictionGame.jsx';
import './TestGameModal.css';

export default function TestTradePredictionModal({ onClose }) {
    return (
        <div className="test-game-modal-overlay">
            <div className="test-game-modal">
                <div className="test-game-header">
                    <h2>Test Trade Prediction Game</h2>
                    <button onClick={onClose} className="btn-close-modal">×</button>
                </div>
                
                <div className="test-game-info">
                    <p>Test the Trade Prediction Game functionality</p>
                </div>

                <div className="test-game-container">
                    <TradePredictionGame testMode={true} testConfig={null} />
                </div>
            </div>
        </div>
    );
}
