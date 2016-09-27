package org.cost.game;

import lombok.*;
import org.cost.player.Player;
import org.hibernate.annotations.Cascade;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Game {
    @Id
    @NotNull
    private String gameName;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "gameName")
    private List<Player> players;

    private int started;
}
