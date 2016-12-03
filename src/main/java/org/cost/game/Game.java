package org.cost.game;

import lombok.*;
import org.cost.player.Player;
import org.cost.player.PlayerTerritory;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Game {
    @Id
    @NotNull
    private String gameName;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "gameName")
    private List<Player> players;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "gameName")
    private List<PlayerTerritory> playerTerritories;

    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean started;

    private int turnNumber;
}