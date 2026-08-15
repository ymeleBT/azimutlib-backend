package cm.univ.library.reservation;

import cm.univ.library.reservation.dto.CreateReservationRequest;
import cm.univ.library.reservation.dto.ReservationResponse;
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
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody CreateReservationRequest request,
                                                         Authentication authentication) {
        var response = reservationService.reserve(request, currentUserService.resolve(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id, Authentication authentication) {
        reservationService.cancel(id, currentUserService.resolve(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public List<ReservationResponse> myReservations(Authentication authentication) {
        return reservationService.reservationsForUser(currentUserService.resolve(authentication).getId());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    public List<ReservationResponse> reservationsForUser(@PathVariable Long userId) {
        return reservationService.reservationsForUser(userId);
    }
}
