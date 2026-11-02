package com.familiemunshi.http.validations;

import com.familiemunshi.common.exceptions.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public final class ValidationUtil {

    private static final long DEFAULT_MAX_IMAGE_SIZE_MB = 2;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp");

    private ValidationUtil() {
        throw new IllegalStateException(
                "Utility class should not be instantiated"
        );
    }

    /**
     * Validate image with default 2 MB limit.
     */
    public static void validateMediaType(MultipartFile image) {
        validateMediaType(image, DEFAULT_MAX_IMAGE_SIZE_MB);
    }

    /**
     * Validate image with a custom maximum size in MB.
     *
     * @param image     uploaded image
     * @param maxSizeMB maximum allowed size in MB
     */
    public static void validateMediaType(
            MultipartFile image,
            long maxSizeMB
    ) {

        // 1. CHECK EMPTY FILE
        if (image == null || image.isEmpty()) {
            throw new BadRequestException("imageCannotBeEmpty");
        }

        // 2. VALIDATE CONFIGURED SIZE
        if (maxSizeMB <= 0) {
            throw new IllegalArgumentException(
                    "Maximum image size must be greater than zero"
            );
        }

        // 3. CHECK FILE SIZE
        long maxSizeBytes = Math.multiplyExact(
                maxSizeMB,
                1024L * 1024L
        );

        if (image.getSize() > maxSizeBytes) {
            throw new BadRequestException(
                    "imageTooLarge",
                    maxSizeMB
            );
        }

        // 4. CHECK FILE NAME AND CONTENT TYPE
        String filename = image.getOriginalFilename();
        String contentType = image.getContentType();

        if (filename == null || contentType == null) {
            throw new BadRequestException("unsupportedImageType");
        }

        String extension = getExtension(filename);
        String mime = contentType.toLowerCase(Locale.ROOT);

        // 5. VALIDATE EXTENSION
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("unsupportedImageType");
        }

        // 6. VALIDATE MIME TYPE AGAINST EXTENSION
        boolean validMime = switch (extension) {
            case "jpg", "jpeg" -> mime.equals("image/jpeg");
            case "png" -> mime.equals("image/png");
            case "webp" -> mime.equals("image/webp");
            default -> false;
        };

        if (!validMime) {
            throw new BadRequestException("unsupportedImageType");
        }

        // 7. VALIDATE ACTUAL FILE SIGNATURE
        if (!hasMatchingImageSignature(image, mime)) {
            throw new BadRequestException("invalidImageContent");
        }
    }

    private static String getExtension(String filename) {

        int lastDot = filename.lastIndexOf('.');

        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDot + 1)
                .toLowerCase(Locale.ROOT);
    }

    private static boolean hasMatchingImageSignature(
            MultipartFile image,
            String mime
    ) {

        try (InputStream stream = image.getInputStream()) {

            byte[] header = stream.readNBytes(12);

            return switch (mime) {
                case "image/jpeg" -> isJpeg(header);
                case "image/png" -> isPng(header);
                case "image/webp" -> isWebp(header);
                default -> false;
            };

        } catch (IOException exception) {
            throw new BadRequestException("imageReadFailed");
        }
    }

    private static boolean isJpeg(byte[] data) {

        return data.length >= 3
                && (data[0] & 0xFF) == 0xFF
                && (data[1] & 0xFF) == 0xD8
                && (data[2] & 0xFF) == 0xFF;
    }

    private static boolean isPng(byte[] data) {

        byte[] signature = {
                (byte) 0x89,
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A
        };

        if (data.length < signature.length) {
            return false;
        }

        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }

        return true;
    }

    private static boolean isWebp(byte[] data) {

        return data.length >= 12
                && matchesAscii(data, 0, "RIFF")
                && matchesAscii(data, 8, "WEBP");
    }

    private static boolean matchesAscii(
            byte[] data,
            int offset,
            String expected
    ) {

        byte[] bytes = expected.getBytes(
                StandardCharsets.US_ASCII
        );

        if (data.length < offset + bytes.length) {
            return false;
        }

        for (int i = 0; i < bytes.length; i++) {
            if (data[offset + i] != bytes[i]) {
                return false;
            }
        }

        return true;
    }
}
