package com.paymybuddy.webapp.model.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@RequiredArgsConstructor
public class RelationshipDTO {

    private Long relationshipId;

    // TODO @NotNull
    private Long userId;

    private Long friendId;

    private String friendFirstname;

    private String friendLastname;

    @NotBlank(message = "Friend email must be specified")
    @Email(message = "Friend email should have an email format (xxx@yyy.zz)")
    private String friendEmail;


    /**
     * vérifie si les informations contenues dans le relationshipDTO sont complètes
     *
     * @return true si relationshipDTO est correct, sinon false
     */
    public boolean isValid() {
        return this.userId != null
                && this.friendEmail != null && !this.friendEmail.isEmpty();
    }
}
