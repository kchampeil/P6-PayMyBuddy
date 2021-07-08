package com.paymybuddy.webapp.model.DTO;

import com.paymybuddy.webapp.constants.BankAccountConstants;
import com.paymybuddy.webapp.constants.IbanPrefixes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.EnumUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@RequiredArgsConstructor
public class BankAccountDTO {
    private Long bankAccountId;

    @NotBlank(message = "The IBAN must be specified")
    @Size(max = 34)
    private String iban;

    @NotBlank(message = "The account name must be specified")
    @Size(max = 64)
    private String name;

    @NotNull(message = "The user id must be specified")
    private Long userId;

    /**
     * vérifie si les informations contenues dans le bankAccountDTO sont complètes
     *
     * @return true si bankAccountDTO est correct, sinon false
     */
    public boolean isValid() {
        return this.iban != null && !this.iban.isEmpty()
                && this.name != null && !this.name.isEmpty()
                && this.userId != null;
    }

    /**
     * vérifie si l IBAN est valide conformément à la norme
     * TODO V2 : pour l instant on vérifie juste la longueur et que ça commence par deux lettres autorisées
     *
     * @return true si l IBAN est correct, sinon false
     */
    public boolean hasValidIban() {
        return (this.iban.length() < BankAccountConstants.MAX_LENGTH_FOR_IBAN
                && EnumUtils.isValidEnum(IbanPrefixes.class, this.iban.substring(0, 2)));

    }
}
