CREATE TABLE Player (
    player_id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    game_name VARCHAR(64) NOT NULL,
    player_name VARCHAR(64) NOT NULL,
    FOREIGN KEY (game_name) REFERENCES Game(game_name)
)
