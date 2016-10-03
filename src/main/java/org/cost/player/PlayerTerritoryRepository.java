package org.cost.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PlayerTerritoryRepository extends JpaRepository<PlayerTerritory, Long>{
    PlayerTerritory findPlayerTerritoryByTerritoryIdAndGameName(Long territoryId, String gameName);
}
