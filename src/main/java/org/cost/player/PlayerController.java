package org.cost.player;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cost.game.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@RestController
public class PlayerController {

    public static final int MAX_PLAYERS_ALLOWED_IN_GAME = 4;
    private PlayerRepository playerRepository;

    @Autowired
    public PlayerController(PlayerRepository gameRepository) {
        this.playerRepository = gameRepository;
    }

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity createPlayerForGame(@RequestBody CreatePlayerRequest createPlayerRequest) {
        List<Player> players = playerRepository.findPlayersByGameName(createPlayerRequest.getGameName()); //will return one null if no players in game
        if (players.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        if (players.size() >= MAX_PLAYERS_ALLOWED_IN_GAME || players.stream()
                .filter(player -> player != null)
                .anyMatch(player -> player.getPlayerName().equals(createPlayerRequest.getPlayerName()))) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        playerRepository.save(Player.builder()
                .gameName(createPlayerRequest.getGameName())
                .playerName(createPlayerRequest.getPlayerName())
                .build());
        return new ResponseEntity(HttpStatus.OK);
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
