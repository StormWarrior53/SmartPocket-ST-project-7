-- Create table to store budget game configurations
CREATE TABLE budget_game_configs (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    min_savings_rate DECIMAL(3,2) NOT NULL,
    meal_cost INTEGER NOT NULL,
    min_meals INTEGER NOT NULL,
    min_electricity INTEGER NOT NULL,
    min_water INTEGER NOT NULL,
    min_gas INTEGER NOT NULL,
    min_play_budget INTEGER NOT NULL,
    prize_pocket_money INTEGER NOT NULL,
    event_config TEXT NOT NULL,
    is_active BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Insert default configuration with original hardcoded values
INSERT INTO budget_game_configs (
    id, name, description, min_savings_rate, meal_cost, min_meals,
    min_electricity, min_water, min_gas, min_play_budget, prize_pocket_money,
    event_config, is_active, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'Standard Budget Game',
    'Default configuration matching original hardcoded values',
    0.20,
    5,
    25,
    20,
    15,
    15,
    500,
    25,
    '{"negative":{"probability":0.4,"minAmount":30,"maxAmount":100,"events":[{"title":"Phone screen broke","message":"Repair costs €{amount}"},{"title":"Unexpected bill","message":"Pay €{amount} for fees"}]},"positive":{"probability":0.3,"minAmount":10,"maxAmount":60,"events":[{"title":"Surprise gift","message":"You received €{amount}!"}]},"neutral":{"probability":0.3,"events":[{"title":"Quiet month","message":"No surprises this month"}]}}',
    true,
    NOW(),
    NOW()
);
