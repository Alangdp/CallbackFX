package com.connectasistemas.framework.utils;

import java.util.Locale;

/**
 * Utilitários para códigos de livros
 */
public final class BookCodeUtils {

    private static final int SEQUENCE_DIGITS = 3;

    private BookCodeUtils() {
    }

    public static int sequenceDigits() {
        return SEQUENCE_DIGITS;
    }

    public static String combine(int groupCode, int sequence) {
        return String.format(Locale.ROOT, "%d%0" + SEQUENCE_DIGITS + "d", groupCode, sequence);
    }

    public static BookCodeParts split(String rawCode) {
        if (StringUtils.isBlank(rawCode)) {
            return null;
        }

        String cleaned = StringUtils.trim(rawCode);
        if (!cleaned.matches("\\d+")) {
            return null;
        }

        if (cleaned.length() <= SEQUENCE_DIGITS) {
            int group = NumberUtils.toInt(cleaned);
            if (group <= 0) {
                return null;
            }
            return new BookCodeParts(group, 0);
        }

        int splitIndex = cleaned.length() - SEQUENCE_DIGITS;
        int groupCode = NumberUtils.toInt(cleaned.substring(0, splitIndex));
        int sequence = NumberUtils.toInt(cleaned.substring(splitIndex));
        if (groupCode <= 0 || sequence < 0) {
            return null;
        }
        return new BookCodeParts(groupCode, sequence);
    }

    /**
     * Partes de um código de livro
     * OBS: Criada como classe estática para evitar a necessidade de um novo arquivo
     */
    public static final class BookCodeParts {
        private final int groupCode;
        private final int sequence;

        public BookCodeParts(int groupCode, int sequence) {
            this.groupCode = groupCode;
            this.sequence = sequence;
        }

        public int groupCode() {
            return groupCode;
        }

        public int sequence() {
            return sequence;
        }
    }
}
