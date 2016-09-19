package org.cost.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.cost.game.Game;
import org.springframework.hateoas.ResourceSupport;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Player extends ResourceSupport {
    private int playerNumber;
    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="game_name", nullable = false)
    private Game game;

    @Id
    @GeneratedValue
    private Long playerId;

    @JsonIgnore
    public Long getPlayerId() { return playerId; }
}
