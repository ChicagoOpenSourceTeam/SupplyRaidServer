package org.cost.actions;

import lombok.*;
import org.cost.game.Game;
import org.cost.game.GameRepository;
import org.cost.player.Player;
import org.cost.player.PlayerController;
import org.cost.player.PlayerTerritory;
import org.cost.territory.TerritoryController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.cost.player.PlayerController.SESSION_GAME_NAME_FIELD;
import static org.cost.player.PlayerController.SESSION_PLAYER_NUMBER_FIELD;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class ActionsController {

    private GameRepository gameRepository;

    @Autowired
    public ActionsController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @RequestMapping(value = "/actions/skip-action", method = RequestMethod.POST)
    ResponseEntity skipAction(HttpSession session) {
        Game game = gameRepository.findOne((String) session.getAttribute(SESSION_GAME_NAME_FIELD));

        if (game == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Integer playerNumber = (Integer) session.getAttribute(PlayerController.SESSION_PLAYER_NUMBER_FIELD);

        if (isInactivePlayer(game, playerNumber)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Player currentPlayer = game.getPlayers().stream()
                .filter(player -> player.getPlayerNumber() == playerNumber)
                .findFirst()
                .get();

        int remainingActions = setRemainingActions(game, currentPlayer);

        gameRepository.save(game);

        return ResponseEntity.ok(SkipActionResponse.builder().actionsRemaining(remainingActions).build());
    }

    private boolean isInactivePlayer(Game game, Integer playerNumber) {
        int turnNumber = game.getTurnNumber();
        int currentTurnPlayerNumber = turnNumber % game.getPlayers().size() == 0 ? game.getPlayers().size() : game.getTurnNumber() % game.getPlayers().size();
        if (currentTurnPlayerNumber != playerNumber) {
            return true;
        }
        return false;
    }

    private int setRemainingActions(Game game, Player currentPlayer) {
        int remainingActions = currentPlayer.getRemainingActions() - 1;
        if (remainingActions == 0) {
            currentPlayer.setRemainingActions(3);
            game.setTurnNumber(game.getTurnNumber() + 1);
        } else {
            currentPlayer.setRemainingActions(remainingActions);
        }
        return remainingActions;
    }

    @RequestMapping(value = "/actions/move-troops", method = RequestMethod.POST)
    ResponseEntity moveTroops(@RequestBody MoveRequest moveRequest, HttpSession session) {
        Game game = gameRepository.findOne((String) session.getAttribute(SESSION_GAME_NAME_FIELD));
        Integer playerNumber = (Integer) session.getAttribute(SESSION_PLAYER_NUMBER_FIELD);

        if (isInactivePlayer(game, playerNumber)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        Optional<Player> playerOptional = game.getPlayers()
                .stream()
                .filter(p -> p.getPlayerNumber() == playerNumber)
                .findFirst();
        Optional<List<PlayerTerritory>> playerTerritoriesOptional = playerOptional
                .map(Player::getPlayerTerritoriesList);

        if (playerTerritoriesOptional.isPresent()) {
            List<PlayerTerritory> playerTerritories = playerTerritoriesOptional.get();
            Optional<PlayerTerritory> moveFromOptional = playerTerritories.stream()
                    .filter(playerTerritory -> playerTerritory.getTerritoryId().equals(moveRequest.getMoveFrom()))
                    .findFirst();
            Optional<PlayerTerritory> moveToOptional = playerTerritories.stream()
                    .filter(playerTerritory -> playerTerritory.getTerritoryId().equals(moveRequest.getMoveTo()))
                    .findFirst();

            if (!moveFromOptional.isPresent() || !moveToOptional.isPresent()) {
                return new ResponseEntity(HttpStatus.FORBIDDEN);
            }
            PlayerTerritory moveFrom = moveFromOptional.get();
            PlayerTerritory moveTo = moveToOptional.get();

            if (moveFrom.getTerritory().getEast() != moveRequest.getMoveTo() &&
                    moveFrom.getTerritory().getWest() != moveRequest.getMoveTo() &&
                    moveFrom.getTerritory().getSouth() != moveRequest.getMoveTo() &&
                    moveFrom.getTerritory().getNorth() != moveRequest.getMoveTo()) {
                return new ResponseEntity(HttpStatus.FORBIDDEN);
            }

            if (moveFrom.getTroops() <= moveRequest.getNumberOfTroops()) {
                return new ResponseEntity(HttpStatus.FORBIDDEN);
            }

            moveFrom.setTroops(moveFrom.getTroops() - moveRequest.getNumberOfTroops());
            moveTo.setTroops(moveTo.getTroops() + moveRequest.getNumberOfTroops());

        }

        MoveActionResponse.MoveActionResponseBuilder builder = MoveActionResponse.builder();
        int remainingActions = 0;
        if (playerOptional.isPresent()) {
            Player currentPlayer = playerOptional.get();
            remainingActions = setRemainingActions(game, currentPlayer);
        }
        builder.actionsRemaining(remainingActions);

        game = gameRepository.save(game);

        List<TerritoryController.AllTerritoriesResponse> territoriesResponse = new ArrayList<>();
        game.getPlayerTerritories()
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


        MoveActionResponse moveActionResponse = builder.territories(territoriesResponse).build();
        return ResponseEntity.ok(moveActionResponse);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class SkipActionResponse {
        private int actionsRemaining;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class MoveActionResponse {
        private int actionsRemaining;
        private List<TerritoryController.AllTerritoriesResponse> territories;
    }



    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class MoveRequest {
        private Long moveFrom;
        private Long moveTo;
        private int numberOfTroops;
    }
}
