package org.cost.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameDataService {

    public Game findGameByGameName(String gameName) {
        return null;
    }

    public boolean gameExistsWithName(String s) {
        return false;
    }

    public void saveGame(Game expectedGame) {
    }

    public void deleteGame(String gameName) {
    }
}
