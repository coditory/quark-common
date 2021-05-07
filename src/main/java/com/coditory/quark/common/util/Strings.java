package com.coditory.quark.common.util;

import com.coditory.quark.common.throwable.ThrowingSupplier;
import org.jetbrains.annotations.Nullable;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static com.coditory.quark.common.check.Args.checkNotNegative;
import static com.coditory.quark.common.check.Args.checkNotNull;
import static com.coditory.quark.common.check.Args.checkPositive;

public class Strings {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final Pattern STRIP_ACCENTS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final int PAD_LIMIT = 8192;

    private Strings() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    public static String[] emptyStringArray() {
        return EMPTY_STRING_ARRAY;
    }

    public static String quote(String text) {
        return quote(text, '"');
    }

    public static String singleQuote(String text) {
        return quote(text, '\'');
    }

    public static String quote(String text, char c) {
        checkNotNull(text, "text");
        return c + escapeQuotes(text, c) + c;
    }

    public static boolean isQuoted(String text) {
        return isQuoted(text, '"');
    }

    public static boolean isSingleQuoted(String text) {
        return isQuoted(text, '\'');
    }

    public static boolean isAnyQuoted(String text) {
        return isQuoted(text) || isSingleQuoted(text);
    }

    public static boolean isQuoted(String text, char quote) {
        checkNotNull(text, "text");
        if (text.length() < 2) {
            return false;
        }
        return text.charAt(0) == quote
                && text.charAt(text.length() - 1) == quote;
    }

    public static String escapeQuotes(String value) {
        return escapeQuotes(value, '"');
    }

    public static String escapeSingleQuotes(String value) {
        return escapeQuotes(value, '\'');
    }

    public static String escapeQuotes(String text, char quote) {
        checkNotNull(text, "text");
        return text.indexOf(quote) == -1
                ? text
                : replace(text, "" + quote, "\\" + quote);
    }

    public static String unquote(String text) {
        return unquote(text, '"');
    }

    public static String unquoteAny(String text) {
        if (isQuoted(text, '"')) {
            return unquote(text);
        }
        return unquoteSingle(text);
    }

    public static String unquoteSingle(String text) {
        return unquote(text, '\'');
    }

    public static String unquote(String text, char quote) {
        checkNotNull(text, "text");
        if (!isQuoted(text, quote)) {
            return text;
        }
        String content = text.substring(1, text.length() - 1);
        return unescapeQuotations(content, quote);
    }

    public static String unescapeQuotations(String value) {
        return unescapeQuotations(value, '"');
    }

    public static String unescapeSingleQuotations(String value) {
        return unescapeQuotations(value, '\'');
    }

    public static String unescapeQuotations(String text, char quote) {
        checkNotNull(text, "text");
        String escapedQuote = "\\" + quote;
        return !text.contains(escapedQuote)
                ? text
                : replace(text, escapedQuote, "" + quote);
    }

    public static String multiline(String... text) {
        return String.join(System.lineSeparator(), text);
    }

    public static String multiline(List<String> text) {
        return String.join(System.lineSeparator(), text);
    }

    public static int maxLength(List<String> text) {
        return text.stream()
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    public static int maxLength(String... text) {
        return maxLength(Arrays.asList(text));
    }

    public static int minLength(List<String> text) {
        return text.stream()
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .min()
                .orElse(0);
    }

    public static int minLength(String... text) {
        return minLength(Arrays.asList(text));
    }

    public static String capitalize(String text) {
        checkNotNull(text, "text");
        if (text.isBlank()) {
            return text;
        }
        int firstLetter = Character.toUpperCase(text.codePointAt(0));
        int firstLetterChars = Character.charCount(text.codePointAt(0));
        return new StringBuilder(text.length())
                .appendCodePoint(firstLetter)
                .append(text.substring(firstLetterChars))
                .toString();
    }

    public static int count(String text, String part) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        if (part == null || part.isEmpty()) {
            return 0;
        }
        int counter = 0;
        int offset = 0;
        int index = text.indexOf(part, offset);
        while (index >= 0) {
            counter++;
            offset = index + part.length();
            index = text.indexOf(part, offset);
        }
        return counter;
    }

