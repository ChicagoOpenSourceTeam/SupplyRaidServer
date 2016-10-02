ALTER TABLE Player_territory ADD game_name VARCHAR(64) NOT NULL;
ALTER TABLE Player_territory ADD CONSTRAINT fk_game_name FOREIGN KEY (game_name) REFERENCES game(game_name);