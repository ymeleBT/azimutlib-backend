package cm.univ.library.loan;

import cm.univ.library.loan.dto.IssueLoanRequest;
import cm.univ.library.loan.dto.LoanResponse;
import cm.univ.library.loan.dto.ReturnByCodeRequest;
import cm.univ.library.security.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public ResponseEntity<LoanResponse> issue(@Valid @RequestBody IssueLoanRequest request, Authentication authentication) {
        var issuedBy = currentUserService.resolve(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.issueLoan(request, issuedBy));
    }

    @PostMapping("/{loanId}/return")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public LoanResponse returnLoan(@PathVariable Long loanId) {
        return loanService.returnLoan(loanId);
    }

    @PostMapping("/return-by-code")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public LoanResponse returnByCode(@Valid @RequestBody ReturnByCodeRequest request) {
        return loanService.returnByCode(request);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public List<LoanResponse> active() {
        return loanService.activeLoans();
    }

    @GetMapping("/active-by-code/{inventoryCode}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public LoanResponse activeByCode(@PathVariable String inventoryCode) {
        return loanService.activeLoanByCode(inventoryCode);
    }

    @PostMapping("/{loanId}/renew")
    public LoanResponse renew(@PathVariable Long loanId, Authentication authentication) {
        return loanService.renewLoan(loanId, currentUserService.resolve(authentication));
    }

    @GetMapping("/my")
    public List<LoanResponse> myLoans(Authentication authentication) {
        return loanService.loansForUser(currentUserService.resolve(authentication).getId());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public List<LoanResponse> loansForUser(@PathVariable Long userId) {
        return loanService.loansForUser(userId);
    }
}
