package org.cost.player;

import org.cost.Exceptions;
import org.cost.SessionFields;
import org.cost.game.Game;
import org.cost.game.GameDataService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpSession;
import java.util.List;

public class PlayerService {
    private static final int MAX_PLAYERS_ALLOWED_IN_GAME = 4;
    private final PlayerDataService playerDataService;
    private final GameDataService gameDataService;
    private final PlayerNumberService playerNumberService;

    @Autowired
    public PlayerService(PlayerDataService playerDataService, GameDataService gameDataService, PlayerNumberService playerNumberService) {

        this.playerDataService = playerDataService;
        this.gameDataService = gameDataService;
        this.playerNumberService = playerNumberService;
    }

    public void addPlayerToGame(CreatePlayerRequest createPlayerRequest, HttpSession httpSession) {
        List<Player> players = playerDataService.findPlayersInGameByGameName(createPlayerRequest.getGameName()); //will return one null if no players in game
        String playerName = createPlayerRequest.getPlayerName();

        Game game = gameDataService.findGameByName(createPlayerRequest.getGameName());
        if (game == null) {
            throw new Exceptions.ResourceNotFoundException("Game not found");
        }

        if (game.isStarted()) {
            throw new Exceptions.ConflictException("The game has already started");
        }

        findGameJoinError(players, playerName);

        int nextPlayerNumber = playerNumberService.getNextPlayerNumber(playerNumberService.getNumberOfPlayersInGame(players));
        playerDataService.savePlayer(Player.builder()
                .gameName(createPlayerRequest.getGameName())
                .name(createPlayerRequest.getPlayerName())
                .playerNumber(nextPlayerNumber)
                .remainingActions(3)
                .build());
        httpSession.setAttribute(SessionFields.GAME_NAME.toString(), createPlayerRequest.getGameName());
        httpSession.setAttribute(SessionFields.PLAYER_NAME.toString(), nextPlayerNumber);
    }

    private void findGameJoinError(List<Player> players, String playerName) {
        if (players.size() >= MAX_PLAYERS_ALLOWED_IN_GAME) {
            throw new Exceptions.ConflictException("Game Lobby is Full");
        }
        if (players.stream()
                .filter(player -> player != null)
                .anyMatch(player -> player.getName().equals(playerName))) {
            throw new Exceptions.ConflictException("Player name already taken");
        }
    }
}
