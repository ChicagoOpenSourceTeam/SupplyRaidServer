package org.cost.game;

import org.cost.Exceptions;
import org.cost.player.*;
import org.cost.territory.TerritoryDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;


public class GameService {

    GameDataService gameDataService;
    PlayerDataService playerDataService;
    TerritoryDataService territoryDataService;
    PlayerTerritoryDataService playerTerritoryDataService;

    @Autowired
    public GameService(GameDataService gameDataService, PlayerDataService playerDataService, TerritoryDataService territoryDataService, PlayerTerritoryDataService playerTerritoryDataService) {
        this.gameDataService = gameDataService;
        this.playerDataService = playerDataService;
        this.territoryDataService = territoryDataService;
        this.playerTerritoryDataService = playerTerritoryDataService;
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


    public GameResponse checkIfGameHasStarted(HttpSession httpSession) {
        String gameName = (String) httpSession.getAttribute(PlayerController.SESSION_GAME_NAME_FIELD);
        Game game = gameDataService.findGameByName(gameName);
        if (game.isStarted()) {
            return GameResponse.builder().gameStarted(true).build();
        } else {
            return GameResponse.builder().gameStarted(false).build();
        }

    }

    public void startGame(HttpSession httpSession) throws Exceptions.ResourceNotFoundException{

        String gameName = (String) httpSession.getAttribute(PlayerController.SESSION_GAME_NAME_FIELD);
        Game game = gameDataService.findGameByName(gameName);
        if (game == null) {
            throw new Exceptions.ResourceNotFoundException("Game Not Found");
        }
        //
//        if (game.isStarted()) {
//            return new ResponseEntity(HttpStatus.CONFLICT);
//        }
//
//        int numberOfPlayers = game.getPlayers().size();
//        if (numberOfPlayers >= 2) {
//            List<Long> allTerritoryIds = new ArrayList<>();
//
//            List<PlayerTerritory> playerTerritories = playerTerritoryRepository.findByGameName(gameName);
//
//            List<StartingLocation> startingLocations = territoryRepository.findAll()
//                    .stream()
//                    .filter(territory -> territory.getSupply() != 0 && territory.getSupply() <= numberOfPlayers)
//                    .map(territory ->  {
//                        StartingLocation.StartingLocationBuilder builder = StartingLocation.builder();
//                        builder.supplyDepot(playerTerritories.stream()
//                                .filter(pt -> pt.getTerritoryId().equals(territory.getTerritoryId()))
//                                .findFirst()
//                                .get());
//
//                        builder.surroundingTerritories(
//                                Arrays.asList(territory.getWest(), territory.getEast(), territory.getSouth(), territory.getNorth())
//                                        .stream()
//                                        .map(id -> playerTerritories
//                                                .stream()
//                                                .filter(pt -> pt.getTerritoryId().equals(id))
//                                                .findFirst())
//                                        .filter(Optional::isPresent)
//                                        .map(Optional::get)
//                                        .collect(Collectors.toList()));
//
//                        return builder.build();
//
//                    })
//                    .collect(Collectors.toList());
//
//            long seed = System.nanoTime();
//            Collections.shuffle(startingLocations, new Random(seed));
//
//            ArrayList<PlayerTerritory> savedPlayerTerritories = new ArrayList<>();
//            for (Player player : game.getPlayers()) {
//                StartingLocation startingLocation = startingLocations.remove(0);
//                startingLocation.getSupplyDepot().setPlayerId(player.getPlayerId());
//                startingLocation.getSupplyDepot().setTroops(8);
//                startingLocation.getSupplyDepot().setSupplyDepotTerritory(true);
//                startingLocation.getSupplyDepot().setPlayer(player);
//                savedPlayerTerritories.add(startingLocation.getSupplyDepot());
//
//                int numberOfSurroundingTerritories = (int)startingLocation.
//                        getSurroundingTerritories().stream().filter(t -> t != null).count();
//                int troopsPerSurroundingTerritory = 12/numberOfSurroundingTerritories;
//
//                initializeTroopsForTerritoriesAdjacentToSupplyDepots(savedPlayerTerritories, player,
//                        startingLocation, troopsPerSurroundingTerritory);
//
//                startingLocation = startingLocations.remove(0);
//                startingLocation.getSupplyDepot().setPlayerId(player.getPlayerId());
//                startingLocation.getSupplyDepot().setTroops(8);
//                startingLocation.getSupplyDepot().setSupplyDepotTerritory(true);
//                startingLocation.getSupplyDepot().setPlayer(player);
//                savedPlayerTerritories.add(startingLocation.getSupplyDepot());
//
//                numberOfSurroundingTerritories = (int)startingLocation.
//                        getSurroundingTerritories().stream().filter(t -> t != null).count();
//                troopsPerSurroundingTerritory = 12/numberOfSurroundingTerritories;
//
//                initializeTroopsForTerritoriesAdjacentToSupplyDepots(savedPlayerTerritories, player,
//                        startingLocation, troopsPerSurroundingTerritory);
//            }
//            playerTerritoryRepository.save(savedPlayerTerritories);
//            suppliedStatusService.markUnsupplied();
//            suppliedStatusService.markSupplied(
//                    territoryRepository.findAll()
//                            .stream()
//                            .filter(territory -> territory.getSupply() != 0 && territory.getSupply() <= numberOfPlayers)
//                            .map(territory ->
//                                    playerTerritories.stream()
//                                            .filter(pt -> pt.getTerritoryId().equals(territory.getTerritoryId()))
//                                            .findFirst()
//                                            .get()
//                            ).collect(Collectors.toList()),
//                    playerTerritoryRepository.findByGameName(gameName));
//
//            game.setStarted(true);
//            gameRepository.save(game);
//
//            return new ResponseEntity(HttpStatus.OK);
//        }
//
//
//        return new ResponseEntity(HttpStatus.CONFLICT); //conflict is 409?

        game.setStarted(true);
        gameDataService.saveGame(game);
    }
}
