package org.cost.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode
public class PlayerTerritory {
    private Long playerId;
    private Long territoryId;
    private String gameName;


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

}
