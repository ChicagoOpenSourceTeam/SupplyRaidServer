package org.cost.game;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CreateGameRequest {
    private String gameName;
}
