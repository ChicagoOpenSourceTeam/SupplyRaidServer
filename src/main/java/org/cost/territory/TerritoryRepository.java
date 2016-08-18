package org.cost.territory;

import org.cost.territory.Territory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TerritoryRepository extends JpaRepository<Territory, Long>{
}
