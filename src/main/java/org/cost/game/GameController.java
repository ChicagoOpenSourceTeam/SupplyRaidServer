package org.cost.game;

import lombok.*;
import org.cost.player.*;
import org.cost.territory.TerritoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController("/game")
public class GameController {

    private GameRepository gameRepository;
    private TerritoryRepository territoryRepository;
    private PlayerTerritoryRepository playerTerritoryRepository;
    private SuppliedStatusService suppliedStatusService;

    @Autowired
    public GameController(GameRepository gameRepository, TerritoryRepository territoryRepository, PlayerTerritoryRepository playerTerritoryRepository, SuppliedStatusService suppliedStatusService) {
        this.gameRepository = gameRepository;
        this.territoryRepository = territoryRepository;
        this.playerTerritoryRepository = playerTerritoryRepository;
        this.suppliedStatusService = suppliedStatusService;
    }

    @RequestMapping(path = "/game", method = RequestMethod.POST)
    public ResponseEntity createGame(@RequestBody final GameRequest gameRequest) {
        if (!gameRepository.exists(gameRequest.getGameName())) {
            final ArrayList<PlayerTerritory> playerTerritories = new ArrayList<>();
            territoryRepository.findAll()
                    .forEach(territory ->{
                        PlayerTerritory playerTerritory = PlayerTerritory.builder()
                                .territoryId(territory.getTerritoryId())
                                .gameName(gameRequest.getGameName())
                                .territoryName(territory.getName())
                                .playerId(null).build();
                        playerTerritories.add(playerTerritory);
                    });
            Game game = Game.builder()
                    .gameName(gameRequest.getGameName())
                    .playerTerritories(playerTerritories).build();
            gameRepository.save(game);
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.CONFLICT);
    }


    @RequestMapping(path = "/game/{gameName}", method = RequestMethod.DELETE)
    public ResponseEntity deleteGame(@PathVariable String gameName) {
        if (!gameRepository.exists(gameName)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        gameRepository.delete(gameName);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(path = "/game/start", method = RequestMethod.POST)
    public ResponseEntity startGame(HttpSession httpSession) {
        String gameName = (String) httpSession.getAttribute(PlayerController.SESSION_GAME_NAME_FIELD);

        Game game = gameRepository.findOne(gameName);
        if (game == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        int numberOfPlayers = game.getPlayers().size();
        if (numberOfPlayers >= 2) {
            List<Long> allTerritoryIds = new ArrayList<>();

            List<PlayerTerritory> playerTerritories = playerTerritoryRepository.findByGameName(gameName);

            List<StartingLocation> startingLocations = territoryRepository.findAll()
                    .stream()
                    .filter(territory -> territory.getSupply() != 0 && territory.getSupply() <= numberOfPlayers)
                    .map(territory ->  {
                        StartingLocation.StartingLocationBuilder builder = StartingLocation.builder();
                        builder.supplyDepot(playerTerritories.stream()
                                .filter(pt -> pt.getTerritoryId().equals(territory.getTerritoryId()))
                                .findFirst()
                                .get());

                        builder.surroundingTerritories(
                                Arrays.asList(territory.getWest(), territory.getEast(), territory.getSouth(), territory.getNorth())
                                        .stream()
                                        .map(id -> playerTerritories
                                                .stream()
                                                .filter(pt -> pt.getTerritoryId().equals(id))
                                                .findFirst())
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList()));

                        return builder.build();

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
            playerTerritoryRepository.save(savedPlayerTerritories);
            suppliedStatusService.markUnsupplied();
            suppliedStatusService.markSupplied(
                    territoryRepository.findAll()
                            .stream()
                            .filter(territory -> territory.getSupply() != 0 && territory.getSupply() <= numberOfPlayers)
                            .map(territory ->
                                playerTerritories.stream()
                                        .filter(pt -> pt.getTerritoryId().equals(territory.getTerritoryId()))
                                        .findFirst()
                                        .get()
                            ).collect(Collectors.toList()),
                    playerTerritoryRepository.findByGameName(gameName));

            game.setStarted(true);
            gameRepository.save(game);

            return new ResponseEntity(HttpStatus.OK);
        }


        return new ResponseEntity(HttpStatus.CONFLICT); //conflict is 409?
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

    @RequestMapping(path = "/game", method = RequestMethod.GET)
    public GameResponse getGame(HttpSession httpSession) {
        String gameName = (String) httpSession.getAttribute(PlayerController.SESSION_GAME_NAME_FIELD);
        Game game = gameRepository.findOne(gameName);
        if (game.isStarted()) {
            return GameResponse.builder().gameStarted(true).build();
        } else {
            return GameResponse.builder().gameStarted(false).build();
        }

    }



    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    public static class GameRequest {
        private String gameName;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class GameResponse {
        private boolean gameStarted;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    public static class StartingLocation {
        private PlayerTerritory supplyDepot;
        private List<PlayerTerritory> surroundingTerritories;
    }
}
