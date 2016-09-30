package org.cost.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.hateoas.ResourceSupport;

import javax.persistence.*;
import java.util.List;

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

    private String gameName;

    @JsonIgnore
    public String getGameName() {
        return gameName;
    }

    @Id
    @GeneratedValue
    private Long playerId;

    @JsonIgnore
    public Long getPlayerId() { return playerId; }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "playerId")
    private List<PlayerTerritory> playerTerritoriesList;
}
