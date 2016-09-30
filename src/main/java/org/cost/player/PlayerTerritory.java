package org.cost.player;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode
public class PlayerTerritory {
    private long playerId;
    private long territoryId;

    @Id
    @NotNull
    private long id;
}
