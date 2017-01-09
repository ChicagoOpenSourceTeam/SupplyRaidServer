package org.cost.game;

import org.cost.player.*;
import org.cost.territory.TerritoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController("/game")
public class GameController {

    private GameRepository gameRepository;
    private TerritoryRepository territoryRepository;
    private PlayerTerritoryRepository playerTerritoryRepository;
    private SuppliedStatusService suppliedStatusService;
    private GameService gameService;

    @Autowired
    public GameController(GameRepository gameRepository, TerritoryRepository territoryRepository, PlayerTerritoryRepository playerTerritoryRepository, SuppliedStatusService suppliedStatusService, GameService gameService ) {
        this.gameRepository = gameRepository;
        this.territoryRepository = territoryRepository;
        this.playerTerritoryRepository = playerTerritoryRepository;
        this.suppliedStatusService = suppliedStatusService;
        this.gameService = gameService;
    }

    @RequestMapping(path = "/game", method = RequestMethod.GET)
    public GameResponse getGame(HttpSession httpSession) throws Exception{
        gameService.checkIfGameHasStarted(httpSession);
        return new GameResponse();
    }

    @RequestMapping(path = "/game", method = RequestMethod.POST)
    public ResponseEntity createGame(@RequestBody final CreateGameRequest gameRequest) throws Exception {
         gameService.createGame(gameRequest);
         return new ResponseEntity(HttpStatus.OK);
    }


    @RequestMapping(path = "/game/{gameName}", method = RequestMethod.DELETE)
    public ResponseEntity deleteGame(@PathVariable String gameName) throws Exception {
         gameService.deleteGame(gameName);
         return new ResponseEntity(HttpStatus.OK);
    }



    @RequestMapping(path = "/game/start", method = RequestMethod.POST)
    public ResponseEntity startGame(HttpSession httpSession) throws Exception{
        gameService.startGame(httpSession);
        return new ResponseEntity(HttpStatus.OK);
    }



}
