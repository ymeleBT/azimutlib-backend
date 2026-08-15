package cm.univ.library.fine;

import cm.univ.library.common.enums.FineType;
import cm.univ.library.common.exception.ResourceNotFoundException;
import cm.univ.library.fine.dto.CreateManualFineRequest;
import cm.univ.library.fine.dto.FineResponse;
import cm.univ.library.loan.Loan;
import cm.univ.library.user.User;
import cm.univ.library.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;
    private final UserService userService;

    @Transactional
    public Fine createFine(Loan loan, BigDecimal amountXaf, String reason, FineType type) {
        Fine fine = Fine.builder()
                .loan(loan)
                .user(loan.getBorrower())
                .amountXaf(amountXaf)
                .reason(reason)
                .type(type)
                .build();
        return fineRepository.save(fine);
    }

    @Transactional
    public FineResponse createManualFine(CreateManualFineRequest request) {
        User user = userService.findEntityById(request.userId());
        Fine fine = Fine.builder()
                .loan(null)
                .user(user)
                .amountXaf(request.amountXaf())
                .reason(request.reason())
                .type(FineType.MANUAL)
                .build();
        return FineResponse.from(fineRepository.save(fine));
    }

    @Transactional
    public FineResponse markPaid(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found: " + fineId));
        fine.setPaid(true);
        fine.setPaidAt(Instant.now());
        return FineResponse.from(fine);
    }

    @Transactional(readOnly = true)
    public List<FineResponse> unpaidFinesForUser(Long userId) {
        return fineRepository.findByUserIdAndPaid(userId, false).stream()
                .map(FineResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasUnpaidFines(Long userId) {
        return fineRepository.existsByUserIdAndPaidFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<FineResponse> allFines() {
        return fineRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(FineResponse::from)
                .toList();
    }
}
