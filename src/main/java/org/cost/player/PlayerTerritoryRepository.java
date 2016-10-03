package org.cost.player;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface PlayerTerritoryRepository extends JpaRepository<Player, Long>{
    PlayerTerritory findPlayerTerritoryByTerritoryIdAndGameName(@Param("territoryId") Long territoryId,
                                                                @Param("gameName") String gameName);
}
