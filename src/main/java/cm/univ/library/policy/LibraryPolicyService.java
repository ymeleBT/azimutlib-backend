package cm.univ.library.policy;

import cm.univ.library.common.enums.Role;
import cm.univ.library.common.exception.ResourceNotFoundException;
import cm.univ.library.policy.dto.LibraryPolicyResponse;
import cm.univ.library.policy.dto.UpdateLibraryPolicyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibraryPolicyService {

    private final LibraryPolicyRepository policyRepository;

    @Transactional(readOnly = true)
    public List<LibraryPolicyResponse> listAll() {
        return policyRepository.findAll().stream().map(LibraryPolicyResponse::from).toList();
    }

    @Transactional
    public LibraryPolicyResponse update(Role role, UpdateLibraryPolicyRequest request) {
        LibraryPolicy policy = policyRepository.findByRole(role)
                .orElseThrow(() -> new ResourceNotFoundException("No policy configured for role " + role));
        policy.setLoanDurationDays(request.loanDurationDays());
        policy.setMaxConcurrentLoans(request.maxConcurrentLoans());
        policy.setMaxRenewals(request.maxRenewals());
        policy.setFinePerDayXaf(request.finePerDayXaf());
        return LibraryPolicyResponse.from(policy);
    }
}
