package com.paymybuddy.webapp.model.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@RequiredArgsConstructor
public class RelationshipDTO {
    private Long relationshipId;

    @NotEmpty
    private Long userId;

    @NotEmpty
    private Long friendId;

    /**
     * vérifie si les informations contenues dans le relationshipDTO sont complètes
     *
     * @return true si relationshipDTO est correct, sinon false
     */
    public boolean isValid() {
        return this.userId != null
                && this.friendId != null;
    }
}
