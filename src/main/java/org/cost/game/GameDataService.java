package org.cost.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameDataService {

    private GameRepository gameRepository;

    @Autowired
    public GameDataService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }


    public Game findGameByName(String gameName) {
        return gameRepository.getOne(gameName);
    }

    public boolean gameExistsWithName(String s) {
        return false;
    }

    public void saveGame(Game expectedGame) {
    }

    public void deleteGame(String gameName) {

    }
}
