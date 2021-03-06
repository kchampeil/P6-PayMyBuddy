package com.paymybuddy.webapp.model.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
public class UserDTO {
    private Long userId;

    @NotBlank(message = "Email should not be empty")
    @Size(max = 256)
    @Email
    private String email;

    @NotBlank(message = "Firstname should not be empty")
    @Size(max = 64)
    private String firstname;

    @NotBlank(message = "Lastname should not be empty")
    @Size(max = 64)
    private String lastname;

    @NotBlank(message = "Password should not be empty")
    @Size(min = 7, max = 64, message = "Password length must be between 7 and 64 characters")
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
     * TODO V2 : A voir si d autres exigences sont requises au niveau de la validité du mail
     *
     * @return true si l'email est correct, sinon false
     */
    public boolean hasValidEmail() {
        return this.email.contains("@")
                && this.email.length() >= 7
                && this.email.length() <= 64;
    }
}
