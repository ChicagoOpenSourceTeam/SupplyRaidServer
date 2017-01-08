package org.cost.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.cost.player.PlayerTerritory;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class StartingLocation {
    private PlayerTerritory supplyDepot;
    private List<PlayerTerritory> surroundingTerritories;
}
