package org.cost.game;

import org.cost.Exceptions;
import org.cost.player.PlayerDataService;
import org.cost.player.PlayerTerritory;
import org.cost.territory.TerritoryDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;


public class GameService {

    GameDataService gameDataService;
    PlayerDataService playerDataService;
    TerritoryDataService territoryDataService;

    @Autowired
    public GameService(GameDataService gameDataService, PlayerDataService playerDataService, TerritoryDataService territoryDataService) {
        this.gameDataService = gameDataService;
        this.playerDataService = playerDataService;
        this.territoryDataService = territoryDataService;
    }

    public void createGame(CreateGameRequest gameRequest) throws Exception {

        if (gameDataService.gameExistsWithName(gameRequest.getGameName())) {
            throw new Exceptions.ConflictException("Game Name Taken");
        }
        final ArrayList<PlayerTerritory> playerTerritories = new ArrayList<>();
        territoryDataService.getListOfTerritoriesOnMap()
                .forEach(territory -> {
                    PlayerTerritory playerTerritory = PlayerTerritory.builder()
                            .territoryId(territory.getTerritoryId())
                            .gameName(gameRequest.getGameName())
                            .territoryName(territory.getName())
                            .playerId(null).build();
                    playerTerritories.add(playerTerritory);
                });
        Game game = Game.builder()
                .gameName(gameRequest.getGameName())
                .playerTerritories(playerTerritories)
                .turnNumber(1)
                .build();
        gameDataService.saveGame(game);
    }



    public void deleteGame(String gameName) {
        if (!gameDataService.gameExistsWithName(gameName)) {
            throw new Exceptions.ResourceNotFoundException("Game Does Not Exist");
        }
        gameDataService.deleteGame(gameName);
    }




}
