package org.cost.game;

import lombok.*;
import org.cost.player.Player;

import javax.persistence.*;
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

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "game")
    private List<Player> players;
}
