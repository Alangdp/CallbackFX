package com.connectasistemas.framework.utils;

public class StringUtils {

    /**
     * Substitui strings substituindo marcadores no padrão %n pelo argumento correspondente.
     * Exemplo de uso:
     * <pre>{@code
     * String resultado = StringUtils.concat("Olá, %1! Hoje é %2.", "Maria", "segunda-feira");
     * // resultado: "Olá, Maria! Hoje é segunda-feira."
     * }</pre>
     *
     * @param template String com marcadores no formato %1, %2, etc.
     * @param args Argumentos que substituirão os marcadores
     * @return String formatada com os marcadores substituídos pelos argumentos
     */
    public static String replaceParams(String template, Object... args) {
        if (template == null) return null;
        if (args == null || args.length == 0) return template;

        StringBuilder result = new StringBuilder(template.length() + 32);
        int lastIndex = 0;

        for (int i = 0; i < template.length(); i++) {
            if (template.charAt(i) == '%' && i + 1 < template.length() && Character.isDigit(template.charAt(i + 1))) {
                result.append(template, lastIndex, i);

                int argIndex = Character.getNumericValue(template.charAt(i + 1)) - 1;

                if (argIndex >= 0 && argIndex < args.length) {
                    result.append(args[argIndex] != null ? args[argIndex].toString() : "null");
                } else {
                    result.append('%').append(template.charAt(i + 1));
                }

                lastIndex = i + 2;
                i++;
            }
        }

        if (lastIndex < template.length()) {
            result.append(template, lastIndex, template.length());
        }

        return result.toString();
    }

    /**
     * Concatenação simples
     * @param parts partes da String para concatenar
     * @return String concatenada
     */
    public static String concat(Object... parts) {
        if (parts == null || parts.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        for (Object p : parts) {
            sb.append(p);
        }
        return sb.toString();
    }

    /**
     * Retorna true se a string for nula ou estiver vazia.
     *
     * @param str String a verificar
     * @return true se for nula ou vazia, senão false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Retorna true se a string for nula, vazia ou composta apenas por espaços em branco.
     *
     * @param str String a verificar
     * @return true se for nula, vazia ou somente whitespace
     */
    public static boolean isBlank(String str) {
        if (isEmpty(str)) return true;
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Converte a primeira letra da string para maiúscula.
     *
     * @param str String de entrada
     * @return String capitalizada ou a própria string se for nula/vazia
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) return str;
        int len = str.length();
        if (len == 1) return str.toUpperCase();
        if (len > 1) return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        return str;
    }

    /**
     * Remove espaços em branco do início e do fim.
     *
     * @param str String de entrada
     * @return String sem espaços externos ou null se a entrada for null
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * Converte a string para letras minúsculas.
     *
     * @param str String de entrada
     * @return String em minúsculas ou "" se str for null
     */
    public static String lowerCase(String str) {
        return str == null ? "" : str.toLowerCase();
    }

}
