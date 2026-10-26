package check4tech.firstdueresponseservice.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Guards for user-supplied file names and path segments (path traversal, unsafe types).
 */
public final class FileSafety {

    private static final Pattern SAFE_SEGMENT = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._ \\-()]{0,200}$");

    /** Types that may be uploaded/served. No html/svg/js: those would allow stored XSS. */
    private static final Map<String, String> TYPES = Map.ofEntries(
            Map.entry("pdf", "application/pdf"),
            Map.entry("png", "image/png"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("gif", "image/gif"),
            Map.entry("webp", "image/webp"),
            Map.entry("bmp", "image/bmp"),
            Map.entry("heic", "image/heic"),
            Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry("csv", "text/csv"),
            Map.entry("txt", "text/plain"));

    private static final Set<String> INLINE_OK = Set.of("pdf", "png", "jpg", "jpeg", "gif", "webp", "bmp");

    private FileSafety() {
    }

    /** A single path segment (id, folder name). Rejects "..", separators and odd characters. */
    public static String segment(String value) {
        if (value == null || !SAFE_SEGMENT.matcher(value).matches() || value.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid path value.");
        }
        return value;
    }

    /** Strips any directory part from a client file name and validates it and its extension. */
    public static String uploadName(String originalFilename) {
        if (originalFilename == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is required.");
        }
        String name = originalFilename.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).trim();
        segment(name);
        if (!TYPES.containsKey(extension(name))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File type is not allowed.");
        }
        return name;
    }

    /** Resolves a child under base and guarantees the result stays inside base. */
    public static Path resolveIn(String base, String child) {
        Path root = Path.of(base).toAbsolutePath().normalize();
        Path resolved = root.resolve(segment(child)).normalize();
        if (!resolved.startsWith(root) || resolved.equals(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path.");
        }
        return resolved;
    }

    public static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** Download response with a fixed content type, nosniff, and attachment for anything that is not a safe inline type. */
    public static ResponseEntity<Resource> download(Resource resource, String name) {
        String ext = extension(name);
        String type = TYPES.getOrDefault(ext, "application/octet-stream");
        String disposition = INLINE_OK.contains(ext) ? "inline" : "attachment";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(type))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + name.replace("\"", "") + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}
