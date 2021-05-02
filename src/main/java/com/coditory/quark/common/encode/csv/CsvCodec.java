package com.coditory.quark.common.encode.csv;

import com.coditory.quark.common.encode.TranslationCodec;
import com.coditory.quark.common.encode.Translator;

public final class CsvCodec {
    private static final Translator ENCODER = CsvEncoder.getInstance();
    private static final Translator DECODER = CsvDecoder.getInstance();
    private static final TranslationCodec CODEC = new TranslationCodec(ENCODER, DECODER);

    public static TranslationCodec forSeparator(char separator) {
        if (separator == ',') {
            return CsvCodec.CODEC;
        }
        if (separator == '\t') {
            return TsvCodec.getInstance();
        }
        return new TranslationCodec(
                CsvEncoder.forSeparator(separator),
                CsvDecoder.forSeparator(separator)
        );
    }

    public static TranslationCodec getInstance() {
        return CODEC;
    }

    public static Translator getEncoder() {
        return CODEC.getEncoder();
    }

    public static Translator getDecoder() {
        return CODEC.getDecoder();
    }

    public static String encode(String input) {
        return CODEC.encode(input);
    }

    public static boolean encode(String input, StringBuilder out) {
        return CODEC.encode(input, out);
    }

    public static String decode(String input) {
        return CODEC.decode(input);
    }

    public static boolean decode(String input, StringBuilder out) {
        return CODEC.decode(input, out);
    }
}
