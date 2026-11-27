package com.familiemunshi.http.validations;

import com.familiemunshi.common.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilIconTest {

    private static final long MAX_KB = 512;

    private static MockMultipartFile svg(String content) {
        return new MockMultipartFile(
                "iconFile", "icon.svg", "image/svg+xml",
                content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void cleanSvg_passes() {
        String content = """
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                  <defs><linearGradient id="g"/></defs>
                  <path d="M0 0h24v24H0z" fill="url(#g)"/>
                </svg>
                """;
        assertThatCode(() -> ValidationUtil.validateIcon(svg(content), MAX_KB))
                .doesNotThrowAnyException();
    }

    @Test
    void png_passes() {
        byte[] png = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
        assertThatCode(() -> ValidationUtil.validateIcon(
                new MockMultipartFile("iconFile", "icon.png", "image/png", png), MAX_KB))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>",
            "<svg xmlns=\"http://www.w3.org/2000/svg\" onload=\"alert(1)\"/>",
            "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">"
                    + "<a xlink:href=\"javascript:alert(1)\"><path d=\"M0 0\"/></a></svg>",
            "<svg xmlns=\"http://www.w3.org/2000/svg\"><image href=\"https://evil.test/x.png\"/></svg>",
            "<svg xmlns=\"http://www.w3.org/2000/svg\"><foreignObject><div/></foreignObject></svg>",
            "<svg xmlns=\"http://www.w3.org/2000/svg\"><style>@import url(https://evil.test/a.css);</style></svg>",
            "<!DOCTYPE svg [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><svg xmlns=\"http://www.w3.org/2000/svg\">&x;</svg>",
            "<html><body/></html>",
            "not xml"
    })
    void unsafeOrInvalidSvg_rejected(String content) {
        assertThatThrownBy(() -> ValidationUtil.validateIcon(svg(content), MAX_KB))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void oversizeIcon_rejected() {
        byte[] big = new byte[(int) (MAX_KB * 1024) + 1];
        assertThatThrownBy(() -> ValidationUtil.validateIcon(
                new MockMultipartFile("iconFile", "icon.svg", "image/svg+xml", big), MAX_KB))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void wrongMime_rejected() {
        assertThatThrownBy(() -> ValidationUtil.validateIcon(
                new MockMultipartFile("iconFile", "icon.svg", "text/plain",
                        "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(StandardCharsets.UTF_8)),
                MAX_KB))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void wrongExtension_rejected() {
        assertThatThrownBy(() -> ValidationUtil.validateIcon(
                new MockMultipartFile("iconFile", "icon.gif", "image/gif", new byte[]{1}), MAX_KB))
                .isInstanceOf(BadRequestException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"shirt", "shopping-cart", "arrow-up-1-0", "ShoppingCart", ""})
    void validLucideNames_pass(String name) {
        assertThatCode(() -> ValidationUtil.validateLucideIconName(name))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"public/x.svg", "../etc", "shopping_cart", "-shirt", "shirt-", "<svg>", "https://x.y/z"})
    void invalidLucideNames_rejected(String name) {
        assertThatThrownBy(() -> ValidationUtil.validateLucideIconName(name))
                .isInstanceOf(BadRequestException.class);
    }
}
