package com.paymybuddy.webapp.model.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
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

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;

    @NotBlank
    @Size(max = 128)
    private String description;

    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal amountFeeExcluded = BigDecimal.ZERO;

    private BigDecimal feeAmount = BigDecimal.ZERO;

    @NotNull
    private Long relationshipId;

    private String friendFirstname;

    private String friendLastname;

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
