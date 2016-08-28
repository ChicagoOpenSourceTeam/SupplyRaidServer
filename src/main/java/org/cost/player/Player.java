package org.cost.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

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
public class Player {
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
