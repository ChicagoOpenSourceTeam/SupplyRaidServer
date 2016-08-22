package org.cost.territory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController("/territories")
class TerritoryController {

    private TerritoryRepository territoryRepository;

    @Autowired
    TerritoryController(TerritoryRepository territoryRepository) {
        this.territoryRepository = territoryRepository;
    }

    @RequestMapping(path = "territories/{territoryId}", method = RequestMethod.GET)
    public Territory getTerritory(@PathVariable("territoryId") Long territoryId) {
        return territoryRepository.findOne(territoryId);
    }

    @RequestMapping(path = "territories", method = RequestMethod.GET)
    public List<Territory> getTerritories() {
        List<Territory> territories = territoryRepository.findAll();
        territories.stream().forEach(new Consumer<Territory>() {
            @Override
            public void accept(Territory territory) {
                territory.add(linkTo(methodOn(TerritoryController.class).getTerritory(territory.getTerritoryId())).withSelfRel());
            }
        });
        return territories;
    }
}
