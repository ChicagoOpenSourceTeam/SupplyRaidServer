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
class Territory extends ResourceSupport {
    @JsonProperty
    private String name;

    @Id
    @GeneratedValue
    private Long territoryId;

    @JsonIgnore
    public Long getTerritoryId() {
        return territoryId;
    }
}
