package org.cost.player;

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
    private String gameName;
    private String playerName;

    @Id
    @GeneratedValue
    private Long playerId;
}
