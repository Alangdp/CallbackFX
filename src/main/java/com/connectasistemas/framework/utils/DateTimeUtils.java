package com.connectasistemas.framework.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utilitário para lidar com formatação e normalização de timestamps usados no banco.
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter DB_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");

    private DateTimeUtils() {
    }

    /**
     * Retorna a data/hora atual formatada conforme o padrão do banco.
     */
    public static String currentTimestamp() {
        return format(LocalDateTime.now());
    }

    /**
     * Formata um {@link LocalDateTime} no padrão adotado pelo banco.
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DB_TIMESTAMP_FORMAT);
    }

    /**
     * Retorna um timestamp futuro com base na data/hora atual acrescida de dias.
     */
    public static String timestampAfterDays(int days) {
        return format(LocalDateTime.now().plusDays(days));
    }

    /**
     * Soma dias ao timestamp informado (caso válido) e devolve já normalizado.
     */
    public static String addDays(String timestamp, int days) {
        LocalDateTime base = tryParse(timestamp, DB_TIMESTAMP_FORMAT);
        if (base == null) {
            base = tryParse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (base == null) {
            return normalizeTimestamp(timestamp);
        }
        return format(base.plusDays(days));
    }

    /**
     * Normaliza uma string para o padrão de timestamp aceito pelo banco.
     * Aceita valores já formatados, em ISO (com "T") ou em epoch millis.
     */
    public static String normalizeTimestamp(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        if (DIGITS_ONLY.matcher(trimmed).matches()) {
            try {
                long epochMillis = Long.parseLong(trimmed);
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
                return format(dateTime);
            } catch (NumberFormatException ignored) {
                return trimmed;
            }
        }

        LocalDateTime parsed = tryParse(trimmed, DB_TIMESTAMP_FORMAT);
        if (parsed != null) {
            return format(parsed);
        }

        parsed = tryParse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (parsed != null) {
            return format(parsed);
        }

        return trimmed;
    }

    private static LocalDateTime tryParse(String value, DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        try {
            return LocalDateTime.parse(value, formatter);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
