package org.cost.player;

import lombok.*;


@Builder
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CreatePlayerRequest {
    private String gameName;
    private String playerName;
}

