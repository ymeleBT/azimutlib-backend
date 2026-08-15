package cm.univ.library.fine;

import cm.univ.library.fine.dto.CreateManualFineRequest;
import cm.univ.library.fine.dto.FineResponse;
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
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;
    private final CurrentUserService currentUserService;

    @GetMapping("/my")
    public List<FineResponse> myUnpaidFines(Authentication authentication) {
        return fineService.unpaidFinesForUser(currentUserService.resolve(authentication).getId());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public List<FineResponse> finesForUser(@PathVariable Long userId) {
        return fineService.unpaidFinesForUser(userId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public List<FineResponse> allFines() {
        return fineService.allFines();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public ResponseEntity<FineResponse> createManual(@Valid @RequestBody CreateManualFineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fineService.createManualFine(request));
    }

    @PostMapping("/{fineId}/pay")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public FineResponse markPaid(@PathVariable Long fineId) {
        return fineService.markPaid(fineId);
    }
}
