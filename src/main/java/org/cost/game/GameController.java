package org.cost.game;

import lombok.*;
import org.cost.player.Player;
import org.cost.player.PlayerController;
import org.cost.player.PlayerTerritory;
import org.cost.player.PlayerTerritoryRepository;
import org.cost.territory.Territory;
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

    @Autowired
    public GameController(GameRepository gameRepository, TerritoryRepository territoryRepository, PlayerTerritoryRepository playerTerritoryRepository) {
        this.gameRepository = gameRepository;
        this.territoryRepository = territoryRepository;
        this.playerTerritoryRepository = playerTerritoryRepository;
    }

    @RequestMapping(path = "/game", method = RequestMethod.POST)
    public ResponseEntity createGame(@RequestBody final GameRequest gameRequest) {
        if (!gameRepository.exists(gameRequest.getGameName())) {
            Game game = new Game();
            game.setGameName(gameRequest.getGameName());
            final ArrayList<PlayerTerritory> playerTerritories = new ArrayList<>();
            territoryRepository.findAll()
                    .forEach(territory ->{
                        PlayerTerritory playerTerritory = new PlayerTerritory();
                        playerTerritory.setTerritoryId(territory.getTerritoryId());
                        playerTerritory.setGameName(gameRequest.getGameName());
                        playerTerritory.setPlayerId(null);
                        playerTerritories.add(playerTerritory);
                    });
            game.setPlayerTerritories(playerTerritories);
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
            game.setStarted(1);
            gameRepository.save(game);

            List<PlayerTerritory> supplyDepotPlayerTerritories = new ArrayList<>();
            List<Long> territoryIds = territoryRepository.findAll()
                    .stream()
                    .filter(territory -> territory.getSupply() != 0 && territory.getSupply() <= numberOfPlayers)
                    .map(Territory::getTerritoryId)
                    .collect(Collectors.toList());

            List<PlayerTerritory> playerTerritories = playerTerritoryRepository.findByGameName(gameName);

            playerTerritories = playerTerritories.stream()
                    .filter(pt -> territoryIds.contains(pt.getTerritoryId()))
                    .collect(Collectors.toList());

            long seed = System.nanoTime();
            Collections.shuffle(playerTerritories, new Random(seed));

            ArrayList<PlayerTerritory> savedPlayerTerritories = new ArrayList<>();
            for (Player player : game.getPlayers()) {
                PlayerTerritory pt = playerTerritories.remove(0);
                pt.setPlayerId(player.getPlayerId());
                savedPlayerTerritories.add(pt);
                pt = playerTerritories.remove(0);
                pt.setPlayerId(player.getPlayerId());
                savedPlayerTerritories.add(pt);
            }
            playerTerritoryRepository.save(savedPlayerTerritories);

            return new ResponseEntity(HttpStatus.OK);
        }


        return new ResponseEntity(HttpStatus.CONFLICT); //conflict is 409?
    }

    @RequestMapping(path = "/game", method = RequestMethod.GET)
    public GameResponse getGame(HttpSession httpSession) {
        String gameName = (String) httpSession.getAttribute(PlayerController.SESSION_GAME_NAME_FIELD);
        Game game = gameRepository.findOne(gameName);
        if (game.getStarted() == 1) {
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
}
