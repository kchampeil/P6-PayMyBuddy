package com.paymybuddy.webapp.model.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
public class UserDTO {
    private Long userId;

    @NotEmpty
    @Size(max = 256)
    @Email
    private String email;

    @NotEmpty
    @Size(max = 64)
    private String firstname;

    @NotEmpty
    @Size(max = 64)
    private String lastname;

    //TODO à voir car pas à renvoyer ou pas en clair ?
    @NotEmpty
    @Size(min = 7, max = 64)
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

    /**
     * vérifie si l email est correct
     * NB : à ce stade seule la présence du @ est vérifiée.
     * TODO : A voir si d autres exigences sont requises au niveau de la validité du mail
     *
     * @return true si l'email est correct, sinon false
     */
    public boolean hasValidEmail() {
        return this.email.contains("@");
    }
}
