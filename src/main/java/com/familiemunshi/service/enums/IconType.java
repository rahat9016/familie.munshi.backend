package com.familiemunshi.service.enums;

/**
 * How a product classification icon is stored in the single icon column.
 * Computed from the value, never persisted.
 */
public enum IconType {

    /** Uploaded file in S3. Column holds the storage key ("public/..."). */
    FILE,

    /** lucide-react icon name. Column holds the name ("shopping-cart"). */
    LUCIDE
}
