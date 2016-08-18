package org.cost.territory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController("/territories")
class TerritoryController {

    private TerritoryRepository territoryRepository;

    @Autowired
    TerritoryController(TerritoryRepository territoryRepository) {
        this.territoryRepository = territoryRepository;
    }

    @RequestMapping(path = "territories/{id}", method = RequestMethod.GET)
    public Territory getTerritory(@PathVariable("id") Long id) {
        return territoryRepository.findOne(id);
    }
}
