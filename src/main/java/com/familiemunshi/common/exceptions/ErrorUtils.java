package com.familiemunshi.common.exceptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ErrorUtils {
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorUtils.class);
    private final MessageSource messageSource;


    public String resolveMessage(String errorCode, Object[] params, Locale locale) {
        try {
            return messageSource.getMessage(errorCode, params, locale);
        } catch (NoSuchMessageException e) {
            LOGGER.warn("Translatable text missing for messageKey: {}", errorCode);
            return errorCode; // Fallback to raw key if property is missing
        }
    }

    public void writeErrorResponse(
            HttpServletResponse response,
            int status,
            String code,
            String message
    ) throws IOException {

        ErrorResponse error = ErrorResponse.of(
                status,
                code,
                message
        );

        response.setStatus(status);
        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );
        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                error
        );
    }
}
