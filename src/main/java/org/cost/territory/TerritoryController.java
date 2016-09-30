package org.cost.territory;

import lombok.*;

import org.cost.player.Player;
import org.cost.player.PlayerController;
import org.cost.player.PlayerRepository;
import org.cost.Exceptions;
import org.cost.player.PlayerTerritory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController("/territories")
class TerritoryController {

    private TerritoryRepository territoryRepository;
    private PlayerRepository playerRepository;
    //private PlayerController playerController;

    @Autowired
    TerritoryController(TerritoryRepository territoryRepository, PlayerRepository playerRepository) {
        this.territoryRepository = territoryRepository;
        this.playerRepository = playerRepository;
    }

    @RequestMapping(path = "territories/{territoryId}", method = RequestMethod.GET)
    public TerritoryResponse getTerritory(@PathVariable("territoryId") Long territoryId, HttpSession session) {
        Territory requestedTerritory = territoryRepository.findOne(territoryId);

        if (requestedTerritory == null) {
            throw new Exceptions.ResourceNotFoundException();
        }

        TerritoryResponse.TerritoryResponseBuilder builder = TerritoryResponse.builder().name(requestedTerritory.getName());

        Long northId = requestedTerritory.getNorth();
        Long eastId = requestedTerritory.getEast();
        Long westId = requestedTerritory.getWest();
        Long southId = requestedTerritory.getSouth();

        if (northId != null) {
            Territory northTerritory = territoryRepository.findOne(northId);
            NeighboringTerritoryResponse northTerritoryResponse = NeighboringTerritoryResponse.builder().name(northTerritory.getName()).build();
            northTerritoryResponse.add(linkTo(methodOn(TerritoryController.class).getTerritory(northTerritory.getTerritoryId(), session)).withSelfRel());
            builder = builder.north(northTerritoryResponse);
        }
        if (eastId != null) {
            Territory eastTerritory = territoryRepository.findOne(eastId);
            NeighboringTerritoryResponse eastTerritoryResponse = NeighboringTerritoryResponse.builder().name(eastTerritory.getName()).build();
            eastTerritoryResponse.add(linkTo(methodOn(TerritoryController.class).getTerritory(eastTerritory.getTerritoryId(), session)).withSelfRel());
            builder = builder.east(eastTerritoryResponse);
        }
        if (westId != null) {
            Territory westTerritory = territoryRepository.findOne(westId);
            NeighboringTerritoryResponse westTerritoryResponse = NeighboringTerritoryResponse.builder().name(westTerritory.getName()).build();
            westTerritoryResponse.add(linkTo(methodOn(TerritoryController.class).getTerritory(westTerritory.getTerritoryId(), session)).withSelfRel());
            builder = builder.west(westTerritoryResponse);
        }
        if (southId != null) {
            Territory southTerritory = territoryRepository.findOne(southId);
            NeighboringTerritoryResponse southTerritoryResponse = NeighboringTerritoryResponse.builder().name(southTerritory.getName()).build();
            southTerritoryResponse.add(linkTo(methodOn(TerritoryController.class).getTerritory(southTerritory.getTerritoryId(), session)).withSelfRel());
            builder = builder.south(southTerritoryResponse);
        }

        int playersInGame = playerRepository.findPlayersByGameName((String)session.getAttribute(PlayerController.SESSION_GAME_NAME_FIELD)).size();
        int supplyDepotId = requestedTerritory.getSupply();

        if ((supplyDepotId == 0) || (supplyDepotId > playersInGame)) {
            builder = builder.supply(false);
        }
        else {
            builder = builder.supply(true);
        }



        return builder.build();
    }

    @RequestMapping(path = "territories", method = RequestMethod.GET)
    public List<Territory> getTerritories(HttpSession session) {
        List<Territory> territories = territoryRepository.findAll();
        territories.forEach(territory -> territory.add(linkTo(methodOn(TerritoryController.class).getTerritory(territory.getTerritoryId(), session)).withSelfRel()));
        return territories;
    }

    @RequestMapping(path = "/territories/owner", method = RequestMethod.POST)
    public ResponseEntity assignTerritoryToPlayer(@RequestBody TerritoryRequest territoryRequest, HttpSession session){
        if(territoryRepository.exists((long) territoryRequest.getTerritoryId())) {

            //assign territory id to player number
            List<Player> players = playerRepository.findPlayersByGameName((String) session.getAttribute(PlayerController.SESSION_GAME_NAME_FIELD));
            Optional<Player> first = players
                    .stream()
                    .filter(p -> p.getPlayerNumber() == territoryRequest.getPlayerNumber())
                    .findFirst();
            if (!first.isPresent()) {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }

            Player player = first.get();

            PlayerTerritory playerTerritory = new PlayerTerritory();
            playerTerritory.setPlayerId(player.getPlayerId());
            playerTerritory.setTerritoryId(territoryRequest.getTerritoryId());
            player.getPlayerTerritoriesList().add(playerTerritory);
            playerRepository.save(player);
            return new ResponseEntity(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class TerritoryResponse {
        private String name;
        private boolean supply;
        private long territoryId;

        private NeighboringTerritoryResponse north;
        private NeighboringTerritoryResponse south;
        private NeighboringTerritoryResponse east;
        private NeighboringTerritoryResponse west;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class NeighboringTerritoryResponse extends ResourceSupport {
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class TerritoryRequest {
        private int territoryId;
        private int playerNumber;
    }
}