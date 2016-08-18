package org.cost.territory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Territory {
    @Id
    @GeneratedValue
    private Long id;

    @JsonProperty
    private String name;
}
