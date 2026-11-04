
package com.familiemunshi.common.utils;

import java.text.Normalizer;
import java.util.Locale;

public final class BranchAdminGenerator {

    private BranchAdminGenerator() {
        throw new IllegalStateException(
                "Utility class cannot be instantiated"
        );
    }

    public static String generateEmail(
            String branchName,
            String branchCode,
            String emailDomain
    ) {

        if (branchName == null || branchName.isBlank()) {
            throw new IllegalArgumentException(
                    "Branch name is required"
            );
        }

        if (branchCode == null || branchCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Branch code is required"
            );
        }

        if (emailDomain == null || emailDomain.isBlank()) {
            throw new IllegalArgumentException(
                    "Email domain is required"
            );
        }

        String slug = Normalizer.normalize(
                        branchName.trim(),
                        Normalizer.Form.NFKD
                )
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            slug = "branch";
        }

        if (slug.length() > 45) {
            slug = slug.substring(0, 45)
                    .replaceAll("-+$", "");
        }

        String normalizedCode = branchCode.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]", "");

        String domain = emailDomain.trim()
                .toLowerCase(Locale.ROOT)
                .replaceFirst("^@", "");

        String email = slug + "." + normalizedCode + "@" + domain;

        if (email.length() > 150) {
            throw new IllegalArgumentException(
                    "Generated email exceeds 150 characters"
            );
        }

        return email;
    }
}
