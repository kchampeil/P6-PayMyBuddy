package com.paymybuddy.webapp.model.DTO;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
public class UserDTO {
    private Long userId;

    @NotNull
    @NotEmpty
    @Email
    private String email;

    @NotNull
    @NotEmpty
    @Size(max = 64)
    private String firstname;

    @NotNull
    @NotEmpty
    @Size(max = 64)
    private String lastname;

    //TODO à voir car pas à renvoyer ou pas en clair ?
    @NotNull
    @NotEmpty
    @Size(max = 64)
    private String password;

    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * vérifie si les informations contenues dans le userDTO sont complètes
     *
     * @return true si userDTO est correct, sinon false
     */
    public boolean isValid() {
        return this.email != null && !this.email.isEmpty()
                && this.firstname != null && !this.firstname.isEmpty()
                && this.lastname != null && !this.lastname.isEmpty()
                && this.password != null && !this.password.isEmpty()
                && this.balance != null;
    }
}