package com.example.dataapi.transform;

import java.util.Locale;

public final class TextTransformer {

    private TextTransformer() {
    }

    public static String transform(String text) {
        return new StringBuilder(text).reverse().toString().toUpperCase(Locale.ROOT);
    }
}
