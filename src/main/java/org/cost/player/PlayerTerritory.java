package org.cost.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
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
    private Long troops;

    @ManyToOne
    @JoinColumn(name = "playerId", insertable = false, updatable = false)
    Player player;


    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


}
