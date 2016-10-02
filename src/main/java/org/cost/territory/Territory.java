package org.cost.territory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.hateoas.ResourceSupport;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  Territory extends ResourceSupport {
    private String name;
    private int supply;
    @Id
    @GeneratedValue
    private Long territoryId;

    private Long north;
    private Long east;
    private Long south;
    private Long west;

    @JsonIgnore
    public Long getTerritoryId() {
        return territoryId;
    }
}
