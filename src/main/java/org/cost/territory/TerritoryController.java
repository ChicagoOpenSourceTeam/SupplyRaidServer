package org.cost.territory;

import lombok.*;

import org.cost.game.GameController;
import org.cost.game.GameRepository;
import org.cost.player.PlayerController;
import org.cost.player.PlayerRepository;
import org.cost.Exceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
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
        territories.stream().forEach(new Consumer<Territory>() {
            @Override
            public void accept(Territory territory) {
                territory.add(linkTo(methodOn(TerritoryController.class).getTerritory(territory.getTerritoryId(), session)).withSelfRel());
            }
        });
        return territories;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class TerritoryResponse {
        private String name;
        private boolean supply;

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
}