    public static int count(String text, char c) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int counter = 0;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == c) {
                counter++;
            }
        }
        return counter;
    }

    public static String indent(String text, int spaces) {
        return indent(text, spaces, ' ');
    }

    public static String indent(String text, int spaces, char indentCharacter) {
        checkNotNull(text, "text");
        checkPositive(spaces, "spaces");
        StringBuilder builder = new StringBuilder();
        String indent = repeat(indentCharacter, spaces);
        String newLine = text.contains("\r\n")
                ? "\r\n"
                : "\n";
        String[] lines = text.split("\r?\n", -1);
        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i];
            if (!line.isEmpty()) {
                builder.append(indent);
                builder.append(line);
            }
            if (i < lines.length - 1) {
                builder.append(newLine);
            }
        }
        return builder.toString();
    }

    public static String appendIfMissing(String text, String suffix) {
        checkNotNull(text, "text");
        checkNotNull(suffix, "suffix");
        return endsWith(text, suffix)
                ? text
                : text + suffix;
    }

    public static String appendIfMissingIgnoreCase(String text, String suffix) {
        checkNotNull(text, "text");
        checkNotNull(suffix, "suffix");
        return endsWithIgnoreCase(text, suffix)
                ? text
                : text + suffix;
    }

    public static String prependIfMissing(String text, String prefix) {
        checkNotNull(text, "text");
        checkNotNull(prefix, "prefix");
        return startsWith(text, prefix)
                ? text
                : prefix + text;
    }

    public static String prependIfMissingIgnoreCase(String text, String prefix) {
        checkNotNull(text, "text");
        checkNotNull(prefix, "prefix");
        return startsWithIgnoreCase(text, prefix)
                ? text
                : prefix + text;
    }

    public static String getCommonPrefix(String... texts) {
        checkNotNull(texts, "texts");
        final int smallestIndexOfDiff = getDiffIndex(texts);
        if (smallestIndexOfDiff == -1) {
            return texts[0] == null
                    ? ""
                    : texts[0];
        }
        if (smallestIndexOfDiff == 0) {
            return "";
        }
        return texts[0].substring(0, smallestIndexOfDiff);
    }

    public static int getDiffIndex(String... texts) {
        checkNotNull(texts, "texts");
        boolean anyStringNull = false;
        boolean allStringsNull = true;
        int arrayLen = texts.length;
        int shortestStrLen = Integer.MAX_VALUE;
        int longestStrLen = 0;
        for (String text : texts) {
            if (text == null) {
                anyStringNull = true;
                shortestStrLen = 0;
            } else {
                allStringsNull = false;
                shortestStrLen = Math.min(text.length(), shortestStrLen);
                longestStrLen = Math.max(text.length(), longestStrLen);
            }
        }
        if (allStringsNull || longestStrLen == 0 && !anyStringNull) {
            return -1;
        }
        if (shortestStrLen == 0) {
            return 0;
        }
        int firstDiff = -1;
        for (int stringPos = 0; stringPos < shortestStrLen; stringPos++) {
            final char comparisonChar = texts[0].charAt(stringPos);
            for (int arrayPos = 1; arrayPos < arrayLen; arrayPos++) {
                if (texts[arrayPos].charAt(stringPos) != comparisonChar) {
                    firstDiff = stringPos;
                    break;
                }
            }
            if (firstDiff != -1) {
                break;
            }
        }
        return (firstDiff == -1 && shortestStrLen != longestStrLen)
                ? shortestStrLen
                : firstDiff;
    }

    static boolean isValidSurrogatePairAt(String text, int index) {
        checkNotNull(text, "text");
        return index >= 0
                && index <= (text.length() - 2)
                && Character.isHighSurrogate(text.charAt(index))
                && Character.isLowSurrogate(text.charAt(index + 1));
    }

    public static List<String> splitLines(String text) {
        return Arrays.asList(text.split("\r?\n", -1));
    }

    public static String compactAllSpaces(String text) {
        return compact(text, Character::isWhitespace);
    }

    public static String compactSpaces(String text) {
        return compact(text, (c) -> c == ' ');
    }

    public static String compact(String text, Predicate<Character> predicate) {
        checkNotNull(text, "text");
        checkNotNull(predicate, "predicate");
        if (text.isEmpty()) {
            return text;
        }
        int size = text.length();
        char[] newChars = new char[size];
        int count = 0;
        int whitespacesCount = 0;
        boolean startWhitespaces = true;
        for (int i = 0; i < size; i++) {
            char actualChar = text.charAt(i);
            boolean isWhitespace = predicate.test(actualChar);
            if (isWhitespace) {
                if (whitespacesCount == 0 && !startWhitespaces) {
                    newChars[count++] = ' ';
                }
                whitespacesCount++;
            } else {
                startWhitespaces = false;
                newChars[count++] = (actualChar == 160 ? 32 : actualChar);
                whitespacesCount = 0;
            }
        }
        if (startWhitespaces) {
            return "";
        }
        return new String(newChars, 0, count - (whitespacesCount > 0 ? 1 : 0)).trim();
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.isEmpty();
    }

    @Nullable
    public static String emptyToNull(String text) {
        return isNullOrEmpty(text) ? null : text;
    }

    public static String nullToEmpty(@Nullable String text) {
        return text == null ? "" : text;
    }

    public static boolean isNotEmpty(String text) {
        return !isNullOrEmpty(text);
    }

    public static String firstNonEmpty(String... texts) {
        String result = firstNonEmptyOrNull(texts);
        if (result == null) {
            throw new IllegalArgumentException("Expected at least one non empty element");
        }
        return result;
    }

    @Nullable
    public static String firstNonEmptyOrNull(String... texts) {
        for (String text : texts) {
            if (isNotEmpty(text)) {
                return text;
            }
        }
        return null;
    }

    public static boolean isNullOrBlank(String text) {
        return text == null || text.isBlank();
    }

    @Nullable
    public static String blankToNull(String text) {
        return isNullOrBlank(text) ? null : text;
    }

    public static boolean isNotBlank(String text) {
        return !isNullOrBlank(text);
    }

    public static String firstNonBlank(String... texts) {
        String result = firstNonBlankOrNull(texts);
        if (result == null) {
            throw new IllegalArgumentException("Expected at least one non blank element");
        }
        return result;
    }

    @Nullable
    public static String firstNonBlankOrNull(String... texts) {
        for (String text : texts) {
            if (isNotBlank(text)) {
                return text;
            }
        }
        return null;
    }

    public static String upperCase(String text) {
        checkNotNull(text, "text");
        return text.toUpperCase(Locale.ROOT);
    }

    public static String lowerCase(String text) {
        checkNotNull(text, "text");
        return text.toLowerCase(Locale.ROOT);
    }

    public static boolean containsNone(String text, List<String> parts) {
        checkNotNull(text, "text");
        checkNotNull(parts, "parts");
        if (text.isEmpty() || parts.isEmpty()) {
            return true;
        }
        return parts.stream()
                .noneMatch(text::contains);
    }

    public static boolean containsNoneChar(String text, String chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        return containsNone(text, chars.toCharArray());
    }

    public static boolean containsNone(String text, char[] chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        if (text.isEmpty() || chars.length == 0) {
            return true;
        }
        for (char c : chars) {
            if (text.indexOf(c) >= 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsNoneIgnoreCase(String text, List<String> parts) {
        checkNotNull(text, "text");
        checkNotNull(parts, "parts");
        if (text.isEmpty() || parts.isEmpty()) {
            return true;
        }
        return parts.stream()
                .noneMatch(s -> containsIgnoreCase(text, s));
    }

    public static boolean containsNoneCharIgnoreCase(String text, String chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        return containsNoneIgnoreCase(text, chars.toCharArray());
    }

    public static boolean containsNoneIgnoreCase(String text, char[] chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        if (text.isEmpty() || chars.length == 0) {
            return true;
        }
        for (char c : chars) {
            if (text.indexOf(Character.toLowerCase(c)) >= 0 || text.indexOf(Character.toUpperCase(c)) >= 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsAny(String text, List<String> parts) {
        checkNotNull(text, "text");
        checkNotNull(parts, "parts");
        if (parts.isEmpty()) {
            return true;
        }
        return parts.stream()
                .anyMatch(text::contains);
    }

    public static boolean containsAnyChar(String text, String chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        return containsAny(text, chars.toCharArray());
    }

    public static boolean containsAny(String text, char[] chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        if (chars.length == 0) {
            return true;
        }
        for (char c : chars) {
            if (text.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAnyIgnoreCase(String text, List<String> parts) {
        checkNotNull(text, "text");
        checkNotNull(parts, "parts");
        if (parts.isEmpty()) {
            return true;
        }
        return parts.stream()
                .anyMatch(s -> containsIgnoreCase(text, s));
    }

    public static boolean containsAnyCharIgnoreCase(String text, String chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        return containsAnyIgnoreCase(text, chars.toCharArray());
    }

    public static boolean containsAnyIgnoreCase(String text, char[] chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        if (chars.length == 0) {
            return true;
        }
        for (char c : chars) {
            if (text.indexOf(Character.toLowerCase(c)) >= 0 || text.indexOf(Character.toUpperCase(c)) >= 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAll(String text, List<String> parts) {
        checkNotNull(text, "text");
        checkNotNull(parts, "parts");
        if (parts.isEmpty()) {
            return true;
        }
        return parts.stream()
                .allMatch(text::contains);
    }

    public static boolean containsAllChars(String text, String chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        return containsAll(text, chars.toCharArray());
    }

    public static boolean containsAll(String text, char[] chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        if (chars.length == 0) {
            return true;
        }
        for (char c : chars) {
            if (text.indexOf(c) < 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsAllIgnoreCase(String text, List<String> parts) {
        checkNotNull(text, "text");
        checkNotNull(parts, "parts");
        if (parts.isEmpty()) {
            return true;
        }
        return parts.stream()
                .allMatch(s -> containsIgnoreCase(text, s));
    }

    public static boolean containsAllIgnoreCase(String text, String chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        return containsAllIgnoreCase(text, chars.toCharArray());
    }

    public static boolean containsAllIgnoreCase(String text, char[] chars) {
        checkNotNull(text, "text");
        checkNotNull(chars, "chars");
        if (chars.length == 0) {
            return true;
        }
        for (char c : chars) {
            if (text.indexOf(Character.toLowerCase(c)) < 0 && text.indexOf(Character.toUpperCase(c)) < 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsAllInOrder(String text, List<String> parts) {
        return containsAllInOrder(text, parts, false);
    }

    public static boolean containsAllIgnoreCaseInOrder(String text, List<String> parts) {
        return containsAllInOrder(text, parts, true);
    }

    private static boolean containsAllInOrder(String text, List<String> parts, boolean ignoreCase) {
        checkNotNull(text, "text");
        checkNotNull(parts, "parts");
        if (parts.isEmpty()) {
            return true;
        }
        int lastIndex = 0;
        for (String part : parts) {
            int index = ignoreCase
                    ? indexOfIgnoreCase(text, part, lastIndex)
                    : text.indexOf(part, lastIndex);
            if (index < 0) {
                return false;
            }
            lastIndex = index + part.length();
        }
        return true;
    }

    public static boolean containsIgnoreCase(String text, String part) {
        checkNotNull(text, "text");
        checkNotNull(part, "part");
        int len = part.length();
        int max = text.length() - len;
        for (int i = 0; i <= max; i++) {
            if (text.regionMatches(true, i, part, 0, len)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsWhitespaces(String text) {
        checkNotNull(text, "text");
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static int indexOfIgnoreCase(String text, String part) {
        return indexOfIgnoreCase(text, part, 0);
    }

    private static int indexOfIgnoreCase(String text, String part, int from) {
        checkNotNull(text, "text");
        checkNotNull(part, "part");
        checkNotNegative(from, "from");
        int endLimit = text.length() - part.length() + 1;
        if (from > endLimit) {
            return -1;
        }
        if (part.length() == 0) {
            return Math.max(0, Math.min(text.length() - 1, from));
        }
        for (int i = from; i < endLimit; i++) {
            if (text.regionMatches(true, i, part, 0, part.length())) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOfIgnoreCase(String text, String part) {
        checkNotNull(text, "text");
        checkNotNull(part, "part");
        if (part.length() == 0) {
            return Math.max(0, text.length() - 1);
        }
        int endLimit = text.length() - part.length() + 1;
        for (int i = endLimit; i >= 0; i--) {
            if (text.regionMatches(true, i, part, 0, part.length())) {
                return i;
            }
        }
        return -1;
    }

    public static boolean startsWithIgnoreCase(String text, String prefix) {
        return startsWith(text, prefix, true);
    }

    public static boolean startsWith(String text, String prefix) {
        return startsWith(text, prefix, false);
    }

    private static boolean startsWith(String text, String prefix, boolean ignoreCase) {
        checkNotNull(text, "text");
        checkNotNull(prefix, "prefix");
        if (prefix.length() > text.length()) {
            return false;
        }
        return text.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
    }

    public static boolean endsWithIgnoreCase(String text, String suffix) {
        return endsWith(text, suffix, true);
    }

    public static boolean endsWith(String text, String suffix) {
        return endsWith(text, suffix, false);
    }

    private static boolean endsWith(String text, String suffix, boolean ignoreCase) {
        checkNotNull(text, "text");
        checkNotNull(suffix, "suffix");
        if (suffix.length() > text.length()) {
            return false;
        }
        int strOffset = text.length() - suffix.length();
        return text.regionMatches(ignoreCase, strOffset, suffix, 0, suffix.length());
    }

    public static String defaultIfBlank(String text, String defaultValue) {
        return isNullOrBlank(text) ? defaultValue : text;
    }

    public static String defaultIfBlank(String text, ThrowingSupplier<String> defaultValue) {
        return isNullOrBlank(text) ? defaultValue.getWithSneakyThrow() : text;
    }

    public static String defaultIfEmpty(String text, String defaultValue) {
        return isNullOrEmpty(text) ? defaultValue : text;
    }

    public static String defaultIfEmpty(String text, ThrowingSupplier<String> defaultValue) {
        return isNullOrEmpty(text) ? defaultValue.getWithSneakyThrow() : text;
    }

    public static String leftPad(String text, int size) {
        return leftPad(text, size, ' ');
    }

    public static String leftPad(String text, int size, char padChar) {
        checkNotNull(text, "text");
        checkNotNegative(size, "size");
        int pads = size - text.length();
        if (pads <= 0) {
            return text;
        }
        if (pads > PAD_LIMIT) {
            return leftPad(text, size, String.valueOf(padChar));
        }
        return repeat(padChar, pads).concat(text);
    }

    public static String leftPad(String text, int size, String padText) {
        checkNotNull(text, "text");
        checkNotNegative(size, "size");
        checkNotNull(padText, "padText");
        if (padText.isEmpty()) {
            return text;
        }
        int padLen = padText.length();
        int strLen = text.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return text;
        }
        if (padText.length() == 1 && pads <= PAD_LIMIT) {
            return leftPad(text, size, padText.charAt(0));
        }
        if (pads == padLen) {
            return padText.concat(text);
        }
        char[] padding = new char[pads];
        char[] padChars = padText.toCharArray();
        for (int i = 0; i < pads; i++) {
            padding[i] = padChars[i % padLen];
        }
        return new String(padding).concat(text);
    }

    public static boolean isWrapped(String text, char c) {
        checkNotNull(text, "text");
        return text.length() >= 2
                && text.charAt(0) == c
                && text.charAt(text.length() - 1) == c;
    }

    public static String unwrap(String text, char c) {
        checkNotNull(text, "text");
        return isWrapped(text, c)
                ? text.substring(1, text.length() - 1)
                : text;
    }

    public static String wrap(String text, char c) {
        checkNotNull(text, "text");
        return c + text + c;
    }

    public static String wrapNonEmpty(String text, char c) {
        checkNotNull(text, "text");
        return text.isEmpty()
                ? text
                : c + text + c;
    }

    public static String repeat(char ch, int count) {
        checkNotNegative(count, "count");
        if (count == 0) {
            return "";
        }
        char[] buf = new char[count];
        for (int i = count - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    public static String repeat(String text, int count) {
        checkNotNull(text, "text");
        checkNotNegative(count, "count");
        if (count == 0) {
            return "";
        }
        int inputLength = text.length();
        if (count == 1 || inputLength == 0) {
            return text;
        }
        if (inputLength == 1 && count <= PAD_LIMIT) {
            return repeat(text.charAt(0), count);
        }

        int outputLength = inputLength * count;
        switch (inputLength) {
            case 1:
                return repeat(text.charAt(0), count);
            case 2:
                char ch0 = text.charAt(0);
                char ch1 = text.charAt(1);
                char[] output2 = new char[outputLength];
                for (int i = count * 2 - 2; i >= 0; i--, i--) {
                    output2[i] = ch0;
                    output2[i + 1] = ch1;
                }
                return new String(output2);
            default:
                StringBuilder buf = new StringBuilder(outputLength);
                for (int i = 0; i < count; i++) {
                    buf.append(text);
                }
                return buf.toString();
        }
    }

    public static String repeat(String text, String separator, int repeat) {
        checkNotNull(text, "text");
        checkNotNull(separator, "separator");
        if (repeat == 1) {
            return text;
        }
        String result = repeat(text + separator, repeat - 1);
        return result + text;
    }

    public static String removeSuffix(String text, String suffix) {
        checkNotNull(text, "text");
        checkNotNull(suffix, "suffix");
        if (suffix.isEmpty()) {
            return text;
        }
        return text.endsWith(suffix)
                ? text.substring(0, text.length() - suffix.length())
                : text;
    }

    public static String removeSuffix(String text, int count) {
        checkNotNull(text, "text");
        checkNotNegative(count, "count");
        int newLength = Math.max(0, text.length() - count);
        return newLength == 0 ? "" : substring(text, 0, newLength);
    }

    public static String removeSuffixIgnoreCase(String text, String suffix) {
        checkNotNull(text, "text");
        checkNotNull(suffix, "suffix");
        if (suffix.isEmpty()) {
            return text;
        }
        return endsWithIgnoreCase(text, suffix)
                ? text.substring(0, text.length() - suffix.length())
                : text;
    }

    public static String removePrefixIgnoreCase(String text, String prefix) {
        checkNotNull(text, "text");
        checkNotNull(prefix, "prefix");
        if (prefix.isEmpty()) {
            return text;
        }
        return startsWithIgnoreCase(text, prefix)
                ? text.substring(prefix.length())
                : text;
    }

    public static String removePrefix(String text, String prefix) {
        checkNotNull(text, "text");
        checkNotNull(prefix, "prefix");
        if (prefix.isEmpty()) {
            return text;
        }
        return text.startsWith(prefix)
                ? text.substring(prefix.length())
                : text;
    }

    public static String removePrefix(String text, int count) {
        checkNotNull(text, "text");
        checkNotNegative(count, "count");
        int length = text.length();
        return length - 1 < count ? "" : substring(text, count);
    }

    public static String reverse(String text) {
        checkNotNull(text, "text");
        int[] codePoints = codePoints(text);
        Integers.reverse(codePoints);
        return new String(codePoints, 0, codePoints.length);
    }

    public static String remove(String text, String searchString) {
        return replace(text, searchString, "", false);
    }

    public static String removeChars(String text, String chars) {
        return removeChars(text, BitSets.toBitSet(chars));
    }

    public static String removeChars(String text, BitSet bitSet) {
        checkNotNull(text, "text");
        checkNotNull(bitSet, "bitSet");
        StringBuilder builder = new StringBuilder();
        text.codePoints()
                .filter(cp -> !bitSet.get(cp))
                .forEach(builder::appendCodePoint);
        return builder.toString();
    }

    public static String removeIgnoreCase(String text, String searchString) {
        return replace(text, searchString, "", true);
    }

    public static String removeFirst(String text, String searchString) {
        return replaceFirst(text, searchString, "");
    }

    public static String removeFirstIgnoreCase(String text, String searchString) {
        return replaceFirstIgnoreCase(text, searchString, "");
    }

    public static String removeLast(String text, String searchString) {
        return replaceLast(text, searchString, "");
    }

    public static String removeLastIgnoreCase(String text, String searchString) {
        return replaceLastIgnoreCase(text, searchString, "");
    }

    public static String replaceFirst(String text, Pattern pattern, String replacement) {
        return pattern.matcher(text)
                .replaceFirst(replacement);
    }

    public static String replaceFirst(String text, Pattern pattern, Function<MatchResult, String> replacer) {
        return pattern.matcher(text)
                .replaceFirst(replacer);
    }

    public static String replace(String text, Pattern pattern, String replacement) {
        return pattern.matcher(text)
                .replaceAll(replacement);
    }

    public static String replace(String text, Pattern pattern, Function<MatchResult, String> replacer) {
        return pattern.matcher(text)
                .replaceAll(replacer);
    }

    public static String replace(String text, int searchCodePoint, int replacement) {
        if (text.isEmpty()) {
            return text;
        }
        int[] replaced = text.codePoints()
                .map(cp -> cp == searchCodePoint ? replacement : cp)
                .toArray();
        return codePointsToString(replaced);
    }

    public static String replace(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, false);
    }

    public static String replaceIgnoreCase(String text, String searchString, String replacement) {
        return replace(text, searchString, replacement, true);
    }

    public static String replaceFirst(String text, String searchString, String replacement) {
        int index = text.indexOf(searchString);
        if (index < 0 || searchString.isEmpty()) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length() - searchString.length() + replacement.length());
        builder.append(text, 0, index);
        builder.append(replacement);
        builder.append(text, index + searchString.length(), text.length());
        return builder.toString();
    }

    public static String replaceFirstIgnoreCase(String text, String searchString, String replacement) {
        int index = indexOfIgnoreCase(text, searchString);
        if (index < 0 || searchString.isEmpty()) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length() - searchString.length() + replacement.length());
        builder.append(text, 0, index);
        builder.append(replacement);
        builder.append(text, index + searchString.length(), text.length());
        return builder.toString();
    }

    public static String replaceLast(String text, String searchString, String replacement) {
        int index = text.lastIndexOf(searchString);
        if (index < 0 || searchString.isEmpty()) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length() - searchString.length() + replacement.length());
        builder.append(text, 0, index);
        builder.append(replacement);
        builder.append(text, index + searchString.length(), text.length());
        return builder.toString();
    }

    public static String replaceLastIgnoreCase(String text, String searchString, String replacement) {
        int index = lastIndexOfIgnoreCase(text, searchString);
        if (index < 0 || searchString.isEmpty()) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length() - searchString.length() + replacement.length());
        builder.append(text, 0, index);
        builder.append(replacement);
        builder.append(text, index + searchString.length(), text.length());
        return builder.toString();
    }

    private static String replace(String text, String searchString, String replacement, boolean ignoreCase) {
        if (isNullOrEmpty(text) || isNullOrEmpty(searchString) || replacement == null) {
            return text;
        }
        if (ignoreCase) {
            searchString = searchString.toLowerCase();
        }
        int start = 0;
        int end = ignoreCase
                ? indexOfIgnoreCase(text, searchString, start)
                : text.indexOf(searchString, start);
        if (end == -1) {
            return text;
        }
        int replLength = searchString.length();
        int increase = Math.max(replacement.length() - replLength, 0) * 16;
        StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != -1) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            end = ignoreCase
                    ? indexOfIgnoreCase(text, searchString, start)
                    : text.indexOf(searchString, start);
        }
        buf.append(text, start, text.length());
        return buf.toString();
    }

    public static String rightPad(String text, int size) {
        return rightPad(text, size, ' ');
    }

    public static String rightPad(String text, int size, char padChar) {
        checkNotNull(text, "text");
        checkNotNegative(size, "size");
        int pads = size - text.length();
        if (pads <= 0) {
            return text;
        }
        if (pads > PAD_LIMIT) {
            return rightPad(text, size, String.valueOf(padChar));
        }
        return text.concat(repeat(padChar, pads));
    }

    public static String rightPad(String text, int size, String pad) {
        checkNotNull(text, "text");
        checkNotNegative(size, "size");
        checkNotNull(pad, "pad");
        if (pad.isEmpty()) {
            return text;
        }
        int padLen = pad.length();
        int strLen = text.length();
        int pads = size - strLen;
        if (pads <= 0) {
            return text;
        }
        if (pad.length() == 1 && pads <= PAD_LIMIT) {
            return rightPad(text, size, pad.charAt(0));
        }
        if (pads == padLen) {
            return text.concat(pad);
        }
        char[] padding = new char[pads];
        char[] padChars = pad.toCharArray();
        for (int i = 0; i < pads; i++) {
            padding[i] = padChars[i % padLen];
        }
        return text.concat(new String(padding));
    }

    public static String stripAccents(String text) {
        checkNotNull(text, "text");
        StringBuilder decomposed = new StringBuilder(Normalizer.normalize(text, Normalizer.Form.NFD));
        for (int i = 0; i < decomposed.length(); i++) {
            if (decomposed.charAt(i) == '\u0141') {
                decomposed.setCharAt(i, 'L');
            } else if (decomposed.charAt(i) == '\u0142') {
                decomposed.setCharAt(i, 'l');
            }
        }
        return STRIP_ACCENTS_PATTERN.matcher(decomposed).replaceAll("");
    }

    public static String truncate(String text, int length) {
        return truncate(text, 0, length);
    }

    public static String truncate(String text, int offset, int length) {
        checkNotNull(text, "text");
        checkNotNegative(offset, "offset");
        checkNotNegative(length, "length");
        if (text.length() <= offset) {
            return "";
        }
        int count = Math.min(length, text.length() - offset);
        return text.substring(offset, offset + count);
    }

    public static String substring(String text, int start) {
        checkNotNull(text, "text");
        return substring(text, start, text.length());
    }

    public static String substring(String text, int start, int end) {
        checkNotNull(text, "text");
        int length = text.length();
        if (end < 0) {
            end = length + end;
        }
        if (start < 0) {
            start = length + start;
        }
        if (end > length) {
            end = length;
        }
        if (start > end) {
            return "";
        }
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
        return text.substring(start, end);
    }

    public static String firstToken(String text, char delimiter) {
        checkNotNull(text, "text");
        int index = text.indexOf(delimiter);
        return index < 0 ? text : text.substring(0, index);
    }

    public static String lastToken(String text, char delimiter) {
        checkNotNull(text, "text");
        int index = text.lastIndexOf(delimiter);
        return index < 0 ? text : text.substring(index + 1);
    }

    public static int[] codePoints(String text) {
        checkNotNull(text, "text");
        return text.codePoints()
                .toArray();
    }

    public static String codePointsToString(int[] codePoints) {
        checkNotNull(codePoints, "codePoints");
        return new String(codePoints, 0, codePoints.length);
    }

    public static String removeSurrogates(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isSurrogate(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean hasSurrogates(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isSurrogate(c)) {
                return true;
            }
        }
        return false;
    }

    public static int countSurrogatePairs(String text) {
        return text.length() - text.codePointCount(0, text.length());
    }
}
