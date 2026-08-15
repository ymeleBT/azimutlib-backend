package cm.univ.library.policy;

import cm.univ.library.common.enums.Role;
import cm.univ.library.policy.dto.LibraryPolicyResponse;
import cm.univ.library.policy.dto.UpdateLibraryPolicyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/library-policies")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
public class LibraryPolicyController {

    private final LibraryPolicyService policyService;

    @GetMapping
    public List<LibraryPolicyResponse> listAll() {
        return policyService.listAll();
    }

    @PutMapping("/{role}")
    public LibraryPolicyResponse update(@PathVariable Role role, @Valid @RequestBody UpdateLibraryPolicyRequest request) {
        return policyService.update(role, request);
    }
}
