package com.paymybuddy.webapp.model.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class TransactionDTO {

    private Long transactionId;

    @NotNull
    @NotEmpty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;

    @NotNull
    @NotEmpty
    @Size(max = 128)
    private String description;

    @NotNull
    private BigDecimal amountFeeExcluded = BigDecimal.ZERO;

    @NotNull
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @NotNull
    @NotEmpty
    private Long relationshipId;

    /**
     * vérifie si les informations contenues dans le transactionDTO sont complètes
     * NB : le feeAmount est calculé dans TransactionService ensuite donc non attendu
     * NB : la date de transaction est positionnée dans TransactionService donc non attendue
     *
     * @return true si transactionDTO est correct, sinon false
     */
    public boolean isValid() {
        return this.description != null && !this.description.isEmpty()
                && this.amountFeeExcluded != null
                && this.relationshipId != null;
    }
}
