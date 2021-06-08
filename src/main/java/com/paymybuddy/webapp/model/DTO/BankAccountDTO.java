package com.paymybuddy.webapp.model.DTO;

import com.paymybuddy.webapp.constants.BankAccountConstants;
import com.paymybuddy.webapp.constants.IbanPrefixes;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.EnumUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class BankAccountDTO {
    private Long bankAccountId;

    @NotNull
    @NotEmpty
    @Size(max = 34)
    private String iban;

    @NotNull
    @NotEmpty
    @Size(max = 64)
    private String name;

    @NotNull
    @NotEmpty
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
     * TODO : pour l instant on vérifie juste la longueur et que ça commence par deux lettres autorisées
     *
     * @return true si l IBAN est correct, sinon false
     */
    public boolean ibanIsValid() {
        return (this.iban.length() < BankAccountConstants.MAX_LENGTH_FOR_IBAN
                && EnumUtils.isValidEnum(IbanPrefixes.class, this.iban.substring(0, 2)));

    }
}
