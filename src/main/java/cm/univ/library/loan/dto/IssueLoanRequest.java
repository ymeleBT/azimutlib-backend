package cm.univ.library.loan.dto;

import jakarta.validation.constraints.NotBlank;

/** Circulation-desk checkout: the librarian scans/types the physical copy's
 *  inventory code and the borrower's matricule, mirroring how a real desk works. */
public record IssueLoanRequest(
        @NotBlank String inventoryCode,
        @NotBlank String borrowerMatricule
) {
}
