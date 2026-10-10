package com.familiemunshi.common.exceptions;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

@Component
public class ErrorUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorUtils.class);
    private final MessageSource messageSource;

    public ErrorUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String resolveMessage(String errorCode, Object[] params, Locale locale) {
        try {
            return messageSource.getMessage(errorCode, params, locale);
        } catch (NoSuchMessageException e) {
            LOGGER.warn("Translatable text missing for messageKey: {}", errorCode);
            return errorCode; // Fallback to raw key if property is missing
        }
    }
}
