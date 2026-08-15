package cm.univ.library.storage;

import cm.univ.library.common.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** Stores uploaded files (teaching resources, avatars, ...) on local disk under
 *  {@code app.upload-dir}/{category}/. Swap for an S3/object-storage-backed
 *  implementation later without touching call sites — the storage key returned by
 *  {@link #store} is an opaque relative path, never an absolute filesystem path. */
@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + root, e);
        }
    }

    /** @return the storage key (e.g. "avatars/&lt;uuid&gt;.png") to persist on the entity. */
    public String store(MultipartFile file, String category) {
        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        String extension = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalFilename.substring(dot);
        }

        Path categoryDir = root.resolve(category).normalize();
        if (!categoryDir.getParent().equals(root)) {
            throw new BusinessRuleException("Invalid storage category");
        }
        try {
            Files.createDirectories(categoryDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create upload directory: " + categoryDir, e);
        }

        String storageKey = category + "/" + UUID.randomUUID() + extension;
        Path target = resolveWithinRoot(storageKey);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessRuleException("Failed to store file: " + e.getMessage());
        }
        return storageKey;
    }

    public InputStream load(String storageKey) {
        Path target = resolveWithinRoot(storageKey);
        try {
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new BusinessRuleException("File not found on disk: " + storageKey);
        }
    }

    public void delete(String storageKey) {
        Path target = resolveWithinRoot(storageKey);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            // Best-effort: an orphaned file on disk is a cleanup issue, not a request failure.
        }
    }

    private Path resolveWithinRoot(String storageKey) {
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessRuleException("Invalid file reference");
        }
        return target;
    }
}
