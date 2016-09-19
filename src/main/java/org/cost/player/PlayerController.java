package org.cost.player;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cost.Exceptions;
import org.cost.game.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class PlayerController {

    public static final int MAX_PLAYERS_ALLOWED_IN_GAME = 4;
    public static final String SESSION_GAME_NAME_FIELD = "game_name";
    private PlayerRepository playerRepository;
    private PlayerNumberService playerNumberService;
    private GameRepository gameRepository;

    @Autowired
    public PlayerController(PlayerRepository playerRepository, PlayerNumberService playerNumberService, GameRepository gameRepository) {
        this.playerRepository = playerRepository;
        this.playerNumberService = playerNumberService;
        this.gameRepository = gameRepository;
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity createPlayerForGame(@RequestBody CreatePlayerRequest createPlayerRequest, HttpSession httpSession) {
        List<Player> players = playerRepository.findPlayersByGameName(createPlayerRequest.getGameName()); //will return one null if no players in game
        if (players.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        if (players.size() >= MAX_PLAYERS_ALLOWED_IN_GAME || players.stream()
                .filter(player -> player != null)
                .anyMatch(player -> player.getName().equals(createPlayerRequest.getPlayerName()))) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        playerRepository.save(Player.builder()
                .game(gameRepository.findOne(createPlayerRequest.getGameName()))
                .name(createPlayerRequest.getPlayerName())
                .playerNumber(playerNumberService.getNextPlayerNumber(playerNumberService.getNumberOfPlayersInGame(players)))
                .build());
        httpSession.setAttribute(SESSION_GAME_NAME_FIELD, createPlayerRequest.getGameName());
        return new ResponseEntity(HttpStatus.OK);
    }


    @RequestMapping(path = "/players/{playerNumber}", method = RequestMethod.GET)
    public Player getPlayer(@PathVariable int playerNumber, HttpSession session) {
        List<Player> players = playerRepository.findPlayersByGameName((String) session.getAttribute(SESSION_GAME_NAME_FIELD));
        try {
            Player player = players.stream()
                    .filter(p -> p.getPlayerNumber() == playerNumber)
                    .findFirst()
                    .get();
            return player;
        } catch (NoSuchElementException e) {
            throw new Exceptions.ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/players", method = RequestMethod.GET)
    public List<Player> getPlayers(HttpSession session) {
        List<Player> players = playerRepository.findPlayersByGameName((String) session.getAttribute(SESSION_GAME_NAME_FIELD));
        if (players.isEmpty()) {
            throw new Exceptions.ResourceNotFoundException();
        }

        players
                .stream()
                .forEach(player -> player.add(
                        linkTo(
                                methodOn(PlayerController.class).getPlayer(player.getPlayerNumber(), session))
                                .withSelfRel())
                );
        return players;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    public static class CreatePlayerRequest {
        private String gameName;
        private String playerName;
    }
}
