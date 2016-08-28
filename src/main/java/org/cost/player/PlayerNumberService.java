package org.cost.player;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerNumberService {
    public int getNextPlayerNumber(int currentPlayerNumber) {
        return (currentPlayerNumber < PlayerController.MAX_PLAYERS_ALLOWED_IN_GAME ? currentPlayerNumber + 1 : -1);
    }

    public int getNumberOfPlayersInGame(List<Player> players) {
        if (players.size() == 1 && players.get(0) == null) {
            return 0;
        }
        return players.size();
    }
}
