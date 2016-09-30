CREATE TABLE Player_territory (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT UNSIGNED,
    territory_id BIGINT,
    FOREIGN KEY (player_id) REFERENCES Player(player_id),
    FOREIGN KEY (territory_id) REFERENCES Territory(territory_id)
)
