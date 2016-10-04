package org.cost.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerTerritoryRepository extends JpaRepository<PlayerTerritory, Long>{
    PlayerTerritory findPlayerTerritoryByTerritoryIdAndGameName(Long territoryId, String gameName);

    List<PlayerTerritory> findByGameName(String gameName);
}
