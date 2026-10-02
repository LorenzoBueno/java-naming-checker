package br.pucrs.plp;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class NamingUtils {
    private NamingUtils() {}

    private static final Pattern PASCAL = Pattern.compile("^[A-Z][a-zA-Z0-9]*$");
    private static final Pattern CAMEL = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern UPPER_SNAKE = Pattern.compile("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$");

    public static boolean isPascal(String s) { return PASCAL.matcher(s).matches(); }
    public static boolean isCamel(String s) { return CAMEL.matcher(s).matches(); }
    public static boolean isUpperSnake(String s) { return UPPER_SNAKE.matcher(s).matches(); }

    // "Calcular_Total" -> [calcular, total] | "maxItens" -> [max, itens]
    private static List<String> palavras(String s) {
        String t = s.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                    .replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        return Arrays.stream(t.split("_+"))
                .filter(p -> !p.isEmpty())
                .map(String::toLowerCase)
                .toList();
    }

    private static String cap(String p) {
        return Character.toUpperCase(p.charAt(0)) + p.substring(1);
    }

    public static String toPascal(String s) {
        return palavras(s).stream().map(NamingUtils::cap).collect(Collectors.joining());
    }

    public static String toCamel(String s) {
        String p = toPascal(s);
        if (p.isEmpty()) return s;
        return Character.toLowerCase(p.charAt(0)) + p.substring(1);
    }

    public static String toUpperSnake(String s) {
        return String.join("_", palavras(s)).toUpperCase();
    }
}