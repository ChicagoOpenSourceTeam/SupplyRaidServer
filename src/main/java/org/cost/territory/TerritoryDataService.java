package org.cost.territory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerritoryDataService {

    //              I AM NOT YET IMPLEMENTED
    public List<Territory> getListOfTerritoriesOnMap() {
        return Arrays.asList(
                Territory.builder().territoryId(0L).name("replace me").build(),
                Territory.builder().territoryId(1L).name("soon").build());
    }
}
