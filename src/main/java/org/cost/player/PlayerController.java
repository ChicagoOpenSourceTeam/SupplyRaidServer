package org.cost.player;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cost.Exceptions;
import org.cost.game.Game;
import org.cost.game.GameRepository;
import org.cost.territory.Territory;
import org.cost.territory.TerritoryController;
import org.cost.territory.TerritoryController.TerritoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
    public ResponseEntity<String> createPlayerForGame(@RequestBody CreatePlayerRequest createPlayerRequest, HttpSession httpSession) {
        List<Player> players = playerRepository.findPlayersByGameName(createPlayerRequest.getGameName()); //will return one null if no players in game
        String playerName = createPlayerRequest.getPlayerName();

        if (gameRepository.findOne(createPlayerRequest.getGameName()) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (gameRepository.findOne(createPlayerRequest.getGameName()).isStarted()) {
            return new ResponseEntity<>("The game has already started.", HttpStatus.CONFLICT);
        }

        ResponseEntity<String> gameJoinError = findGameJoinError(players, playerName);
        if (gameJoinError != null) {
            return gameJoinError;
        }

        playerRepository.save(Player.builder()
                .gameName(createPlayerRequest.getGameName())
                .name(createPlayerRequest.getPlayerName())
                .playerNumber(playerNumberService.getNextPlayerNumber(playerNumberService.getNumberOfPlayersInGame(players)))
                .build());
        httpSession.setAttribute(SESSION_GAME_NAME_FIELD, createPlayerRequest.getGameName());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<String> findGameJoinError(List<Player> players, String playerName) {
        if (players.size() >= MAX_PLAYERS_ALLOWED_IN_GAME) {
            return new ResponseEntity<>("Game Lobby is Full", HttpStatus.CONFLICT);
        }
        if (players.stream()
                .filter(player -> player != null)
                .anyMatch(player -> player.getName().equals(playerName))) {
            return new ResponseEntity<>("Player name already taken", HttpStatus.CONFLICT);
        }
        return null;
    }


    @RequestMapping(path = "/players/{playerNumber}", method = RequestMethod.GET)
    public SinglePlayerResponse getPlayer(@PathVariable int playerNumber, HttpSession session) {
        List<Player> players = playerRepository.findPlayersByGameName((String) session.getAttribute(SESSION_GAME_NAME_FIELD));
        try {
            Player player = players.stream()
                    .filter(p -> p.getPlayerNumber() == playerNumber)
                    .findFirst()
                    .get();
            List<TerritoryForSinglePlayerResponse> territoryResponseList = new ArrayList<>();
            if (player.getPlayerTerritoriesList() != null) {
                player.getPlayerTerritoriesList()
                        .forEach(territory -> {
                            TerritoryForSinglePlayerResponse territoryResponse = TerritoryForSinglePlayerResponse.builder()
                                    .name(territory.getTerritoryName())
                                    .troops(territory.getTroops())
                                    .territoryId(territory.getTerritoryId())
                                    .build();
                            territoryResponse
                                    .add(
                                            linkTo(
                                                    methodOn(TerritoryController.class).getTerritory(territory.getTerritoryId(), session))
                                                    .withSelfRel());
                            territoryResponseList.add(territoryResponse);
                        });
            }

            return SinglePlayerResponse.builder()
                    .name(player.getName())
                    .playerNumber((long) player.getPlayerNumber())
                    .ownedTerritories(territoryResponseList)
                    .build();

        } catch (NoSuchElementException e) {
            throw new Exceptions.ResourceNotFoundException();
        }
    }

    @RequestMapping(path = "/players", method = RequestMethod.GET)
    public List<AllPlayersPlayerResponse> getPlayers(HttpSession session) {
        List<Player> players = playerRepository.findPlayersByGameName((String) session.getAttribute(SESSION_GAME_NAME_FIELD));
        if (players.isEmpty()) {
            throw new Exceptions.ResourceNotFoundException();
        }

        List<AllPlayersPlayerResponse> allPlayersPlayerResponses = new ArrayList<>();
        players
                .forEach(p -> {
                    AllPlayersPlayerResponse playerResponse = AllPlayersPlayerResponse.builder()
                                    .name(p.getName())
                                    .playerNumber(p.getPlayerNumber())
                                    .troops(p.getPlayerTerritoriesList().stream().mapToInt(PlayerTerritory::getTroops).sum())
                                    .territories((int) p.getPlayerTerritoriesList().stream().count())
                                    .build();
                    playerResponse
                            .add(
                                    linkTo(
                                            methodOn(PlayerController.class).getPlayer(p.getPlayerNumber(), session))
                                    .withSelfRel());
                    allPlayersPlayerResponses.add(playerResponse);
                        }
                );
        return allPlayersPlayerResponses;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    public static class CreatePlayerRequest {
        private String gameName;
        private String playerName;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    public static class SinglePlayerResponse {
        private String name;
        private Long playerNumber;
        private List<TerritoryForSinglePlayerResponse> ownedTerritories;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    public static class AllPlayersPlayerResponse extends ResourceSupport {
        private String name;
        private int playerNumber;
        private int troops;
        private int territories;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    public static class TerritoryForSinglePlayerResponse extends ResourceSupport{
        private String name;
        private long troops;
        private long territoryId;
    }



}
