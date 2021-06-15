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
public class BankTransferDTO {
    private Long bankTransferId;

    @NotNull
    @NotEmpty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;

    @NotNull
    @NotEmpty
    @Size(max = 128)
    private String description;

    @NotNull
    private BigDecimal amount = BigDecimal.ZERO;

    @NotNull
    @NotEmpty
    private Long bankAccountId;

    /**
     * vérifie si les informations contenues dans le bankTransferDTO sont complètes
     *
     * @return true si bankTransferDTO est correct, sinon false
     */
    public boolean isValid() {
        return this.date != null
                && this.description != null && !this.description.isEmpty()
                && this.amount != null
                && this.bankAccountId != null;
    }

}
