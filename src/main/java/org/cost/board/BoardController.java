package org.cost.board;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cost.player.*;
import org.cost.territory.TerritoryController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static org.cost.player.PlayerController.SESSION_GAME_NAME_FIELD;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class BoardController {
    private final PlayerTerritoryRepository playerTerritoryRepository;
    private final PlayerRepository playerRepository;

    public BoardController(PlayerTerritoryRepository playerTerritoryRepository, PlayerRepository playerRepository) {
        this.playerTerritoryRepository = playerTerritoryRepository;
        this.playerRepository = playerRepository;
    }

    @RequestMapping(path = "/board", method = RequestMethod.GET)
    public BoardResponse getBoard(HttpSession session) {
        String gamename = (String) session.getAttribute(SESSION_GAME_NAME_FIELD);
        List<Player> players = playerRepository.findPlayersByGameName(gamename);
        List<PlayerTerritory> territories = playerTerritoryRepository.findByGameName(gamename);
        BoardResponse.BoardResponseBuilder builder = BoardResponse.builder();

        List<PlayerController.AllPlayersPlayerResponse> playersResponse = new ArrayList<>();
        players
                .forEach(p -> {
                            PlayerController.AllPlayersPlayerResponse playerResponse = PlayerController.AllPlayersPlayerResponse.builder()
                                    .name(p.getName())
                                    .playerNumber(p.getPlayerNumber())
                                    .troops(p.getPlayerTerritoriesList().stream().mapToInt(PlayerTerritory::getTroops).sum())
                                    .territories((int) p.getPlayerTerritoriesList().stream().count())
                                    .supplyDepots((int) p.getPlayerTerritoriesList()
                                            .stream().filter(pt -> pt.isSupplyDepotTerritory()).count())
                                    .build();
                            playerResponse
                                    .add(
                                            linkTo(
                                                    methodOn(PlayerController.class).getPlayer(p.getPlayerNumber(), session))
                                                    .withSelfRel());
                            playersResponse.add(playerResponse);
                        }
                );
        builder.players(playersResponse);

        List<TerritoryController.AllTerritoriesResponse> territoriesResponse = new ArrayList<>();
        territories
                .forEach(territory -> {
                    TerritoryController.AllTerritoriesResponse terrritoryResponse = TerritoryController.AllTerritoriesResponse.builder()
                            .name(territory.getTerritoryName())
                            .territoryId((territory.getTerritoryId().intValue()))
                            .supplyDepot((territory.isSupplyDepotTerritory()))
                            .supplied(territory.isSupplied())
                            .troops(territory.getTroops())
                            .playerNumber(
                                    (territory.getPlayer() == null) ? 0 :
                                            territory.getPlayer().getPlayerNumber())
                            .build();
                    terrritoryResponse
                            .add(
                                    linkTo(
                                            methodOn(TerritoryController.class).getTerritory(
                                                    territory.getTerritoryId(), session))
                                            .withSelfRel());
                    territoriesResponse.add(terrritoryResponse);
                });

        builder.territories(territoriesResponse);

        return builder.build();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class BoardResponse {
        List<TerritoryController.AllTerritoriesResponse> territories;
        List<PlayerController.AllPlayersPlayerResponse> players;
    }
}
