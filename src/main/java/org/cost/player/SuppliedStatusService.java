package org.cost.player;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuppliedStatusService {

    private PlayerRepository playerRepository;
    private PlayerTerritoryRepository playerTerritoryRepository;

    @Autowired
    public SuppliedStatusService(PlayerRepository playerRepository, PlayerTerritoryRepository playerTerritoryRepository) {

        this.playerRepository = playerRepository;
        this.playerTerritoryRepository = playerTerritoryRepository;
    }

    public void markUnsupplied() {
        List<Player> players = playerRepository.findAll();

        players
                .forEach(player -> player.getPlayerTerritoriesList()
                        .forEach(playerTerritory -> playerTerritory.setSupplied(false)));

        playerRepository.save(players);
    }

    public void markSupplied(List<PlayerTerritory> supplyDepots, List<PlayerTerritory> allPlayerTerritories) {
        HashMap<Long, MappedTerritory> nodes = new HashMap<>();

        allPlayerTerritories
                .forEach(pt -> {
                    MappedTerritory mappedTerritory = new MappedTerritory();
                    mappedTerritory.setPlayerTerritory(pt);
                    nodes.put(pt.getTerritoryId(), mappedTerritory);
                });

        allPlayerTerritories
                .forEach(pt -> {
                    MappedTerritory mappedTerritory = nodes.get(pt.getTerritoryId());
                    Arrays.asList(
                            pt.getTerritory().getEast(),
                            pt.getTerritory().getWest(),
                            pt.getTerritory().getNorth(),
                            pt.getTerritory().getSouth())
                            .stream()
                            .filter(id -> id != null)
                            .forEach(
                                   each -> mappedTerritory.getNeighboringTerritories().add(nodes.get(each)));

                } );

        




        playerTerritoryRepository.save(allPlayerTerritories);
    }
    @Getter
    @Setter
    @EqualsAndHashCode
    private class MappedTerritory {
        private List<MappedTerritory> neighboringTerritories = new ArrayList<>();

        private PlayerTerritory playerTerritory;
    }
}
