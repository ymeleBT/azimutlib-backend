package cm.univ.library.resource;

import cm.univ.library.common.enums.ResourceType;
import cm.univ.library.resource.dto.TeachingResourceResponse;
import cm.univ.library.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class TeachingResourceController {

    private final TeachingResourceService resourceService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public List<TeachingResourceResponse> list(@RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) ResourceType type) {
        return resourceService.list(categoryId, type);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('LECTURER','LIBRARIAN','ADMIN')")
    public ResponseEntity<TeachingResourceResponse> upload(@RequestParam MultipartFile file,
                                                             @RequestParam String title,
                                                             @RequestParam(required = false) String description,
                                                             @RequestParam ResourceType type,
                                                             @RequestParam Long categoryId,
                                                             @RequestParam(required = false) String author,
                                                             @RequestParam(required = false) String academicYear,
                                                             Authentication authentication) {
        var uploader = currentUserService.resolve(authentication);
        var response = resourceService.upload(file, title, description, type, categoryId, author, academicYear, uploader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) {
        var resource = resourceService.findEntityById(id);
        var stream = resourceService.download(id);

        MediaType mediaType = resource.getContentType() != null
                ? MediaType.parseMediaType(resource.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(resource.getOriginalFilename(), StandardCharsets.UTF_8)
                                .build().toString())
                .body(new InputStreamResource(stream));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LECTURER','LIBRARIAN','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        resourceService.delete(id, currentUserService.resolve(authentication));
        return ResponseEntity.noContent().build();
    }
}
