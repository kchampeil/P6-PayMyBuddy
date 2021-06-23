package com.paymybuddy.webapp.model.DTO;

import com.paymybuddy.webapp.constants.BankTransferTypes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class BankTransferDTO {
    private Long bankTransferId;

    @NotEmpty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;

    @NotEmpty
    @Size(max = 128)
    private String description;

    @NotNull
    private BigDecimal amount = BigDecimal.ZERO;

    @NotEmpty
    private BankTransferTypes type;

    @NotEmpty
    private Long bankAccountId;

    /**
     * vérifie si les informations contenues dans le bankTransferDTO sont complètes
     * NB : la date de transfert est positionnée dans BankTransferService donc non attendue
     *
     * @return true si bankTransferDTO est correct, sinon false
     */
    public boolean isValid() {
        return this.description != null && !this.description.isEmpty()
                && this.amount != null
                && this.type != null
                && this.bankAccountId != null;
    }

    /**
     * vérifie que le type de transfert à une valeur valide dans l'énumération BankTransferTypes
     *
     * @return true si type est correct, sinon false
     */
    public boolean typeIsValid() {
        return EnumUtils.isValidEnum(BankTransferTypes.class, this.type.toString());
    }

}
