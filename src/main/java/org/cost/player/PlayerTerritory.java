package org.cost.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlayerTerritory {
    private Long playerId;
    private Long territoryId;
    private String gameName;
    private String territoryName;
    private int troops;

    @ManyToOne
    @JoinColumn(name = "playerId", insertable = false, updatable = false)
    Player player;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


}
