package org.cost.actions;

import org.cost.game.Game;
import org.cost.game.GameRepository;
import org.cost.player.Player;
import org.cost.player.PlayerController;
import org.cost.player.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import static org.cost.player.PlayerController.SESSION_GAME_NAME_FIELD;

@RestController
public class ActionsController {

    private GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    public ActionsController(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    @RequestMapping(value = "/actions/skip-action", method = RequestMethod.POST)
    ResponseEntity skipAction(HttpSession session) {
        Game game = gameRepository.findOne((String) session.getAttribute(SESSION_GAME_NAME_FIELD));
        int currentTurnPlayerNumber = game.getTurnNumber() % game.getPlayers().size() == 0 ? game.getPlayers().size() : game.getTurnNumber() % game.getPlayers().size();

        Integer playerNumber = (Integer) session.getAttribute(PlayerController.SESSION_PLAYER_NUMBER_FIELD);
        if (currentTurnPlayerNumber != playerNumber) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Player currentPlayer = game.getPlayers().stream()
                .filter(player -> player.getPlayerNumber() == playerNumber)
                .findFirst()
                .get();
        currentPlayer.setRemainingActions(currentPlayer.getRemainingActions() - 1);

        playerRepository.save(currentPlayer);

        return new ResponseEntity(HttpStatus.OK);
    }
}
