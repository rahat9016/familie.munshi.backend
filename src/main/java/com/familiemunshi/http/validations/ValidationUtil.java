package com.familiemunshi.http.validations;

import com.familiemunshi.common.exceptions.BadRequestException;
import com.familiemunshi.common.utils.Util;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final int MIN_BRANCH_ADMIN_PASSWORD_LENGTH = 8;
    private static final long DEFAULT_MAX_IMAGE_SIZE_MB = 2;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp");

    private static final Set<String> ICON_EXTENSIONS =
            Set.of("svg", "png", "webp", "jpg", "jpeg");

    private static final Set<String> SVG_FORBIDDEN_ELEMENTS = Set.of(
            "script", "foreignobject", "iframe", "embed", "object",
            "audio", "video", "handler", "listener"
    );

    // url(...) that does not point to a local #fragment.
    private static final Pattern SVG_EXTERNAL_URL =
            Pattern.compile("url\\(['\"]?(?!#)");

    /*
     * lucide-react icon names: kebab-case ("shopping-cart", "arrow-up-1-0")
     * or PascalCase component names ("ShoppingCart").
     * No '/' allowed, so a name can never look like an S3 key.
     */
    public static final String LUCIDE_ICON_NAME_REGEX =
            "^(?:[a-z0-9]+(?:-[a-z0-9]+)*|[A-Z][A-Za-z0-9]*)$";

    private static final Pattern LUCIDE_ICON_NAME =
            Pattern.compile(LUCIDE_ICON_NAME_REGEX);

    private static final int LUCIDE_ICON_MAX_LENGTH = 100;

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

    /**
     * Validate an icon upload: SVG, PNG, WEBP or JPG up to {@code maxSizeKB}.
     * SVG content is parsed (XXE-safe) and rejected if it contains scripts,
     * event handlers, external references or a DOCTYPE.
     */
    public static void validateIcon(
            MultipartFile icon,
            long maxSizeKB
    ) {

        // 1. CHECK EMPTY FILE
        if (icon == null || icon.isEmpty()) {
            throw new BadRequestException("iconCannotBeEmpty");
        }

        // 2. VALIDATE CONFIGURED SIZE
        if (maxSizeKB <= 0) {
            throw new IllegalArgumentException(
                    "Maximum icon size must be greater than zero"
            );
        }

        // 3. CHECK FILE SIZE
        if (icon.getSize() > Math.multiplyExact(maxSizeKB, 1024L)) {
            throw new BadRequestException("iconTooLarge", maxSizeKB);
        }

        // 4. CHECK FILE NAME AND CONTENT TYPE
        String filename = icon.getOriginalFilename();
        String contentType = icon.getContentType();

        if (filename == null || contentType == null) {
            throw new BadRequestException("unsupportedIconType");
        }

        String extension = getExtension(filename);
        String mime = contentType
                .toLowerCase(Locale.ROOT)
                .split(";")[0]
                .trim();

        // 5. VALIDATE EXTENSION
        if (!ICON_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("unsupportedIconType");
        }

        // 6. VALIDATE MIME TYPE AGAINST EXTENSION
        boolean validMime = switch (extension) {
            case "svg" -> mime.equals("image/svg+xml");
            case "jpg", "jpeg" -> mime.equals("image/jpeg");
            case "png" -> mime.equals("image/png");
            case "webp" -> mime.equals("image/webp");
            default -> false;
        };

        if (!validMime) {
            throw new BadRequestException("unsupportedIconType");
        }

        // 7. VALIDATE ACTUAL CONTENT
        if (extension.equals("svg")) {
            validateSvgContent(icon);
        } else if (!hasMatchingImageSignature(icon, mime)) {
            throw new BadRequestException("invalidIconContent");
        }
    }

    /**
     * Validate a lucide-react icon name. Blank is allowed (means no icon).
     */
    public static void validateLucideIconName(String name) {

        if (name == null || name.isBlank()) {
            return;
        }

        String trimmed = name.trim();

        if (trimmed.length() > LUCIDE_ICON_MAX_LENGTH
                || !LUCIDE_ICON_NAME.matcher(trimmed).matches()) {
            throw new BadRequestException("invalidLucideIconName", trimmed);
        }
    }

    private static void validateSvgContent(MultipartFile file) {

        Document document;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());

            document = builder.parse(
                    new ByteArrayInputStream(file.getBytes())
            );
        } catch (ParserConfigurationException exception) {
            throw new IllegalStateException(
                    "SVG parser is not available",
                    exception
            );
        } catch (SAXException | IOException exception) {
            throw new BadRequestException("invalidIconContent");
        }

        // <?xml-stylesheet ...?> and similar can pull in external content.
        NodeList topLevel = document.getChildNodes();

        for (int i = 0; i < topLevel.getLength(); i++) {
            if (topLevel.item(i).getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                throw new BadRequestException("invalidIconContent");
            }
        }

        Element root = document.getDocumentElement();

        if (root == null || !"svg".equalsIgnoreCase(root.getLocalName())) {
            throw new BadRequestException("invalidIconContent");
        }

        validateSvgElement(root);
    }

    private static void validateSvgElement(Element element) {

        String name = String.valueOf(element.getLocalName())
                .toLowerCase(Locale.ROOT);

        if (SVG_FORBIDDEN_ELEMENTS.contains(name)) {
            throw new BadRequestException("invalidIconContent");
        }

        if (name.equals("style") && isUnsafeSvgValue(element.getTextContent())) {
            throw new BadRequestException("invalidIconContent");
        }

        NamedNodeMap attributes = element.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {

            Node attribute = attributes.item(i);

            String attributeName = String.valueOf(
                    attribute.getLocalName() != null
                            ? attribute.getLocalName()
                            : attribute.getNodeName()
            ).toLowerCase(Locale.ROOT);

            String value = attribute.getNodeValue() == null
                    ? ""
                    : attribute.getNodeValue().trim();

            // onclick, onload, ...
            if (attributeName.startsWith("on")) {
                throw new BadRequestException("invalidIconContent");
            }

            // href / xlink:href may only point inside the document.
            if (attributeName.equals("href")
                    && !value.isEmpty()
                    && !value.startsWith("#")) {
                throw new BadRequestException("invalidIconContent");
            }

            if (isUnsafeSvgValue(value)) {
                throw new BadRequestException("invalidIconContent");
            }
        }

        NodeList children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element child) {
                validateSvgElement(child);
            }
        }
    }

    private static boolean isUnsafeSvgValue(String value) {

        if (value == null || value.isEmpty()) {
            return false;
        }

        String normalized = value
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "");

        return normalized.contains("javascript:")
                || normalized.contains("vbscript:")
                || normalized.contains("@import")
                || normalized.contains("expression(")
                || SVG_EXTERNAL_URL.matcher(normalized).find();
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

    public static void validateBranchAdminPassword(
            String commonPassword
    ) {

        if (commonPassword == null
                || commonPassword.isBlank()
                || commonPassword.length()
                < MIN_BRANCH_ADMIN_PASSWORD_LENGTH) {

            throw new IllegalStateException(
                    "Branch Admin common password must "
                            + "contain at least "
                            + MIN_BRANCH_ADMIN_PASSWORD_LENGTH
                            + " characters"
            );
        }
    }


    public static void validateNewBranchAdminPassword(
            String newPassword,
            String currentPasswordHash
    ) {

        if (newPassword == null
                || newPassword.isBlank()
                || newPassword.length() < 8
                || newPassword.length() > 72
                || newPassword.getBytes(
                StandardCharsets.UTF_8
        ).length > 72) {

            throw new BadRequestException(
                    "invalidNewBranchAdminPassword"
            );
        }

        if (Util.verifyPassword(
                newPassword,
                currentPasswordHash
        )) {
            throw new BadRequestException(
                    "newPasswordMustBeDifferent"
            );
        }
    }

}
