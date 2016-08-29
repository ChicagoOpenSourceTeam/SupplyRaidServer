package org.cost.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.hateoas.ResourceSupport;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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
}
