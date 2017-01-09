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
    SuppliedStatusService suppliedStatusService;


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

    /// should method be written for unrealistic scenarios, such as nonexistent session and game? //
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
        if (game.isStarted()) {
            throw new Exceptions.ConflictException("Game Already Started");
        }

        int numberOfPlayers = game.getPlayers().size();
        if(numberOfPlayers < 2){
            throw new Exceptions.ConflictException("Invalid Game: Too Few Players");
        }


        //
        List<PlayerTerritory> playerTerritories = playerTerritoryDataService.getTerritoriesInGame(gameName);

        List<StartingLocation> startingLocations = territoryDataService.getListOfTerritoriesOnMap()
                .stream()
                .filter(territory -> territory.getSupply() != 0 && territory.getSupply() <= numberOfPlayers)
                .map(territory ->  {
                    StartingLocation.StartingLocationBuilder buildOutFromSupplyDepot = StartingLocation.builder();
                    buildOutFromSupplyDepot.supplyDepot(playerTerritories.stream()
                            .filter(pt -> pt.getTerritoryId().equals(territory.getTerritoryId()))
                            .findFirst()
                            .get());

                    buildOutFromSupplyDepot.surroundingTerritories(
                            Arrays.asList(territory.getWest(), territory.getEast(), territory.getSouth(), territory.getNorth())
                                    .stream()
                                    .map(id -> playerTerritories
                                            .stream()
                                            .filter(pt -> pt.getTerritoryId().equals(id))
                                            .findFirst())
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList()));

                    return buildOutFromSupplyDepot.build();

                })
                .collect(Collectors.toList());

        long seed = System.nanoTime();
        Collections.shuffle(startingLocations, new Random(seed));

        ArrayList<PlayerTerritory> savedPlayerTerritories = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            StartingLocation startingLocation = startingLocations.remove(0);
            startingLocation.getSupplyDepot().setPlayerId(player.getPlayerId());
            startingLocation.getSupplyDepot().setTroops(8);
            startingLocation.getSupplyDepot().setSupplyDepotTerritory(true);
            startingLocation.getSupplyDepot().setPlayer(player);
            savedPlayerTerritories.add(startingLocation.getSupplyDepot());

            int numberOfSurroundingTerritories = (int)startingLocation.
                    getSurroundingTerritories().stream().filter(t -> t != null).count();
            int troopsPerSurroundingTerritory = 12/numberOfSurroundingTerritories;

            initializeTroopsForTerritoriesAdjacentToSupplyDepots(savedPlayerTerritories, player,
                    startingLocation, troopsPerSurroundingTerritory);

            startingLocation = startingLocations.remove(0);
            startingLocation.getSupplyDepot().setPlayerId(player.getPlayerId());
            startingLocation.getSupplyDepot().setTroops(8);
            startingLocation.getSupplyDepot().setSupplyDepotTerritory(true);
            startingLocation.getSupplyDepot().setPlayer(player);
            savedPlayerTerritories.add(startingLocation.getSupplyDepot());

            numberOfSurroundingTerritories = (int)startingLocation.
                    getSurroundingTerritories().stream().filter(t -> t != null).count();
            troopsPerSurroundingTerritory = 12/numberOfSurroundingTerritories;

            initializeTroopsForTerritoriesAdjacentToSupplyDepots(savedPlayerTerritories, player,
                    startingLocation, troopsPerSurroundingTerritory);
        }
        playerTerritoryDataService.saveTerritory(savedPlayerTerritories);
        suppliedStatusService.markUnsupplied();
        suppliedStatusService.markSupplied(
                territoryDataService.getListOfTerritoriesOnMap()
                        .stream()
                        .filter(territory -> territory.getSupply() != 0 && territory.getSupply() <= numberOfPlayers)
                        .map(territory ->
                                playerTerritories.stream()
                                        .filter(pt -> pt.getTerritoryId().equals(territory.getTerritoryId()))
                                        .findFirst()
                                        .get()
                        ).collect(Collectors.toList()),
                playerTerritoryDataService.findByGameName(gameName));

        game.setStarted(true);
        gameDataService.saveGame(game);
    }





    private void initializeTroopsForTerritoriesAdjacentToSupplyDepots(ArrayList<PlayerTerritory> savedPlayerTerritories,
                                                                      Player player, StartingLocation startingLocation,
                                                                      int troopsPerSurroundingTerritory) {
        startingLocation.getSurroundingTerritories()
                .forEach(
                        surroundingTerritory -> {
                            surroundingTerritory.setPlayerId(player.getPlayerId());
                            surroundingTerritory.setTroops(troopsPerSurroundingTerritory);
                            surroundingTerritory.setPlayer(player);
                            savedPlayerTerritories.add(surroundingTerritory);
                        }
                );
    }
}
