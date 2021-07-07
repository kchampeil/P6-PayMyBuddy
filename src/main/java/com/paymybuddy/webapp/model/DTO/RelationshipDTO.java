package com.paymybuddy.webapp.model.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@RequiredArgsConstructor
public class RelationshipDTO {

    private Long relationshipId;

    @NotNull(message = "User must be specified")
    private UserDTO user;

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
        return this.user != null
                && this.friendEmail != null && !this.friendEmail.isEmpty();
    }
}
