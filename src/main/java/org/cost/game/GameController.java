package org.cost.game;

import lombok.*;
import org.cost.player.PlayerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController("/game")
public class GameController {

    private GameRepository gameRepository;


    @Autowired
    public GameController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @RequestMapping(path = "/game", method = RequestMethod.POST)
    public ResponseEntity createGame(@RequestBody GameRequest gameRequest) {
        if (!gameRepository.exists(gameRequest.getGameName())) {
            Game game = new Game();
            game.setGameName(gameRequest.getGameName());
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
        if (game.getPlayers().size() >= 2) {
            game.setStarted(1);
            gameRepository.save(game);
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
